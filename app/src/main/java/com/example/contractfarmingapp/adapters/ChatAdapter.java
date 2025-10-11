package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.ChatMessage;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());


    private static final int VIEW_TYPE_SENDER = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;


    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSender ? VIEW_TYPE_SENDER : VIEW_TYPE_RECEIVER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENDER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String time = timeFormat.format(new Date(message.timestamp));

        if (holder instanceof SenderViewHolder) {
            ((SenderViewHolder) holder).bind(message, time);
        } else {
            ((ReceiverViewHolder) holder).bind(message, time);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    abstract static class BaseViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        ImageView imgSticker, imgFilePreview;
        ProgressBar progressDownload;

        BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgSticker = itemView.findViewById(R.id.imgSticker);
            imgFilePreview = itemView.findViewById(R.id.imgFilePreview);
            progressDownload = itemView.findViewById(R.id.downloadProgress);
        }

        void bind(ChatMessage message, String time) {
            txtTime.setText(time);
            String content = message.message;

            // Reset semua view
            imgSticker.setVisibility(View.GONE);
            imgFilePreview.setVisibility(View.GONE);
            txtMessage.setVisibility(View.VISIBLE);
            txtMessage.setOnClickListener(null);

            if (content.startsWith("[sticker]")) {
                showSticker(itemView.getContext(), content.replace("[sticker]", ""));
            } else if (content.startsWith("[file]")) {
                String fileUrl = content.replace("[file]", "");
                showFile(itemView.getContext(), fileUrl);
            } else {
                txtMessage.setText(content);
            }
        }

        void showSticker(Context context, String stickerName) {
            try {
                InputStream is = context.getAssets().open("stickers/" + stickerName);
                Drawable drawable = Drawable.createFromStream(is, null);
                imgSticker.setImageDrawable(drawable);
                imgSticker.setVisibility(View.VISIBLE);
                txtMessage.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void showFile(Context context, String fileUrl) {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            txtMessage.setText("📎 " + fileName);
            File localFile = new File(context.getExternalFilesDir("chat_files"), fileName);


            // Buat direktori jika belum ada
            if (!localFile.getParentFile().exists()) {
                localFile.getParentFile().mkdirs();
            }

            if (localFile.exists()) {
                // Sudah ada, tampilkan langsung
                txtMessage.setOnClickListener(v -> openFile(context, localFile));
                if (isImageFile(fileName)) {
                    imgFilePreview.setImageURI(Uri.fromFile(localFile));
                    imgFilePreview.setVisibility(View.VISIBLE);
                    txtMessage.setVisibility(View.GONE);

                    imgFilePreview.setOnClickListener(v -> {
                        Intent intent = new Intent(context, com.example.contractfarmingapp.ImagePreviewActivity.class);
                        intent.putExtra("image_url", localFile.getAbsolutePath());
                        context.startActivity(intent);
                    });
                }

            } else {
                // Belum ada, tampilkan progress dan unduh
                progressDownload.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        InputStream in = new java.net.URL(fileUrl).openStream();
                        java.io.FileOutputStream out = new java.io.FileOutputStream(localFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                        in.close();
                        out.close();

                        imgFilePreview.post(() -> {
                            progressDownload.setVisibility(View.GONE);
                            txtMessage.setOnClickListener(v -> openFile(context, localFile));
                            if (isImageFile(fileName)) {
                                imgFilePreview.setImageURI(Uri.fromFile(localFile));
                                imgFilePreview.setVisibility(View.VISIBLE);
                                txtMessage.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        imgFilePreview.post(() -> {
                            progressDownload.setVisibility(View.GONE);
                            txtMessage.setText("Gagal mengunduh file");
                        });
                    }
                }).start();
            }
        }
        private boolean isImageFile(String name) {
            return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
        }

        private void openFile(Context context, File file) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = androidx.core.content.FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider", file);

            intent.setDataAndType(uri, getMimeType(file.getName()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        private String getMimeType(String fileName) {
            if (fileName.endsWith(".pdf")) return "application/pdf";
            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) return "application/msword";
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) return "application/vnd.ms-excel";
            if (fileName.endsWith(".txt")) return "text/plain";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
            if (fileName.endsWith(".png")) return "image/png";
            return "*/*";
        }

    }

    static class SenderViewHolder extends BaseViewHolder {
        SenderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ReceiverViewHolder extends BaseViewHolder {
        ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}