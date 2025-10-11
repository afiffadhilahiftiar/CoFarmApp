package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.ChatActivity;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.AnggotaModel;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AnggotaAdapter extends RecyclerView.Adapter<AnggotaAdapter.ViewHolder> {

    private final List<AnggotaModel> anggotaList;
    private final Context context;
    private final OnAnggotaDeleteListener deleteListener;
    private final String userPeran;

    // Interface untuk callback hapus
    public interface OnAnggotaDeleteListener {
        void onDeleteClick(AnggotaModel anggota, int position);
    }

    public AnggotaAdapter(Context context, List<AnggotaModel> anggotaList, String userPeran, OnAnggotaDeleteListener deleteListener) {
        this.context = context;
        this.anggotaList = anggotaList;
        this.userPeran = userPeran;
        this.deleteListener = deleteListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anggota, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnggotaModel anggota = anggotaList.get(position);

        holder.txtNama.setText(anggota.getNama());
        holder.txtPeran.setText(anggota.getPeran());

        String luasLahan = anggota.getAreaSize();
        holder.txtLuasLahan.setText((luasLahan == null || luasLahan.isEmpty())
                ? "Luas Lahan: -" : "Luas Lahan: " + luasLahan + " Ha");
        holder.txtLuasLahan.setVisibility(View.GONE);
        holder.txtJumlahKontrak.setText("Kontrak selesai: " + anggota.getJumlahKontrak());

        // Load foto profil
        if (anggota.getFotoProfile() != null && !anggota.getFotoProfile().isEmpty()) {
            Glide.with(context)
                    .load(anggota.getFotoProfile())
                    .placeholder(R.drawable.baseline_account_box_24)
                    .error(R.drawable.baseline_account_box_24)
                    .into(holder.imgFotoProfile);
        } else {
            holder.imgFotoProfile.setImageResource(R.drawable.baseline_account_box_24);
        }
        if (!"admin".equalsIgnoreCase(userPeran)) {
            holder.btnHapus.setVisibility(View.GONE); // Sembunyikan tombol hapus

        } else {
            holder.btnHapus.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiver_id", anggota.getId());   // id petani
                intent.putExtra("nama_petani", anggota.getNama()); // nama petani
                context.startActivity(intent);
            });
        }

        holder.btnHapus.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(anggota, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return anggotaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtPeran, txtLuasLahan, txtJumlahKontrak;
        ImageView btnHapus, imgFotoProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNamaAnggota);
            txtPeran = itemView.findViewById(R.id.txtPeranAnggota);
            txtLuasLahan = itemView.findViewById(R.id.txtLuasLahan);
            txtJumlahKontrak = itemView.findViewById(R.id.txtJumlahKontrak);
            btnHapus = itemView.findViewById(R.id.btnHapusAnggota);
            imgFotoProfile = itemView.findViewById(R.id.imgFotoProfile);
        }
    }
}
