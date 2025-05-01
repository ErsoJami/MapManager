package com.example.mapmanager.models;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatsData {
    private ArrayList<String> messageList;
    private String lastMessageId;

    public ChatsData() {}
    public ChatsData(ArrayList<String> messageList, String lastMessageId) {
        this.lastMessageId = lastMessageId;
        this.messageList = messageList;
    }
    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public void setMessageList(ArrayList<String> messageList) {
        this.messageList = messageList;
    }
}
