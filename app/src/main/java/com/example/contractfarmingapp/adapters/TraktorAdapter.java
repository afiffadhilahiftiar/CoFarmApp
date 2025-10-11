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
import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.EditTraktorActivity;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.activities.TraktorDetailActivity;
import com.example.contractfarmingapp.models.TraktorModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraktorAdapter extends RecyclerView.Adapter<TraktorAdapter.ViewHolder> {

    private List<TraktorModel> traktorList;
    private Context context;
    private String companyId;
    private String userPeran;

    public TraktorAdapter(Context context, List<TraktorModel> traktorList, String companyId, String userPeran) {
        this.context = context;
        this.traktorList = traktorList;
        this.companyId = companyId;
        this.userPeran = userPeran;
    }

    @NonNull
    @Override
    public TraktorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_traktor, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TraktorAdapter.ViewHolder holder, int position) {
        TraktorModel traktor = traktorList.get(position);

        holder.tvJenis.setText("Jenis Traktor: " + traktor.getJenisTraktor());
        holder.tvKapasitas.setText("Kapasitas: " + traktor.getKapasitas() + " HP");
        holder.tvOperator.setText("Operator: " + traktor.getNamaOperator());
        holder.tvHp.setText("No HP: " + traktor.getNoHp());

        if (!"admin".equalsIgnoreCase(userPeran)) {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnHapus.setVisibility(View.GONE);
        }

        // klik item ke detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TraktorDetailActivity.class);
            intent.putExtra("id", traktor.getId());
            intent.putExtra("jenis_traktor", traktor.getJenisTraktor());
            intent.putExtra("kapasitas", traktor.getKapasitas());
            intent.putExtra("nama_operator", traktor.getNamaOperator());
            intent.putExtra("no_hp", traktor.getNoHp());
            intent.putExtra("foto_traktor", traktor.getFotoTraktor());
            intent.putExtra("company_id", companyId);
            context.startActivity(intent);
        });

        // tombol edit traktor
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditTraktorActivity.class);
            intent.putExtra("id", traktor.getId());
            intent.putExtra("jenis_traktor", traktor.getJenisTraktor());
            intent.putExtra("kapasitas", traktor.getKapasitas());
            intent.putExtra("nama_operator", traktor.getNamaOperator());
            intent.putExtra("no_hp", traktor.getNoHp());
            intent.putExtra("foto_traktor", traktor.getFotoTraktor());
            intent.putExtra("company_id", companyId);
            context.startActivity(intent);
        });
        holder.btnChat.setOnClickListener(v -> {
            String noHp = traktor.getNoHp().trim();

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

        // tombol hapus traktor
        holder.btnHapus.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Apakah Anda yakin ingin menghapus traktor ini?")
                    .setPositiveButton("Ya", (dialog, which) -> hapusTraktor(traktor.getId(), position))
                    .setNegativeButton("Batal", null)
                    .show();
        });

        // tampilkan foto traktor jika ada
        if (traktor.getFotoTraktor() != null && !traktor.getFotoTraktor().isEmpty()) {
            Glide.with(context)
                    .load(traktor.getFotoTraktor())
                    .into(holder.ivFoto);
        }
    }

    @Override
    public int getItemCount() {
        return traktorList.size();
    }

    private void hapusTraktor(String id, int position) {
        String url = ApiConfig.BASE_URL + "delete_traktor.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(context, "Traktor berhasil dihapus", Toast.LENGTH_SHORT).show();
                    traktorList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, traktorList.size());
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
        TextView tvJenis, tvKapasitas, tvOperator, tvHp;
        Button btnEdit, btnChat;
        ImageView btnHapus, ivFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJenis = itemView.findViewById(R.id.tvJenisTraktor);
            tvKapasitas = itemView.findViewById(R.id.tvKapasitas);
            tvOperator = itemView.findViewById(R.id.tvNamaOperator);
            tvHp = itemView.findViewById(R.id.tvNoHp);
            btnEdit = itemView.findViewById(R.id.btnEditTraktor);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnHapus = itemView.findViewById(R.id.btnHapusTraktor);
            ivFoto = itemView.findViewById(R.id.ivFotoTraktor);
        }
    }
}
