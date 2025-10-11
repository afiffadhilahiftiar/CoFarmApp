package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.activities.ContractDetailActivity;
import com.example.contractfarmingapp.models.KontrakModel;

import java.util.ArrayList;
import java.util.List;

public class KontrakAdapter extends RecyclerView.Adapter<KontrakAdapter.ViewHolder> {

    private Context context;
    private List<KontrakModel> kontrakList;

    public KontrakAdapter(Context context, List<KontrakModel> kontrakList) {
        this.context = context;
        this.kontrakList = kontrakList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvNamaPerusahaan, tvKebutuhan, tvJumlahKebutuhan, tvHarga, tvWaktu, tvRating, tvJumlahKontrak, tvLokasi, tvTimeUpload;
        ImageView imgLogo, icVerified;
        LinearLayout layoutSyaratTags; // TAMBAH INI

        public ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvId);
            tvKebutuhan = itemView.findViewById(R.id.tvKebutuhan);
            tvJumlahKebutuhan = itemView.findViewById(R.id.tvJumlahKebutuhan);
            tvNamaPerusahaan = itemView.findViewById(R.id.tvNamaPerusahaan);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvLokasi = itemView.findViewById(R.id.tvLokasi);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            tvWaktu = itemView.findViewById(R.id.tvWaktu);
            tvJumlahKontrak = itemView.findViewById(R.id.tvJumlahKontrak);
            layoutSyaratTags = itemView.findViewById(R.id.layoutSyaratTags);
            tvTimeUpload = itemView.findViewById(R.id.tvTimeUpload);
            imgLogo = itemView.findViewById(R.id.imgLogo);
            icVerified = itemView.findViewById(R.id.icVerified);

        }
    }

    @Override
    public KontrakAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kontrak, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(KontrakAdapter.ViewHolder holder, int position) {
        KontrakModel m = kontrakList.get(position);
        holder.tvId.setText(String.format("%s", m.getId()));
        holder.tvKebutuhan.setText(String.format("%s |", m.getKebutuhan()));
        String rawJumlah = String.valueOf(m.getJumlahkebutuhan());
        String angkaJumlah = rawJumlah.replace(".", "").replace(",", "").replace(" ", "");
        int jumlahInt = 0;
        try {
            jumlahInt = Integer.parseInt(angkaJumlah);
        } catch (NumberFormatException e) {
            jumlahInt = 0;
        }
        String jumlahFormatted = String.format("%,d", jumlahInt).replace(',', '.');
        holder.tvJumlahKebutuhan.setText(String.format("%s %s", jumlahFormatted, m.getSatuan()));

        holder.tvNamaPerusahaan.setText(m.getNamaPerusahaan());
        if (m.getCertificate() != null && !m.getCertificate().isEmpty()) {
            holder.icVerified.setVisibility(View.VISIBLE);
        } else {
            holder.icVerified.setVisibility(View.GONE);
        }

        holder.tvRating.setText(String.format("Rating: %s⭐", m.getRating()));
        holder.tvLokasi.setText(String.format("Lokasi: %s", m.getLokasi()));

        String rawHarga = m.getHargaPerKg(); // misal: "Rp 5.000" atau "5000"
        String angkaHarga = rawHarga.replace("Rp", "").replace(" ", "").replace(".", "");
        int hargaInt = 0;
        try {
            hargaInt = Integer.parseInt(angkaHarga);
        } catch (NumberFormatException e) {
            hargaInt = 0; // fallback jika gagal parse
        }
        String hargaFormatted = String.format("Rp%,d", hargaInt).replace(',', '.');
        holder.tvHarga.setText(String.format("Harga: %s/%s", hargaFormatted, m.getSatuan()));
        holder.tvWaktu.setText(String.format("Waktu: %s", m.getWaktuDibutuhkan()));
        holder.tvJumlahKontrak.setText(String.format("%s peserta telah ajukan kontrak", m.getJumlahKontrak()));
        // TAGS SYARAT
        holder.layoutSyaratTags.removeAllViews();
        String[] tags = m.getPersyaratan().split(",");
        for (String tag : tags) {
            TextView tagView = new TextView(context);
            tagView.setText(tag.trim());
            tagView.setTextSize(12);
            tagView.setTextColor(context.getResources().getColor(R.color.white));
            tagView.setPadding(20, 10, 20, 10);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(context.getResources().getColor(R.color.teal_700));
            bg.setCornerRadius(40);
            tagView.setBackground(bg);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            tagView.setLayoutParams(params);

            holder.layoutSyaratTags.addView(tagView);
        }
        holder.tvTimeUpload.setText(String.format("Diunggah: %s", m.getTimeUpload()));
        Glide.with(context)
                .load(m.getLogoUrl())
                .placeholder(R.drawable.store_icon)
                .error(R.drawable.store_icon)
                .into(holder.imgLogo);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ContractDetailActivity.class);
            intent.putExtra("contract_id", m.getId()); // Kirim ID ke detail
            context.startActivity(intent);
        });


// tampilkan icon jika certificate tidak kosong
        if (m.getCertificate() != null && !m.getCertificate().isEmpty()) {
            holder.icVerified.setVisibility(View.VISIBLE);
        } else {
            holder.icVerified.setVisibility(View.GONE);
        }

    }
    public void updateList(ArrayList<KontrakModel> newList) {
        kontrakList.clear();
        kontrakList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return kontrakList.size();
    }
}
