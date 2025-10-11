package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.models.ProdukCheckoutModel;

public class ProdukCheckoutAdapter extends RecyclerView.Adapter<ProdukCheckoutAdapter.ProdukViewHolder> {

    private List<ProdukCheckoutModel> produkList;
    private Context context;

    public ProdukCheckoutAdapter(List<ProdukCheckoutModel> produkList, Context context) {
        this.produkList = produkList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProdukViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_produk, parent, false);
        return new ProdukViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdukViewHolder holder, int position) {
        ProdukCheckoutModel produk = produkList.get(position);

        holder.txtNamaProduk.setText(produk.getNamaProduk());
        holder.txtVariasiProduk.setText(produk.getVariasi());
        holder.txtJumlah.setText("x" + produk.getJumlah());

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String hargaFormatted = formatRupiah.format(produk.getHarga()).replace(",00", "");
        holder.txtHarga.setText(hargaFormatted);
    }

    @Override
    public int getItemCount() {
        return produkList.size();
    }

    public static class ProdukViewHolder extends RecyclerView.ViewHolder {
        TextView txtNamaProduk, txtVariasiProduk, txtJumlah, txtHarga;

        public ProdukViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNamaProduk = itemView.findViewById(R.id.txtNamaProduk);
            txtVariasiProduk = itemView.findViewById(R.id.txtVariasiProduk);
            txtJumlah = itemView.findViewById(R.id.txtJumlah);
            txtHarga = itemView.findViewById(R.id.txtHarga);
        }
    }
}
