package com.example.contractfarmingapp.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.adapters.ProductImageAdapter;
import com.example.contractfarmingapp.models.Product;
import com.example.contractfarmingapp.models.Review;
import com.example.contractfarmingapp.models.Variation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends AppCompatActivity {

    private ViewPager viewPager;


    private LinearLayout layoutReview;
    private TextView txtProductName, txtStoreName, txtStoreLocation, txtProductPrice, txtOriginalPrice, txtSoldCount, txtRating, txtProductDescription;
    private RatingBar ratingBar;
    private Spinner spinnerVariasi;
    private Button btnAddToCart, btnBuyNow;
    private ImageView imgBack, imgStoreLogo;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private int baseProductPrice = 0;
    private int baseOriginalPrice = 0;
    private ProductImageAdapter pagerAdapter;

    private String formatRupiah(int number) {
        Locale localeID = new Locale("in", "ID");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(localeID);
        return "Rp" + numberFormat.format(number);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        int productId = getIntent().getIntExtra("product_id", -1);

        if (productId != -1) {
            // Load data produk berdasarkan productId
            loadProductData(productId);
        } else {
            // productId tidak ada atau default (-1), bisa tampilkan error atau behavior lain
            Toast.makeText(this, "Product ID tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
        // Inisialisasi view
        viewPager = findViewById(R.id.viewPager);
        layoutReview = findViewById(R.id.layoutReview);
        txtProductName = findViewById(R.id.txtProductName);
        txtStoreName = findViewById(R.id.txtStoreName);
        txtStoreLocation = findViewById(R.id.txtStoreLocation);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        txtSoldCount = findViewById(R.id.txtSoldCount);
        txtRating = findViewById(R.id.txtRating);
        ratingBar = findViewById(R.id.ratingBar);
        txtProductDescription = findViewById(R.id.txtProductDescription);
        spinnerVariasi = findViewById(R.id.spinnerVariasi);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnBuyNow = findViewById(R.id.btnBuyNow);
        imgBack = findViewById(R.id.btnBack);
        imgStoreLogo = findViewById(R.id.imgStoreLogo);

        // Coret harga asli
        txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Ambil productId dari intent


        // Tombol kembali
        imgBack.setOnClickListener(v -> finish());
        btnAddToCart.setOnClickListener(v -> {
            Variation selectedVariation = null;
            if (spinnerVariasi.getSelectedItem() instanceof Variation) {
                selectedVariation = (Variation) spinnerVariasi.getSelectedItem();
            }

            int basePrice = 0;
            try {
                basePrice = NumberFormat.getNumberInstance(new Locale("in", "ID"))
                        .parse(txtProductPrice.getText().toString().replace("Rp", "").trim()).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

            int totalPrice = basePrice;
            String variationId = (selectedVariation != null) ? selectedVariation.getId() : null;

            // Ambil email dari Firebase user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String userEmail = user.getEmail();  // Email dari Firebase
                addToCart(userEmail, productId, variationId, totalPrice);
            } else {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });


        btnBuyNow.setOnClickListener(v -> {
            Variation selectedVariation = null;
            if (spinnerVariasi.getSelectedItem() instanceof Variation) {
                selectedVariation = (Variation) spinnerVariasi.getSelectedItem();
            }

            int basePrice = 0;
            try {
                basePrice = NumberFormat.getNumberInstance(new Locale("in", "ID"))
                        .parse(txtProductPrice.getText().toString().replace("Rp", "").trim()).intValue();
            } catch (Exception e) {
                e.printStackTrace();
            }


            int totalPrice = basePrice;

            String selectedVarName = (selectedVariation != null) ? selectedVariation.getName() : "Default";

            Toast.makeText(this,
                    "Total Harga: " + formatRupiah(totalPrice) +
                            "\nBeli Sekarang:" + txtProductName.getText() +
                            "\nVariasi: " + selectedVarName,
                    Toast.LENGTH_LONG).show();

            // TODO: Tambahkan logika untuk proses pembelian
        });

    }
    private void addToCart(String userEmail, int productId, @Nullable String variationId, int totalPrice) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.27:8080/contractfarming/add_to_cart.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "user_email=" + userEmail +
                        "&product_id=" + productId +
                        "&variation_id=" + (variationId == null ? "" : variationId) +
                        "&total_price=" + totalPrice;

                conn.getOutputStream().write(params.getBytes());

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                runOnUiThread(() -> {
                    if (jsonResponse.optBoolean("success")) {
                        Toast.makeText(this, "Berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Gagal: " + jsonResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Kesalahan koneksi", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadProductData(int productId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.27:9090/contractfarming/get_product_detail_go?productId=" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONObject p = json.getJSONObject("product");

                Product product = new Product();
                product.setId(p.getString("id"));
                product.setName(p.getString("name"));
                product.setNamaperusahaan(p.getString("namaperusahaan"));
                product.setLokasiperusahaan(p.getString("lokasiperusahaan"));
                product.setLogoperusahaan(p.getString("logoperusahaan"));
                product.setPrice(formatRupiah(p.getInt("price")));
                product.setOriginalPrice(formatRupiah(p.getInt("original_price")));
                product.setRating((float) p.getDouble("rating"));
                product.setReviewCount(p.getInt("rating_count"));
                product.setDescription(p.getString("description"));
                product.setSoldCount(p.getInt("sold_count"));

                // Image URLs
                List<String> images = new ArrayList<>();
                JSONArray imgArr = p.getJSONArray("images");
                for (int i = 0; i < imgArr.length(); i++) {
                    images.add(imgArr.getString(i));
                }
                product.setImageUrls(images);
                product.setYoutubeVideoId(p.getString("youtube_video_id")); // dari JSON

                // Variasi produk
                List<Variation> variations = new ArrayList<>();
                if (p.has("variations")) {
                    JSONArray varArr = p.getJSONArray("variations");
                    for (int i = 0; i < varArr.length(); i++) {
                        JSONObject v = varArr.getJSONObject(i);
                        String varId = v.getString("id");
                        String varName = v.getString("name");
                        int priceDiff = v.getInt("price_difference");

                        variations.add(new Variation(varId, varName, priceDiff));
                    }
                }
                product.setVariations(variations);

                // Reviews
                List<Review> reviews = new ArrayList<>();
                JSONArray reviewArray = json.getJSONArray("reviews");
                for (int i = 0; i < reviewArray.length(); i++) {
                    JSONObject r = reviewArray.getJSONObject(i);
                    Review review = new Review();
                    review.setReviewerName(r.getString("name"));
                    review.setRating((float) r.getDouble("rating"));
                    review.setComment(r.getString("comment"));
                    reviews.add(review);
                }

                runOnUiThread(() -> {
                    bindProduct(product);
                    displayReviews(reviews);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ProductActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void bindProduct(Product product) {
        txtProductName.setText(product.getName());
        txtStoreName.setText(product.getNamaperusahaan());
        txtStoreLocation.setText(product.getLokasiperusahaan()); // lokasi perusahaan
        Glide.with(this)
                .load(product.getLogoperusahaan())
                .placeholder(R.drawable.kotakabu) // gunakan icon placeholder sementara
                .into(imgStoreLogo);
        // Simpan base price
        try {
            baseProductPrice = NumberFormat.getNumberInstance(new Locale("in", "ID"))
                    .parse(product.getPrice().replace("Rp", "").trim()).intValue();
            baseOriginalPrice = NumberFormat.getNumberInstance(new Locale("in", "ID"))
                    .parse(product.getOriginalPrice().replace("Rp", "").trim()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        txtProductPrice.setText(formatRupiah(baseProductPrice));
        txtOriginalPrice.setText(formatRupiah(baseOriginalPrice));
        txtSoldCount.setText(product.getSoldCount() + " terjual");
        txtProductDescription.setText(product.getDescription());
        ratingBar.setRating(product.getRating());
        txtRating.setText(product.getRating() + " dari " + product.getReviewCount() + " ulasan");

        pagerAdapter = new ProductImageAdapter(this, product.getImageUrls());
        viewPager.setAdapter(pagerAdapter);
        startAutoSlide();

        // Tampilkan variasi ke Spinner
        if (product.getVariations() != null && !product.getVariations().isEmpty()) {
            ArrayAdapter<Variation> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, product.getVariations());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerVariasi.setAdapter(adapter);
            spinnerVariasi.setVisibility(View.VISIBLE);

            // Listener untuk mengubah harga secara real-time saat variasi dipilih
            spinnerVariasi.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    Variation selectedVariation = (Variation) parent.getItemAtPosition(position);
                    int priceDiff = selectedVariation.getPriceDifference();

                    txtProductPrice.setText(formatRupiah(baseProductPrice + priceDiff));
                    txtOriginalPrice.setText(formatRupiah(baseOriginalPrice + priceDiff));
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    txtProductPrice.setText(formatRupiah(baseProductPrice));
                    txtOriginalPrice.setText(formatRupiah(baseOriginalPrice));
                }
            });

        } else {
            spinnerVariasi.setVisibility(View.GONE);
        }


    }

    private void displayReviews(List<Review> reviews) {
        layoutReview.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (Review review : reviews) {
            View view = inflater.inflate(R.layout.item_ulasan, layoutReview, false);
            TextView txtReviewerName = view.findViewById(R.id.tvNamaPerusahaan);
            RatingBar rbReview = view.findViewById(R.id.ratingBar);
            TextView txtReviewText = view.findViewById(R.id.tvUlasan);

            txtReviewerName.setText(review.getReviewerName());
            rbReview.setRating(review.getRating());
            txtReviewText.setText(review.getComment());

            layoutReview.addView(view);
        }
    }

    private void startAutoSlide() {
        final int NUM_PAGES = pagerAdapter.getCount();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(runnable, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);
        viewPager.setOnTouchListener((v, event) -> {
            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }
            return false;
        });
    }

    @Override
    protected void onDestroy() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();
    }
}