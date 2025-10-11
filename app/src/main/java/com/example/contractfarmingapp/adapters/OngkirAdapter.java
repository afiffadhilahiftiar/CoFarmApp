package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.OngkirModel;

import java.util.List;

public class OngkirAdapter extends RecyclerView.Adapter<OngkirAdapter.ViewHolder> {

    private final List<OngkirModel> ongkirList;
    private final Context context;
    private final OnOngkirSelectedListener listener;

    public interface OnOngkirSelectedListener {
        void onOngkirSelected(OngkirModel selected);
    }

    public OngkirAdapter(List<OngkirModel> ongkirList, Context context, OnOngkirSelectedListener listener) {
        this.ongkirList = ongkirList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OngkirAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ongkir, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OngkirAdapter.ViewHolder holder, int position) {
        OngkirModel model = ongkirList.get(position);

        holder.txtKurir.setText(model.courierName);
        holder.txtLayanan.setText(model.serviceType);
        holder.txtOngkir.setText("Rp" + formatRupiah(model.estimatedCost));
        holder.txtEtd.setText("Estimasi: " + (model.estimatedDeliveryTime.isEmpty() ? "-" : model.estimatedDeliveryTime));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOngkirSelected(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ongkirList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtKurir, txtLayanan, txtOngkir, txtEtd;

        ViewHolder(View itemView) {
            super(itemView);
            txtKurir = itemView.findViewById(R.id.txtKurir);
            txtLayanan = itemView.findViewById(R.id.txtLayanan);
            txtOngkir = itemView.findViewById(R.id.txtOngkir);
            txtEtd = itemView.findViewById(R.id.txtEtd);
        }
    }

    private String formatRupiah(int number) {
        return String.format("%,d", number).replace(',', '.');
    }
}
