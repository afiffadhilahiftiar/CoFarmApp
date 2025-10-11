package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.adapters.CartAdapter;
import com.example.contractfarmingapp.models.CartItem;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private OkHttpClient client;
    private TextView txtTotal;
    private Button btnCheckout;

    private static final String API_URL_LOAD_CART = "http://192.168.1.27:8080/contractfarming/load_cart.php?user_email=apipvaxs@gmail.com";
    private static final String API_URL_DELETE_CART = "http://192.168.1.27:8080/contractfarming/delete_cart.php?id=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerCart);
        progressBar = findViewById(R.id.progressBar);
        txtTotal = findViewById(R.id.textTotal);
        btnCheckout = findViewById(R.id.btnCheckout);



        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartList = new ArrayList<>();
        client = new OkHttpClient();

        fetchCartItems();
    }
    private String formatRupiah(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        String result = format.format(amount);
        return result.replace("Rp", "Rp").replace(",00", "").replace("\u00A0", "");
    }

    private void fetchCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        Request request = new Request.Builder().url(API_URL_LOAD_CART).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CartActivity.this, "Gagal memuat keranjang", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        JSONArray cartArray = jsonObject.getJSONArray("cart");

                        cartList.clear();

                        for (int i = 0; i < cartArray.length(); i++) {
                            JSONObject item = cartArray.getJSONObject(i);

                            CartItem cartItem = new CartItem(
                                    item.getInt("id"),
                                    item.getString("userEmail"),
                                    item.getInt("productId"),
                                    item.getInt("variationId"),
                                    item.getInt("quantity"),
                                    item.getDouble("totalPrice"),
                                    item.getDouble("pricePerUnit"),
                                    item.getString("addedAt"),
                                    item.getString("namaProduk"),
                                    item.getString("namaPerusahaan"),
                                    item.getString("namaVariasi"),
                                    item.getString("logoProduk")
                            );

                            cartList.add(cartItem);
                        }

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            if (adapter == null) {
                                adapter = new CartAdapter(CartActivity.this, cartList);
                                recyclerView.setAdapter(adapter);

                                // Set callback hapus item
                                adapter.setOnDeleteClickListener((position, item) -> {
                                    hapusItemCart(item.getId(), position);
                                });

                                // Set callback saat checkbox dicentang/dihapus centangnya
                                adapter.setOnItemCheckedChangeListener((selectedItems, totalPrice) -> {
                                    txtTotal.setText("Total: " + formatRupiah(totalPrice));
                                    // Tambahkan logika enable/disable tombol checkout jika diperlukan
                                });
                        } else {
                                adapter.notifyDataSetChanged();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(CartActivity.this, "Terjadi kesalahan parsing data", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CartActivity.this, "Gagal memuat keranjang", Toast.LENGTH_SHORT).show();
                    });
                    Log.e("CartActivity", "Response not successful");
                }
            }
        });
    }

    private void hapusItemCart(int cartItemId, int position) {
        progressBar.setVisibility(View.VISIBLE);
        String url = API_URL_DELETE_CART + cartItemId;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CartActivity.this, "Gagal menghapus item", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                if (response.isSuccessful()) {
                    try {
                        String resp = response.body().string();
                        JSONObject jsonObject = new JSONObject(resp);
                        boolean success = jsonObject.optBoolean("success", false);

                        runOnUiThread(() -> {
                            if (success) {
                                cartList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(CartActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CartActivity.this, "Gagal menghapus item", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CartActivity.this, "Terjadi kesalahan saat menghapus item", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(CartActivity.this, "Gagal menghapus item", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
