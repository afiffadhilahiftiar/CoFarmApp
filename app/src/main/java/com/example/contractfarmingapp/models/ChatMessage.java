package com.example.contractfarmingapp.models;

public class ChatMessage {
    public String sender;
    public int receiverId;
    public String message;
    public long timestamp;
    public boolean isSender; // Tambahan
    public boolean isSticker; // Tambahkan ini

    public ChatMessage(String sender, int receiverId, String message, long timestamp) {
        this.sender = sender;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isSender = sender.equalsIgnoreCase("Admin");
    }
}
