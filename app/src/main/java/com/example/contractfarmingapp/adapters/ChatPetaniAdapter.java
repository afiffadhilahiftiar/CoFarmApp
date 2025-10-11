package com.example.contractfarmingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.ChatItem;

import java.util.List;

public class ChatPetaniAdapter extends RecyclerView.Adapter<ChatPetaniAdapter.ViewHolder> {

    private List<ChatItem> chatList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ChatItem item);
    }

    public ChatPetaniAdapter(List<ChatItem> chatList, OnItemClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_petani, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatItem item = chatList.get(position);
        holder.tvSenderName.setText(item.getSenderName());
        holder.tvLastMessage.setText(item.getLastMessage());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvLastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}

