package com.example.contractfarmingapp.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.PetaniFasilitator;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PetaniAdapterFasilitator extends RecyclerView.Adapter<PetaniAdapterFasilitator.ViewHolder> {

    private final List<PetaniFasilitator> petaniList;
    private final OnPetaniActionListener listener;

    public interface OnPetaniActionListener {
        void onValidasiClick(PetaniFasilitator petani);
        void onBeriUlasan(PetaniFasilitator petani);
        void onChatClick(PetaniFasilitator petani);
        void onLihatKontrakClick(PetaniFasilitator petani);
        void onValidasiLogistikClick(PetaniFasilitator petani);
        void onUpdateSopir(PetaniFasilitator petani);
        void onKlaimDanaClick(PetaniFasilitator petani);
    }

    public PetaniAdapterFasilitator(List<PetaniFasilitator> petaniList, OnPetaniActionListener listener) {
        this.petaniList = petaniList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_petani_fasilitator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PetaniFasilitator petani = petaniList.get(position);

        holder.txtNama.setText(petani.nama);
        NumberFormat rupiahFormat = NumberFormat.getInstance(new Locale("id", "ID"));

        long harga = 0;
        try {
            harga = Long.parseLong(petani.harga);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.txtHarga.setText("Rp" + rupiahFormat.format(harga));
        holder.txtPerusahaan.setText("Offtaker: " + petani.companyName);
        holder.txtPoktan.setText("Kelompok Tani: " + petani.poktanName);
        holder.txtLahan.setText("Lahan: " + petani.lahan);
        holder.txtProgres.setText("Progres: " + petani.progres);
        holder.txtCatatan.setText("Catatan: " + petani.catatan);

        // Status & tombol validasi
        String status = petani.status != null ? petani.status : "";

        holder.btnValidasi.setEnabled(true); // Reset sebelum diproses
        switch (status) {

            case "Kontrak divalidasi fasilitator":
                holder.btnValidasi.setText("Sudah Validasi");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Kontrak direview lebih lanjut oleh perusahaan":
                holder.btnValidasi.setText("Direview Offtaker");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Kontrak diterima oleh perusahaan":
                holder.btnValidasi.setText("Diterima Offtaker");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnKlaimDana.setEnabled(true);
                holder.btnKlaimDana.setVisibility(View.GONE);
                holder.btnKlaimDana.setOnClickListener(v -> listener.onKlaimDanaClick(petani));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Perusahaan tidak memproses kontrak lebih lanjut":
            case "Kontrak ditolak admin poktan":
            case "Kontrak ditolak fasilitator":
                holder.btnValidasi.setText("Ditolak");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Kontrak selesai":
                holder.btnValidasi.setText("Kontrak Selesai");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Review offtaker selesai":
            case "Review poktan selesai":
                holder.btnValidasi.setText("Selesai");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Menunggu persetujuan admin poktan":
                holder.btnValidasi.setText("Pengajuan logistik");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Pengiriman barang dilakukan":
                holder.btnValidasi.setText("Update Sopir");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnValidasi.setOnClickListener(v -> listener.onUpdateSopir(petani));
                break;

            case "Memilih logistik":
                holder.btnValidasi.setText("Perusahaan sedang memilih logistik");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Pesanan sampai":
                holder.btnValidasi.setText("Pesanan sampai");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Sedang dalam perjalanan":
                holder.btnValidasi.setText("Sedang dalam perjalanan");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Menunggu validasi admin poktan":
                holder.btnValidasi.setText("Proses admin poktan");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(null);
                break;

            default:
                holder.btnValidasi.setText("Validasi");
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                holder.btnValidasi.setOnClickListener(v -> listener.onValidasiClick(petani));
                break;
        }

        // Tombol lain selalu aktif
        holder.btnChat.setOnClickListener(v -> listener.onChatClick(petani));
        holder.btnLihatKontrak.setOnClickListener(v -> listener.onLihatKontrakClick(petani));
    }

    @Override
    public int getItemCount() {
        return petaniList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtHarga, txtPerusahaan, txtPoktan, txtLahan, txtProgres, txtCatatan;
        Button btnValidasi, btnChat, btnLihatKontrak, btnKlaimDana;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtHarga = itemView.findViewById(R.id.txtHarga);
            txtPerusahaan = itemView.findViewById(R.id.txtPerusahaan);
            txtPoktan = itemView.findViewById(R.id.txtPoktan);
            txtLahan = itemView.findViewById(R.id.txtLahan);
            txtProgres = itemView.findViewById(R.id.txtProgres);
            txtCatatan = itemView.findViewById(R.id.txtCatatan);
            btnValidasi = itemView.findViewById(R.id.btnValidasi);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnLihatKontrak = itemView.findViewById(R.id.btnLihatKontrak);
            btnKlaimDana = itemView.findViewById(R.id.btnKlaimDana);
        }
    }
}
