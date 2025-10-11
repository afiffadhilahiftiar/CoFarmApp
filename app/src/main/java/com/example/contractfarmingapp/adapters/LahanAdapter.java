package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.LahanModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LahanAdapter extends RecyclerView.Adapter<LahanAdapter.ViewHolder> {

    private final Context context;
    private final List<LahanModel> lahanList;
    private final String peran;
    public interface OnLahanDeleteListener {
        void onDelete(LahanModel model);
    }

    private final OnLahanDeleteListener deleteListener;


    public LahanAdapter(Context context, List<LahanModel> lahanList, String peran, OnLahanDeleteListener deleteListener) {
        this.context = context;
        this.lahanList = lahanList;
        this.peran = peran;
        this.deleteListener = deleteListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lahan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LahanModel lahan = lahanList.get(position);
        holder.txtNama.setText(lahan.getNama());
        holder.txtPerusahaan.setText("Perusahaan: " + lahan.getPerusahaan());
        holder.txtEmail.setText("Email: " + lahan.getEmail());
        holder.txtKomoditas.setText("Komoditas: " + lahan.getKomoditas());
        holder.txtLuas.setText("Luas Area Tanam: " + lahan.getLuas() + " Ha");
        holder.txtLokasi.setText("Lokasi: " + lahan.getLokasi());


        String status = lahan.getStatus();
        holder.txtStatus.setText("Status: " + status);

        // Cek peran untuk tombol hapus
        if ("admin".equalsIgnoreCase(peran)) {
            holder.btnHapus.setVisibility(View.VISIBLE);
            holder.btnHapus.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDelete(lahan);
                }
            });
        } else {
            holder.btnHapus.setVisibility(View.GONE);
        }

        // Atur warna status
        switch (status.toLowerCase()) {
            case "pengolahan lahan":
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_orange);
                break;
            case "penanaman":
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_green);
                break;
            case "perawatan":
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_blue);
                break;
            case "panen":
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_gold);
                break;
            case "tidak aktif":
            default:
                holder.txtStatus.setBackgroundResource(R.drawable.bg_status_gray);
                break;
        }
    }



    @Override
    public int getItemCount() {
        return lahanList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtEmail,txtPerusahaan, txtStatus, txtKomoditas, txtLuas, txtLokasi;
        View btnHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNamaLahan);
            txtPerusahaan = itemView.findViewById(R.id.txtPerusahaan);
            txtEmail = itemView.findViewById(R.id.txtEmailLahan);
            txtStatus = itemView.findViewById(R.id.txtStatusLahan);
            txtKomoditas = itemView.findViewById(R.id.txtKomoditasLahan);
            txtLuas = itemView.findViewById(R.id.txtLuasLahan);
            txtLokasi = itemView.findViewById(R.id.txtLokasiLahan);
            btnHapus = itemView.findViewById(R.id.btnHapus); // tambahkan ini
        }

    }
}
