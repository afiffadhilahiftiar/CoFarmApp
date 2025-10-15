package com.example.contractfarmingapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.SopirModel;
import com.example.contractfarmingapp.activities.SopirDetailActivity;
import com.example.contractfarmingapp.EditSopirActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SopirAdapter extends RecyclerView.Adapter<SopirAdapter.ViewHolder> {

    private List<SopirModel> sopirList;
    private Context context;
    private String companyId; // simpan companyId
    private String userPeran; // simpan companyId

    // constructor diperbaiki
    public SopirAdapter(Context context, List<SopirModel> sopirList, String companyId, String userPeran) {
        this.context = context;
        this.sopirList = sopirList;
        this.companyId = companyId;
        this.userPeran = userPeran;
    }

    @NonNull
    @Override
    public SopirAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_sopir, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SopirAdapter.ViewHolder holder, int position) {
        SopirModel sopir = sopirList.get(position);
        holder.tvNama.setText("Nama Sopir: " + sopir.getNama());
        holder.tvHp.setText("No HP: " + sopir.getNoHp());
        holder.tvKendaraan.setText("Kendaraan: " + sopir.getKendaraan());
        holder.tvPlat.setText("Plat Nomor: " + sopir.getPlatNomor());
        holder.tvLinkLokasi.setText("Lokasi Terkini: " + sopir.getLinkLokasi());
        holder.tvKapasitas.setText("Kapasitas: " + sopir.getKapasitas());
        if (!"admin".equalsIgnoreCase(userPeran)) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnHapus.setVisibility(View.GONE);// pakai View.GONE
            Toast.makeText(context, "Peran Anda: " + userPeran, Toast.LENGTH_SHORT).show();
        }
        // klik item ke detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SopirDetailActivity.class);
            intent.putExtra("id", sopir.getId());
            intent.putExtra("nama", sopir.getNama());
            intent.putExtra("no_hp", sopir.getNoHp());
            intent.putExtra("kendaraan", sopir.getKendaraan());
            intent.putExtra("plat_nomor", sopir.getPlatNomor());
            intent.putExtra("kapasitas", sopir.getKapasitas());
            intent.putExtra("foto_sopir", sopir.getFotoSopir());
            intent.putExtra("foto_kendaraan", sopir.getFotoKendaraan());
            intent.putExtra("foto_sim", sopir.getFotoSim());
            intent.putExtra("foto_stnk", sopir.getFotoStnk());
            intent.putExtra("link_lokasi", sopir.getLinkLokasi());
            intent.putExtra("company_id", companyId); // tambahkan
            context.startActivity(intent);
        });

        // tombol chat whatsapp
        holder.btnChat.setOnClickListener(v -> {
            String noHp = sopir.getNoHp().trim();

            if (noHp.startsWith("+")) {
                noHp = noHp.substring(1);
            } else if (noHp.startsWith("0")) {
                noHp = "62" + noHp.substring(1);
            }

            String url = "https://wa.me/" + noHp;

            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(android.net.Uri.parse(url));
                context.startActivity(i);
            } catch (Exception e) {
                Toast.makeText(context, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show();
            }
        });

        // tombol hapus sopir
        holder.btnHapus.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Apakah Anda yakin ingin menghapus sopir ini?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        hapusSopir(sopir.getId(), position);
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // tombol edit sopir
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditSopirActivity.class);
            intent.putExtra("id", sopir.getId());
            intent.putExtra("nama", sopir.getNama());
            intent.putExtra("no_hp", sopir.getNoHp());
            intent.putExtra("kendaraan", sopir.getKendaraan());
            intent.putExtra("plat_nomor", sopir.getPlatNomor());
            intent.putExtra("kapasitas", sopir.getKapasitas());
            intent.putExtra("foto_sopir", sopir.getFotoSopir());
            intent.putExtra("foto_kendaraan", sopir.getFotoKendaraan());
            intent.putExtra("foto_sim", sopir.getFotoSim());
            intent.putExtra("foto_stnk", sopir.getFotoStnk());
            intent.putExtra("company_id", companyId); // tambahkan
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sopirList.size();
    }

    private void hapusSopir(String id, int position) {
        String url = ApiConfig.BASE_URL + "delete_sopir.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(context, "Sopir berhasil dihapus", Toast.LENGTH_SHORT).show();
                    sopirList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, sopirList.size());
                },
                error -> Toast.makeText(context, "Gagal menghapus: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvHp, tvKendaraan, tvPlat, tvKapasitas, tvLinkLokasi;
        Button btnChat, btnEdit;
        ImageView btnHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.tvNamaSopir);
            tvHp = itemView.findViewById(R.id.tvNoHp);
            tvKendaraan = itemView.findViewById(R.id.tvKendaraan);
            tvPlat = itemView.findViewById(R.id.tvPlatNomor);
            tvLinkLokasi = itemView.findViewById(R.id.tvLinkLokasi);
            tvKapasitas = itemView.findViewById(R.id.tvKapasitas);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnHapus = itemView.findViewById(R.id.btnHapusSopir);
            btnEdit = itemView.findViewById(R.id.btnEditSopir);
        }
    }
}
