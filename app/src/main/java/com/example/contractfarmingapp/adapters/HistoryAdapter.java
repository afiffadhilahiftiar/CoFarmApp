package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.InvoiceHistory;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<InvoiceHistory> historyList;
    private OnItemClickListener listener;

    // Interface klik item
    public interface OnItemClickListener {
        void onItemClick(InvoiceHistory item);
    }

    // Setter untuk listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Konstruktor
    public HistoryAdapter(Context context, List<InvoiceHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtMethod, txtAmount, txtTimestamp, txtStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtId);
            txtMethod = itemView.findViewById(R.id.txtMethod);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtTimestamp = itemView.findViewById(R.id.txtTimestamp);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invoice_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        InvoiceHistory item = historyList.get(position);

        // Format amount ke rupiah
        String formattedAmount = "Rp " + NumberFormat.getNumberInstance(new Locale("id", "ID")).format(item.getAmount());

        // Tampilkan data
        holder.txtId.setText("ID: " + item.getId());
        holder.txtMethod.setText("Metode: " + item.getPaymentMethod());
        holder.txtAmount.setText("Jumlah: " + formattedAmount);
        holder.txtTimestamp.setText("Tanggal: " + item.getTimestamp());
        holder.txtStatus.setText("Status: " + item.getStatus());

        // Trigger listener saat item diklik
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
