package com.example.contractfarmingapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.contractfarmingapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<ChatMessage>> chatMessages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ChatMessage>> getChatMessages() {
        return chatMessages;
    }

    public void addMessage(ChatMessage message) {
        List<ChatMessage> current = chatMessages.getValue();
        if (current == null) {
            current = new ArrayList<>();
        }
        current.add(message);
        chatMessages.setValue(current);
    }

    public void setMessages(List<ChatMessage> messages) {
        chatMessages.setValue(messages != null ? messages : new ArrayList<>());
    }

    public void clearMessages() {
        chatMessages.setValue(new ArrayList<>());
    }
}
