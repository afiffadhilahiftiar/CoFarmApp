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
import com.example.contractfarmingapp.models.Petani;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PetaniAdapterOfftaker extends RecyclerView.Adapter<PetaniAdapterOfftaker.ViewHolder> {

    private final List<Petani> petaniList;
    private final OnPetaniActionListener listener;

    public interface OnPetaniActionListener {
        void onValidasiClick(Petani petani);
        void onKontrakSelesaiClick(Petani petani);
        void onKontrakLogistikClick(Petani petani);
        void onKontrakLogistikPetani(Petani petani);
        void onKontrakSampaiClick(Petani petani);
        void onUpdateSopir(Petani petani);
        void onPesananSampai(Petani petani);
        void onChatClick(Petani petani);
        void onLihatKontrakClick(Petani petani);
        void onLihatLahanClick(Petani petani);
        void onBeriUlasan(Petani petani);
        void onAjukanGantiRugiClick(Petani petani); // <-- ditambahkan
    }

    public PetaniAdapterOfftaker(List<Petani> petaniList, OnPetaniActionListener listener) {
        this.petaniList = petaniList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_petani_offtaker, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Petani petani = petaniList.get(position);

        holder.txtNama.setText(petani.nama);
        NumberFormat rupiahFormat = NumberFormat.getInstance(new Locale("id", "ID"));
        long harga = Long.parseLong(petani.harga);
        holder.txtHarga.setText("Rp" + rupiahFormat.format(harga));
        holder.txtPerusahaan.setText("Kelompok Tani: " + petani.companyName);
        holder.txtLahan.setText("Lahan: " + petani.lahan);
        holder.txtProgres.setText("Progres: " + petani.progres);
        holder.txtCatatan.setText("Catatan: " + petani.catatan);
        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = v.getContext();
            android.content.Intent intent = new android.content.Intent(context, com.example.contractfarmingapp.PerusahaanActivity.class);
            intent.putExtra("company_id", String.valueOf(petani.companyId));
            context.startActivity(intent);
        });
        // Status & tombol validasi
        String status = petani.status != null ? petani.status : "";

        holder.btnValidasi.setEnabled(true); // Reset sebelum diproses
        switch (status) {
            case "Kontrak diterima oleh perusahaan":
                holder.btnValidasi.setText("Bayar");
                holder.btnValidasi.setEnabled(true);
                holder.btnGantiRugi.setVisibility(View.VISIBLE);
                holder.btnGantiRugi.setText("Ajukan Ganti Rugi");
                holder.btnGantiRugi.setOnClickListener(v -> listener.onAjukanGantiRugiClick(petani));
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onKontrakSelesaiClick(petani));
                break;

            case "Kontrak selesai":
                holder.btnValidasi.setText("Berikan Ulasan");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;
            case "Review poktan selesai":
                holder.btnValidasi.setText("Berikan Ulasan");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;
            case "Review offtaker selesai":
                holder.btnValidasi.setText("Selesai");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Biru
                holder.btnValidasi.setOnClickListener(v -> listener.onBeriUlasan(petani));
                break;

            case "Memilih logistik":
                holder.btnValidasi.setText("Pilih Logistik");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3"))); // Biru
                holder.btnValidasi.setOnClickListener(v -> listener.onKontrakLogistikClick(petani));
                break;
            case "Menunggu persetujuan admin poktan":
                holder.btnValidasi.setText("Logistik Petani\n(Menunggu persetujuan admin poktan)");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFA500"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onKontrakLogistikPetani(petani));
                break;
            case "Pengiriman barang dilakukan":
                holder.btnValidasi.setText("Update Sopir");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onUpdateSopir(petani));
                break;
            case "Pengiriman barang ditolak admin poktan":
                holder.btnValidasi.setText("Ditolak admin poktan (Pilih Logistik Sendiri)");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Merah
                holder.btnValidasi.setOnClickListener(v -> listener.onKontrakLogistikClick(petani));
                break;
            case "Sedang dalam perjalanan":
                holder.btnValidasi.setText("Sedang dalam perjalanan");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onPesananSampai(petani));
                break;
            case "Pesanan sampai":
                holder.btnValidasi.setText("Bukti Pesananan Sampai");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Oranye-kuning
                holder.btnValidasi.setOnClickListener(v -> listener.onKontrakSampaiClick(petani));
                break;
            case "Perusahaan tidak memproses kontrak lebih lanjut":
                holder.btnValidasi.setText("Ditolak");
                holder.btnValidasi.setEnabled(false);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F44336"))); // Merah
                holder.btnValidasi.setOnClickListener(null);
                break;

            case "Kontrak divalidasi fasilitator":
            default:
                holder.btnValidasi.setText("Validasi Offtaker");
                holder.btnValidasi.setEnabled(true);
                holder.btnValidasi.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Hijau
                holder.btnValidasi.setOnClickListener(v -> listener.onValidasiClick(petani));
                break;
        }

        // Tombol lain selalu aktif
        holder.btnChat.setOnClickListener(v -> listener.onChatClick(petani));
        holder.btnLihatKontrak.setOnClickListener(v -> listener.onLihatKontrakClick(petani));
        holder.btnLihatLahan.setOnClickListener(v -> listener.onLihatLahanClick(petani));
    }

    @Override
    public int getItemCount() {
        return petaniList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtHarga, txtPerusahaan, txtLahan, txtProgres, txtCatatan;
        Button btnValidasi, btnChat, btnLihatKontrak, btnLihatLahan, btnGantiRugi; // <-- btn baru

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
            btnLihatLahan = itemView.findViewById(R.id.btnLihatLahan);
            btnGantiRugi = itemView.findViewById(R.id.btnGantiRugi); // <-- btn baru
        }
    }

}
