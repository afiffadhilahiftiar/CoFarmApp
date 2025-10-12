package com.example.contractfarmingapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.ColorStateList;
import android.graphics.Color;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.Petani;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PetaniAdapter extends RecyclerView.Adapter<PetaniAdapter.ViewHolder> {

    private final List<Petani> petaniList;
    private final OnPetaniActionListener listener;

    public interface OnPetaniActionListener {
        void onItemClick(Petani petani);
        void onValidasiClick(Petani petani);
        void onKlaimDiterimaClick(Petani petani);
        void onBeriUlasan(Petani petani);
        void onChatClick(Petani petani);
        void onLihatKontrakClick(Petani petani);
        void onValidasiLogistikClick(Petani petani);
        void onUpdateSopir(Petani petani);
        void onKlaimDanaClick(Petani petani);

    }

    public PetaniAdapter(List<Petani> petaniList, OnPetaniActionListener listener) {
        this.petaniList = petaniList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_petani, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Petani petani = petaniList.get(position);

        holder.txtNama.setText(petani.nama);
        NumberFormat rupiahFormat = NumberFormat.getInstance(new Locale("id", "ID"));
        long harga = Long.parseLong(petani.harga);
        holder.txtHarga.setText("Rp" + rupiahFormat.format(harga));
        holder.txtPerusahaan.setText("Perusahaan: " + petani.companyName);
        holder.txtLahan.setText("Lahan: " + petani.lahan);
        holder.txtProgres.setText("Progres: " + petani.progres);
        holder.txtCatatan.setText("Catatan: " + petani.catatan);

        // Status & tombol validasi
        String status = petani.status != null ? petani.status : "";
        String statusKlaim = petani.statusKlaim != null ? petani.statusKlaim : "";
        holder.btnKlaimDana.setEnabled(true);
        switch (statusKlaim) {
            case "Klaim Dana":
                holder.btnKlaimDana.setText("Klaim dana cadangan untuk petani");
                holder.btnKlaimDana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnKlaimDana.setOnClickListener(v -> listener.onKlaimDanaClick(petani));
                break;
            case "Menunggu Validasi":
                holder.btnKlaimDana.setText("Menunggu validasi klaim dana");
                holder.btnKlaimDana.setVisibility(View.VISIBLE);
                holder.btnKlaimDana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnKlaimDana.setOnClickListener(null);
                break;
            case "Ditolak":
                holder.btnKlaimDana.setText("Klaim Dana Ditolak");
                holder.btnKlaimDana.setVisibility(View.VISIBLE);
                holder.btnKlaimDana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                holder.btnKlaimDana.setOnClickListener(null);
                break;
            case "Dibayar":
                holder.btnKlaimDana.setText("Klaim dana dibayar fasilitator (Konfirmasi)");
                holder.btnKlaimDana.setVisibility(View.VISIBLE);
                holder.btnKlaimDana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnKlaimDana.setOnClickListener(v -> listener.onKlaimDiterimaClick(petani));
                break;
            case "Dana Cadangan Diterima":
                holder.btnKlaimDana.setText("Pembayaran klaim diterima petani");
                holder.btnKlaimDana.setVisibility(View.VISIBLE);
                holder.btnKlaimDana.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnKlaimDana.setOnClickListener(null);
                break;
        }

        holder.btnValidasi.setEnabled(true); // Reset sebelum diproses
        switch (status) {
            case "Kontrak divalidasi admin poktan":
                holder.btnValidasi.setText("Dikirim ke fasilitator");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak divalidasi fasilitator":
                holder.btnValidasi.setText("Dikirim ke offtaker");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Biru
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak direview lebih lanjut oleh perusahaan":
                holder.btnValidasi.setText("Direview Offtaker");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak diterima oleh perusahaan":
                holder.btnValidasi.setText("Diterima Offtaker");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // oranye
                holder.btnValidasi.setOnClickListener(null);
                holder.btnKlaimDana.setEnabled(true);
                holder.btnKlaimDana.setVisibility(View.VISIBLE);

                break;
            case "Perusahaan tidak memproses kontrak lebih lanjut":
                holder.btnValidasi.setText("Ditolak Offtaker");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Merah
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak ditolak admin poktan":
                holder.btnValidasi.setText("Ditolak admin");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Merah
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak ditolak fasilitator":
                holder.btnValidasi.setText("Ditolak fasilitator");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Merah
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Kontrak selesai":
                holder.btnValidasi.setText("Berikan Ulasan");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Biru
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;
            case "Review offtaker selesai":
                holder.btnValidasi.setText("Berikan Ulasan");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;
            case "Review poktan selesai":
                holder.btnValidasi.setText("Selesai");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Biru
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;
            case "Menunggu persetujuan admin poktan":
                holder.btnValidasi.setText("Validasi");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onValidasiLogistikClick(petani));
                break;
            case "Pengiriman barang dilakukan":
                holder.btnValidasi.setText("Update Sopir");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onUpdateSopir(petani));
                break;
            case "Memilih logistik":
                holder.btnValidasi.setText("Perusahaan sedang memilih logistik");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onUpdateSopir(petani));
                break;
            case "Pesanan sampai":
                holder.btnValidasi.setText("Pesanan sampai");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(null);
                break;
            case "Sedang dalam perjalanan":
                holder.btnValidasi.setText("Sedang dalam perjalanan");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onUpdateSopir(petani));
                break;
            case "Menunggu validasi admin poktan":
            default:
                holder.btnValidasi.setText("Validasi");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onValidasiClick(petani));
                break;

        }

        // Tombol lain selalu aktif
        holder.btnChat.setOnClickListener(v -> listener.onChatClick(petani));
        holder.btnLihatKontrak.setOnClickListener(v -> listener.onLihatKontrakClick(petani));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(petani);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petaniList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtHarga, txtPerusahaan, txtLahan, txtProgres, txtCatatan;
        Button btnValidasi, btnChat, btnLihatKontrak, btnKlaimDana; // Tambah button klaim

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtHarga = itemView.findViewById(R.id.txtHarga);
            txtPerusahaan = itemView.findViewById(R.id.txtPerusahaan);
            txtLahan = itemView.findViewById(R.id.txtLahan);
            txtProgres = itemView.findViewById(R.id.txtProgres);
            txtCatatan = itemView.findViewById(R.id.txtCatatan);
            btnValidasi = itemView.findViewById(R.id.btnValidasi);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnLihatKontrak = itemView.findViewById(R.id.btnLihatKontrak);
            btnKlaimDana = itemView.findViewById(R.id.btnKlaimDana); // Inisialisasi
        }
    }

}
