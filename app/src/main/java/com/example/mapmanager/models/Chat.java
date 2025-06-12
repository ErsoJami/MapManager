package com.example.mapmanager.models;

import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mapmanager.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    private String id;
    private String ownerId;
    private long lastMessageTime;
    private ArrayList<String> membersList;
    private String groupName;
    private String groupAvatarUrl;
    private DatabaseReference databaseReference;
    private String lastMessage;
    private String lastReadMessageId;
    public Chat() {
        this.membersList = new ArrayList<>();
    }
    public Chat(long lastMessageTime, ArrayList<String> membersList, String groupName, String groupAvatarUrl, String ownerId) {
        this.lastMessageTime = lastMessageTime;
        this.membersList = membersList;
        this.groupName = groupName;
        this.groupAvatarUrl = groupAvatarUrl;
        this.ownerId = ownerId;
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
        boolean flag = true;
        for (int i = 0; i < membersList.size(); i++) {
            if (membersList.get(i).equals(userId)) {
                flag = false;
                break;
            }
        }
        if (!flag) {
            return;
        }
        membersList.add(userId);
        HashMap<String, ChatsData> chatList = MainActivity.user.getUserChatsData();
        if (chatList == null) {
            chatList = new HashMap<>();
        }
        chatList.put(id, new ChatsData(new ArrayList<>(), ""));
        MainActivity.user.setUserChatsData(chatList);
        MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(userId));
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(id);
        HashMap<String, Object> data = new HashMap<>();
        data.put("groupName", this.groupName);
        data.put("groupAvatarUrl", this.groupAvatarUrl);
        data.put("lastMessageTime", this.lastMessageTime);
        data.put("membersList", this.membersList);
        databaseReference.updateChildren(data).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(String lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
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
        data.put("ownerId", this.ownerId);
        DatabaseReference newChat = databaseReference.push();
        this.id = newChat.getKey();
        newChat.setValue(data).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });
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
