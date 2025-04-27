package com.example.mapmanager.models;

import com.example.mapmanager.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private String id;
    private long lastMessageTime;
    private String lastReadMessageId;
    private ArrayList<String> membersList;
    private String groupName;
    private String groupAvatarUrl;
    private DatabaseReference databaseReference;
    public Chat() {
        this.membersList = new ArrayList<>();
    }
    public Chat(long lastMessageTime, ArrayList<String> membersList, String groupName, String groupAvatarUrl) {
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
        createNewChat();
    }
    public Chat(String id, long lastMessageTime, ArrayList<String> membersList, String groupName, String groupAvatarUrl) {
        this.id = id;
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
    }
    public void addNewMember(String userId) {
        membersList.add(userId);
        HashMap<String, String> chatList = MainActivity.user.getChatList();
        chatList.replace(id, "0");
        MainActivity.user.setChatList(chatList);
        MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(userId));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(id);
        Map<String, Object> data = new HashMap<>();
        data.put("groupName", this.groupName);
        data.put("groupAvatarUrl", this.groupAvatarUrl);
        data.put("lastMessageTime", this.lastMessageTime);
        data.put("membersList", this.membersList);
        databaseReference.updateChildren(data);
    }

    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public Chat(String id, long lastMessageTime, ArrayList<String> membersList, String groupName, String groupAvatarUrl, String lastReadMessageId) {
        this.id = id;
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
        this.lastReadMessageId = lastReadMessageId;
    }
    public void createNewChat() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chats");
        Map<String, Object> data = new HashMap<>();
        data.put("groupName", this.groupName);
        data.put("groupAvatarUrl", this.groupAvatarUrl);
        data.put("lastMessageTime", this.lastMessageTime);
        data.put("membersList", this.membersList);
        DatabaseReference newChat = databaseReference.push();
        this.id = newChat.getKey();
        newChat.setValue(data);
    }
    public ArrayList<String> getMembersList() {
        return membersList;
    }

    public void setMembersList(ArrayList<String> membersList) {
        this.membersList = membersList;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public String getGroupAvatarUrl() {
        return groupAvatarUrl;
    }

    public void setGroupAvatarUrl(String groupAvatarUrl) {
        this.groupAvatarUrl = groupAvatarUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
