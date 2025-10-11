package com.example.contractfarmingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.UlasanModel;

import java.util.List;

public class UlasanAdapter extends RecyclerView.Adapter<UlasanAdapter.UlasanViewHolder> {

    private List<UlasanModel> ulasanList;

    public UlasanAdapter(List<UlasanModel> ulasanList) {
        this.ulasanList = ulasanList;
    }

    @NonNull
    @Override
    public UlasanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ulasan, parent, false);
        return new UlasanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UlasanViewHolder holder, int position) {
        UlasanModel ulasan = ulasanList.get(position);
        holder.tvNamaPerusahaan.setText(ulasan.getNamaPerusahaan());
        holder.tvUlasan.setText(ulasan.getUlasan());
        holder.ratingBar.setRating(ulasan.getRating());
    }

    @Override
    public int getItemCount() {
        return ulasanList.size();
    }

    public static class UlasanViewHolder extends RecyclerView.ViewHolder {
        TextView tvNamaPerusahaan, tvUlasan;
        RatingBar ratingBar;

        public UlasanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);
            tvUlasan = itemView.findViewById(R.id.tvUlasan);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
