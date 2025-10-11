package com.example.contractfarmingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.NotificationModel;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationModel> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onClick(NotificationModel notification);
    }

    public NotificationAdapter(List<NotificationModel> notificationList) {
        this.notificationList = notificationList;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<NotificationModel> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        View itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = itemView;
            title = itemView.findViewById(R.id.tvNotificationTitle);
            message = itemView.findViewById(R.id.tvNotificationMessage);
            time = itemView.findViewById(R.id.tvNotificationTime);
        }

        public void bind(NotificationModel model) {
            title.setText(model.getTitle());
            message.setText(model.getMessage());
            time.setText(model.getTime());
            itemLayout.setAlpha(model.isRead() ? 0.5f : 1.0f);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(model);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notificationList.get(position));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }
}
