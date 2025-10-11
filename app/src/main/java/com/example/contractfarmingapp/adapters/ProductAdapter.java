package com.example.contractfarmingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.activities.ProductActivity;
import com.example.contractfarmingapp.models.ProductModel;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<ProductModel> productList;

    public ProductAdapter(Context context, List<ProductModel> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModel product = productList.get(position);

        holder.textName.setText(product.getName());

        NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String priceFormatted = formatRupiah.format(product.getPrice());
        holder.textPrice.setText(priceFormatted);

        holder.textSoldCount.setText("Terjual: " + product.getSoldCount());
        holder.textRating.setText("⭐ " + String.valueOf(product.getRating()));

        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(1000)
                .setBaseAlpha(0.9f)
                .setHighlightAlpha(1.0f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build();


        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(shimmerDrawable)
                .into(holder.imageProduct);


        // ⬇️ Klik item produk
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductActivity.class);
            intent.putExtra("product_id", product.getId()); // Mengirim ID produk ke ProductActivity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView textName, textPrice, textSoldCount, textRating;
        ImageView imageProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textNamaProduk);
            textPrice = itemView.findViewById(R.id.textHargaProduk);
            textSoldCount = itemView.findViewById(R.id.textTerjual);
            textRating = itemView.findViewById(R.id.textRating);
            imageProduct = itemView.findViewById(R.id.imageProduct);
        }
    }
}
