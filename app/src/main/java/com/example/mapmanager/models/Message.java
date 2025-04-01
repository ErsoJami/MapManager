package com.example.mapmanager.models;

import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private int typeMessage;  // 0 - текс, 1 - избражение
    private String userId;
    private String messageId;
    private Timestamp time;
    private String message;
    private DatabaseReference databaseReference;
    public Message() {
        this.typeMessage = 0;
        this.userId = null;
        this.messageId = null;
        this.time = null;
        this.message = null;
    }
    public Message(int typeMessage, String userId, String messageId, Timestamp time, String message) {
        this.typeMessage = typeMessage;
        this.userId = userId;
        this.messageId = messageId;
        this.time = time;
        this.message = message;
    }

    public void createNewMessage(String chatId) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages");
        Map<String, Object> data = new HashMap<>();
        data.put("typeMessage", this.typeMessage);
        data.put("userId", this.userId);
        data.put("time", this.time);
        data.put("message", this.message);
        DatabaseReference newChat = databaseReference.push();
        this.messageId = newChat.getKey();
        newChat.setValue(data);
    }
    public int getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(int typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

}
