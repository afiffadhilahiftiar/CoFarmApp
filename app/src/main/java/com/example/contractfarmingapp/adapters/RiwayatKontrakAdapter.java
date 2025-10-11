package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.RiwayatKontrakModel;

import java.util.List;

public class RiwayatKontrakAdapter extends RecyclerView.Adapter<RiwayatKontrakAdapter.ViewHolder> {

    private Context context;
    private List<RiwayatKontrakModel> list;

    public RiwayatKontrakAdapter(Context context, List<RiwayatKontrakModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RiwayatKontrakAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_riwayat_kontrak, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatKontrakAdapter.ViewHolder holder, int position) {
        RiwayatKontrakModel data = list.get(position);
        holder.tvKebutuhan.setText(data.getKebutuhan());
        holder.tvJumlahKebutuhan.setText(data.getJumlahKebutuhan() + " " + data.getSatuan());
        holder.tvPerusahaan.setText(data.getNamaPerusahaan());
        holder.tvWaktuDibutuhkan.setText("Waktu Dibutuhkan: " + data.getWaktuDibutuhkan());
        holder.tvLahan.setText(data.getLahan());
        holder.tvStatusLahan.setText("Status lahan saat ini: " + data.getStatuslahan());
        holder.tvStatus.setText("Status: " + data.getStatus());
        holder.tvCatatan.setText("Catatan: " + data.getCatatan());
        holder.tvTanggal.setText("Diajukan: " + data.getTanggal());

        // Tombol lihat kontrak
        holder.btnLihatKontrak.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLihatKontrakClick(data);
            }
        });

        // Tombol cetak
        holder.btnCetak.setOnClickListener(v -> {
            if (cetakClickListener != null) {
                cetakClickListener.onCetakClick(data);
            }
        });

        // Tombol upload bukti
        holder.btnUploadBukti.setOnClickListener(v -> {
            if (uploadClickListener != null) {
                uploadClickListener.onUploadClick(data);
            }
        });

        // Tombol chat
        holder.btnChat.setOnClickListener(v -> {
            if (chatClickListener != null) {
                chatClickListener.onChatClick(data);
            }
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKebutuhan, tvJumlahKebutuhan, tvPerusahaan, tvWaktuDibutuhkan, tvLahan, tvStatusLahan, tvCatatan, tvStatus, tvTanggal;
        Button btnLihatKontrak, btnCetak, btnUploadBukti, btnChat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKebutuhan = itemView.findViewById(R.id.tvKebutuhan);
            tvJumlahKebutuhan = itemView.findViewById(R.id.tvJumlahKebutuhan);
            tvPerusahaan = itemView.findViewById(R.id.tvPerusahaan);
            tvWaktuDibutuhkan = itemView.findViewById(R.id.tvWaktuDibutuhkan);
            tvLahan = itemView.findViewById(R.id.tvLahan);
            tvStatusLahan = itemView.findViewById(R.id.tvStatusLahan);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCatatan = itemView.findViewById(R.id.tvCatatan);
            tvTanggal = itemView.findViewById(R.id.tvTanggal);
            btnLihatKontrak = itemView.findViewById(R.id.btnLihatKontrak);
            btnCetak = itemView.findViewById(R.id.btnCetak);
            btnChat = itemView.findViewById(R.id.btnChat);
            btnUploadBukti = itemView.findViewById(R.id.btnUploadBukti);
        }
    }

    // Listener lihat kontrak
    public interface OnRiwayatKontrakClickListener {
        void onLihatKontrakClick(RiwayatKontrakModel data);
    }
    private OnRiwayatKontrakClickListener listener;
    public void setOnRiwayatKontrakClickListener(OnRiwayatKontrakClickListener listener) {
        this.listener = listener;
    }

    // Listener cetak
    public interface OnCetakClickListener {
        void onCetakClick(RiwayatKontrakModel data);
    }
    private OnCetakClickListener cetakClickListener;
    public void setOnCetakClickListener(OnCetakClickListener listener) {
        this.cetakClickListener = listener;
    }

    // Listener upload bukti
    public interface OnUploadBuktiClickListener {
        void onUploadClick(RiwayatKontrakModel data);
    }
    private OnUploadBuktiClickListener uploadClickListener;
    public void setOnUploadBuktiClickListener(OnUploadBuktiClickListener listener) {
        this.uploadClickListener = listener;
    }

    // Listener chat
    public interface OnChatClickListener {
        void onChatClick(RiwayatKontrakModel data);
    }
    private OnChatClickListener chatClickListener;
    public void setOnChatClickListener(OnChatClickListener listener) {
        this.chatClickListener = listener;
    }
}
