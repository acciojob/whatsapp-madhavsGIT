package com.driver;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    //add user
    //If the mobile number exists in database, throw "User already exists" exception
    //Otherwise, create the user and return "SUCCESS"

    public String createUser(String name, String mobileNo) throws Exception{
        if(userMobile.contains(mobileNo)){
            throw new Exception("User already exists");
        }else{
            userMobile.add(mobileNo);
            User user = new User(name, mobileNo);
        }
        return "SUCCESS";
    }


    // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
    // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
    // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
    // Note that a personal chat is not considered a group and the count is not updated for personal chats.
    // If group is successfully created, return group.

    //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
    //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

    public Group createGroup(List<User> users){

        if(users.size() == 2){
            Group group = new Group(users.get(0).getName(),2);
            groupUserMap.put(group, users);
            adminMap.put(group, users.get(1));
            return group;
        }
        if(users.size() > 2){
            customGroupCount++;
            String groupName = "Group "+ customGroupCount;
            Group group = new Group( groupName,users.size());
            groupUserMap.put(group, users);
            adminMap.put(group, users.get(0));
            return group;
        }
        return null;
    }

    public int createMessage(String content){
        messageId++;

        Message message = new Message();
        message.setId(messageId);
        message.setContent(content);
//        message.setTimestamp(new Date());
        return messageId;
    }

    //Throw "Group does not exist" if the mentioned group does not exist
    //Throw "You are not allowed to send message" if the sender is not a member of the group
    //If the message is sent successfully, return the final number of messages in that group.
    public int sendMessage(Message message, User sender, Group group) throws Exception{
        if(groupUserMap.containsKey(group)){
            for(List<User> userList : groupUserMap.values()) {
                if(userList.contains(sender)){
                    senderMap.put(message,sender);
                    groupMessageMap.get(group).add(message);

                }else{
                    throw  new Exception("You are not allowed to send message");
                }
            }
        }else{
            throw new Exception("Group does not exist");
        }
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Change the admin of the group to "user".
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin
        if(adminMap.containsKey(group)){
            if(adminMap.get(group).equals(approver)){
                if(groupUserMap.get(group).contains(user)){
                    adminMap.put(group,user);
                }else{
                    throw new Exception("User is not a participant");
                }
            }else{
                throw new Exception("Approver does not have rights");
            }
        }else{
            throw new Exception("Group does not exist");
        }
        return "SUCCESS";
    }


    public int removeUser(User user) throws Exception{

        boolean userFound = false;
        int result = 0;
        Group ansGroup = null;
        for (Group group : groupUserMap.keySet()) {
            List<User> userList = groupUserMap.get(group);
            if (userList.contains(user)) {

                if (adminMap.containsValue(user)) {
                    throw new Exception("Cannot remove admin");
                }
                userFound = true;
                ansGroup = group;
                break;
            }
        }
        if(userFound == false) {
            throw new Exception("User not found");
        }else
        {
            List<User> updatedUsers = new ArrayList<>();
            for(User u : groupUserMap.get(ansGroup)){
                if(u.equals(user)) continue;
                updatedUsers.add(u);
            }
            groupUserMap.put(ansGroup, updatedUsers);

            //groupmessageMap

            List<Message> updatedMessages = new ArrayList<>();
            for(Message m : groupMessageMap.get(ansGroup)){
                if(senderMap.get(m).equals(user)){
                    continue;
                }
                updatedMessages.add(m);
            }
            groupMessageMap.put(ansGroup, updatedMessages);

            //sendermap

            HashMap<Message,User> updatedSenderMap = new HashMap<>();
            for(Message message : senderMap.keySet()){
                if(senderMap.get(message).equals(user)){
                    continue;
                }
                updatedSenderMap.put(message, senderMap.get(message));
            }
            senderMap = updatedSenderMap;

            result = updatedUsers.size() + updatedMessages.size() + senderMap.size();


        }
        return result;

    }


}
