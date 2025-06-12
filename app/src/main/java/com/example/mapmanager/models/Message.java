package com.example.mapmanager.models;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.mapmanager.MainActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Message {
    private int typeMessage;  // 0 - текс, 1 - избражение
    private String userId;
    private String nick;
    private String messageId;
    private long time;
    private String message;
    private DatabaseReference databaseReference;
    private ArrayList<String> mediaList;
    public Message() {
        this.typeMessage = 0;
        this.userId = null;
        this.messageId = null;
        this.time = 0;
        this.message = null;
    }
    public Message(int typeMessage, String userId, long time, String message) {
        this.typeMessage = typeMessage;
        this.userId = userId;
        this.time = time;
        this.message = message;
    }
    public Message(int typeMessage, String userId, long time, String message, ArrayList<String> mediaList) {
        this.typeMessage = typeMessage;
        this.userId = userId;
        this.time = time;
        this.message = message;
        this.mediaList = mediaList;
    }
    public Message(int typeMessage, String userId, String nick, long time, String message) {
        this.typeMessage = typeMessage;
        this.userId = userId;
        this.time = time;
        this.nick = nick;
        this.message = message;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    public void setDatabaseReference(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    public void createNewMessage(String chatId) {
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages");
        Map<String, Object> data = new HashMap<>();
        data.put("typeMessage", this.typeMessage);
        data.put("userId", this.userId);
        data.put("time", this.time);
        data.put("message", this.message);
        data.put("mediaList", this.mediaList);
        DatabaseReference newMessage = databaseReference.push();
        this.messageId = newMessage.getKey();
        FirebaseDatabase.getInstance().getReference().child("users").child(this.userId).child("nick").get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.getValue(String.class);
                    setNick(userName);
                    data.put("nick", userName);
                    newMessage.setValue(data);
                    HashMap<String, ChatsData> chatList = MainActivity.user.getUserChatsData();
                    ChatsData chatsData = chatList.get(chatId);
                    ArrayList<String> messageList = chatsData.getMessageList();
                    if (messageList == null) {
                        messageList = new ArrayList<>();
                    }
                    messageList.add(messageId);
                    chatsData.setMessageList(messageList);
                    chatList.replace(chatId, chatsData);
                    MainActivity.user.setUserChatsData(chatList);
                    MainActivity.user.changeData(FirebaseDatabase.getInstance().getReference().child("users").child(userId));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO доделать сообщение об исключении
            }
        });

    }
    public void updateMessage(String chatId) {
        if (this.messageId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages").child(this.messageId);
            Map<String, Object> data = new HashMap<>();
            data.put("typeMessage", this.typeMessage);
            data.put("userId", this.userId);
            data.put("time", this.time);
            data.put("message", this.message);
            data.put("nick", this.nick);
            databaseReference.updateChildren(data).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO доделать сообщение об исключении
                }
            });
        }
    }
    public void deleteMessage(String chatId) {
        if (this.messageId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("messages").child(this.messageId);
            databaseReference.removeValue().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO доделать сообщение об исключении
                }
            });
        }
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String userName) {
        this.nick = userName;
    }

    public int getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(int typeMessage) {
        this.typeMessage = typeMessage;
    }

    public ArrayList<String> getMediaList() {
        return mediaList;
    }

    public void setMediaList(ArrayList<String> mediaList) {
        this.mediaList = mediaList;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
