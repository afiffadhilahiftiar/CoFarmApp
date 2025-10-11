package com.example.contractfarmingapp.models;

public class ChatItem {
    private String senderId;
    private String senderName;
    private String lastMessage;

    public ChatItem(String senderId, String senderName, String lastMessage) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.lastMessage = lastMessage;
    }

    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getLastMessage() { return lastMessage; }
}

