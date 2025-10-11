package com.example.contractfarmingapp.adapters;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.List;

public class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {

    private List<String> stickers;
    private OnStickerClickListener listener;

    public interface OnStickerClickListener {
        void onStickerClick(String stickerName);
    }

    public StickerAdapter(List<String> stickers, OnStickerClickListener listener) {
        this.stickers = stickers;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String stickerName = stickers.get(position);
        try {
            InputStream is = holder.itemView.getContext().getAssets().open("stickers/" + stickerName);
            Drawable drawable = Drawable.createFromStream(is, null);
            ((ImageView) holder.itemView).setImageDrawable(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(v -> listener.onStickerClick(stickerName));
    }

    @Override
    public int getItemCount() {
        return stickers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}

