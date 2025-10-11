package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.TokoCheckoutModel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.TokoViewHolder> {

    private final List<TokoCheckoutModel> tokoList;
    private final Context context;
    private final LogistikCallback logistikCallback;

    public interface LogistikCallback {
        void onPilihLogistik(int index, String namaToko);
    }

    public CheckoutAdapter(List<TokoCheckoutModel> tokoList, Context context, LogistikCallback callback) {
        this.tokoList = tokoList;
        this.context = context;
        this.logistikCallback = callback;
    }

    @NonNull
    @Override
    public TokoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_toko, parent, false);
        return new TokoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TokoViewHolder holder, int position) {
        TokoCheckoutModel toko = tokoList.get(position);
        holder.txtNamaToko.setText(toko.getNamaToko());

        ProdukCheckoutAdapter produkAdapter = new ProdukCheckoutAdapter(toko.getProdukList(), context);
        holder.recyclerProduk.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerProduk.setAdapter(produkAdapter);

        holder.btnVoucher.setOnClickListener(v -> {
            // Bisa tambahkan aksi voucher
        });

        holder.btnPengiriman.setOnClickListener(v -> {
            if (logistikCallback != null) {
                logistikCallback.onPilihLogistik(holder.getAdapterPosition(), toko.getNamaToko());
            }
        });

        if (toko.getKurir() != null && toko.getLayanan() != null) {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("in", "ID"));
            holder.txtInfoOngkir.setText("Kurir: " + toko.getKurir() + " (" + toko.getLayanan() + ") - Rp" + formatter.format(toko.getOngkir()));
        } else {
            holder.txtInfoOngkir.setText("Pilih Pengiriman");
        }
    }

    @Override
    public int getItemCount() {
        return tokoList.size();
    }

    public static class TokoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNamaToko, txtInfoOngkir;
        RecyclerView recyclerProduk;
        ImageButton btnVoucher, btnPengiriman;

        public TokoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaToko = itemView.findViewById(R.id.txtNamaToko);
            btnVoucher = itemView.findViewById(R.id.btnPilihVoucher);
            btnPengiriman = itemView.findViewById(R.id.btnPilihPengiriman);
            txtInfoOngkir = itemView.findViewById(R.id.txtOngkirInfo);
            recyclerProduk = itemView.findViewById(R.id.recyclerProduk);
        }
    }

    public void updateOngkir(int index, int ongkir, String kurir, String layanan, String etd) {
        if (index >= 0 && index < tokoList.size()) {
            TokoCheckoutModel toko = tokoList.get(index);
            toko.setOngkir(ongkir);
            toko.setKurir(kurir);
            toko.setLayanan(layanan);
            toko.setEtd(etd);
            notifyItemChanged(index);
        }
    }

    public int getTotalOngkir() {
        int total = 0;
        for (TokoCheckoutModel toko : tokoList) {
            total += toko.getOngkir();
        }
        return total;
    }
}
