package com.example.mapmanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private String id;
    private Timestamp lastMessageTime;
    private List<String> membersList;
    private String groupName;
    private String groupAvatarUrl;
    private DatabaseReference databaseReference;
    public Chat() {
        this.membersList = new ArrayList<>();
    }
    public Chat(Timestamp lastMessageTime, List<String> membersList, String groupName, String groupAvatarUrl) {
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
        createNewChat();
    }
    public Chat(String id, Timestamp lastMessageTime, List<String> membersList, String groupName, String groupAvatarUrl) {
        this.id = id;
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
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
    public List<String> getMembersList() {
        return membersList;
    }

    public void setMembersList(List<String> membersList) {
        this.membersList = membersList;
    }

    public Timestamp getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Timestamp lastMessageTime) {
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
