package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.adapters.ContractAdapter;
import com.example.contractfarmingapp.adapters.UlasanAdapter;
import com.example.contractfarmingapp.models.ContractModel;
import com.example.contractfarmingapp.models.UlasanModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PerusahaanActivity extends AppCompatActivity {

    private ImageView companyLogo, icVerified;
    private TextView companyName, companyJenis, companyId, companyDescription,
            companyWebsite, companyLocation, companyKoordinat, companyCertificate,
            companySocialMedia, companyRating, employeeCount, logistikCount,
            tractorCount, luasLahan, companyTerbayar;
    private TextView companySkNumber;
    private Button btnPreviewSk, btnChat;
    private Handler autoScrollHandler = new Handler();
    private int currentPosition = 0;

    private ImageButton btnDaftarKaryawan, btnLahan, btnDaftarTraktor, btnDaftarLogistik;
    private LinearLayout layoutDaftarKaryawan, layoutLahan, layoutDaftarLogistik, layoutDaftarTraktor;

    private ProgressDialog progressDialog;
    private List<ContractModel> contractList = new ArrayList<>();
    private ContractAdapter contractAdapter;
    private RecyclerView rvUlasan;
    private UlasanAdapter ulasanAdapter;
    private List<UlasanModel> ulasanList = new ArrayList<>();

    private String companyIdValue;
    private String currentCompanyName = "";
    private String currentPeran = "user"; // default role

    private static final String URL_PROFILE = ApiConfig.BASE_URL + "get_profil_company_by_id.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perusahaan);

        // Ambil company_id dari Intent
        companyIdValue = getIntent().getStringExtra("company_id");
        if (companyIdValue == null || companyIdValue.isEmpty()) {
            Toast.makeText(this, "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi View
        companyLogo = findViewById(R.id.companyLogo);
        companyName = findViewById(R.id.companyName);
        companyJenis = findViewById(R.id.companyJenis);
        companySkNumber = findViewById(R.id.companyNomorSk);
        btnPreviewSk = findViewById(R.id.btnPreviewSk);
        companyId = findViewById(R.id.companyId);
        companyDescription = findViewById(R.id.companyDescription);
        companyWebsite = findViewById(R.id.companyWebsite);
        companyLocation = findViewById(R.id.companyLocation);
        companyTerbayar = findViewById(R.id.tvTerbayar);

        companyKoordinat = findViewById(R.id.companyKoordinat);
        companyCertificate = findViewById(R.id.companyCertificate);
        companySocialMedia = findViewById(R.id.companySocialMedia);
        companyRating = findViewById(R.id.companyRating);
        employeeCount = findViewById(R.id.employeeCount);
        logistikCount = findViewById(R.id.logistikCount);
        tractorCount = findViewById(R.id.tractorCount);
        luasLahan = findViewById(R.id.luasLahan);
        icVerified = findViewById(R.id.icVerified);
        layoutDaftarKaryawan = findViewById(R.id.layoutDaftarKaryawan);
        layoutDaftarTraktor = findViewById(R.id.layoutDaftarTraktor);
        layoutDaftarLogistik = findViewById(R.id.layoutDaftarLogistik);
        layoutLahan = findViewById(R.id.layoutLahan);

        btnLahan = findViewById(R.id.btnLahan);
        btnDaftarKaryawan = findViewById(R.id.btnDaftarKaryawan);
        btnDaftarTraktor = findViewById(R.id.btnDaftarTraktor);
        btnDaftarLogistik = findViewById(R.id.btnDaftarLogistik);
        rvUlasan = findViewById(R.id.rvUlasan);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvUlasan.setLayoutManager(layoutManager);

        ulasanAdapter = new UlasanAdapter(ulasanList);
        rvUlasan.setAdapter(ulasanAdapter);

// Auto-scroll
        autoScrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ulasanList.size() > 0) {
                    if (currentPosition == ulasanList.size()) {
                        currentPosition = 0; // reset ke awal
                    }
                    rvUlasan.smoothScrollToPosition(currentPosition++);
                    autoScrollHandler.postDelayed(this, 3000); // scroll setiap 3 detik
                }
            }
        }, 3000);
        // Event tombol daftar karyawan
        btnDaftarKaryawan.setOnClickListener(v -> {
            Intent intent = new Intent(PerusahaanActivity.this, DaftarAnggotaActivity.class);
            intent.putExtra("peran", "user");
            intent.putExtra("company_id", companyIdValue);
            startActivity(intent);
        });

        // Event tombol daftar logistik
        btnDaftarLogistik.setOnClickListener(v -> {
            Intent intent = new Intent(PerusahaanActivity.this, DaftarSopirActivity.class);
            intent.putExtra("peran", currentPeran);
            intent.putExtra("company_id", companyIdValue);
            startActivity(intent);
        });

        // Event tombol daftar traktor
        btnDaftarTraktor.setOnClickListener(v -> {
            Intent intent = new Intent(PerusahaanActivity.this, DaftarTraktorActivity.class);
            intent.putExtra("peran", currentPeran);
            intent.putExtra("company_id", companyIdValue);
            startActivity(intent);
        });

        // Event tombol lahan
        btnLahan.setOnClickListener(v -> {
            Intent intent = new Intent(PerusahaanActivity.this, LahanActivityUser.class);
            intent.putExtra("peran", currentPeran);
            intent.putExtra("company_id", companyIdValue);
            intent.putExtra("company_name", currentCompanyName);
            startActivity(intent);
        });
        btnChat = findViewById(R.id.btnChat);
        btnChat.setText("Chat");
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(PerusahaanActivity.this, ChatActivityPetaniOfftaker.class);
            intent.putExtra("oftaker_id", companyIdValue);  // kirim company_id sebagai oftaker_id
            intent.putExtra("nama_perusahaan", currentCompanyName); // kirim nama perusahaan
            startActivity(intent);
        });


        // Load data perusahaan
        loadCompanyProfile();
        loadUlasan();
    }

    private void loadCompanyProfile() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat profil perusahaan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject params = new JSONObject();
        try {
            params.put("company_id", companyIdValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_PROFILE, params,
                response -> {
                    progressDialog.dismiss();
                    try {
                        currentCompanyName = response.getString("company_name");

                        companyName.setText(currentCompanyName);
                        String certificate = response.optString("certificate", "");
                        if (certificate != null && !certificate.isEmpty()) {
                            icVerified.setVisibility(ImageView.VISIBLE);
                        } else {
                            icVerified.setVisibility(ImageView.GONE);
                        }
                        companyJenis.setText(response.getString("jenis_usaha"));
                        companyId.setText("ID: " + response.getString("company_id"));
                        companyDescription.setText(response.getString("description"));
                        companyWebsite.setText("🌐 " + response.getString("website"));
                        companyLocation.setText("🗺️ " + response.getString("location"));
                        companyKoordinat.setText("📍 " + response.getString("coordinate"));
                        companyCertificate.setText("📄 Sertifikat: " + response.getString("certificate"));
                        companySocialMedia.setText("📱 Instagram: " + response.getString("social_media"));
                        companyRating.setText("⭐ Rating: " + response.optString("company_rating", "Belum ada rating"));
                        // Ambil jumlah kontrak terbayar
                        String kontrakTerbayar = response.optString("contracts_paid", "0");
                        companyTerbayar.setText("   |   Terbayar: " + kontrakTerbayar + " Kontrak");
                        employeeCount.setText("👥 Jumlah Anggota: " + response.getString("employee_count"));
                        logistikCount.setText("🚚 Jumlah Logistik: " + response.getString("logistik_count"));
                        tractorCount.setText("🚜 Jumlah Traktor: " + response.getString("tractor_count"));
                        luasLahan.setText("🏞️ Luas Lahan: " + response.getString("land_area"));

                        String websiteUrl = response.getString("website");
                        companyWebsite.setOnClickListener(v -> {
                            if (websiteUrl != null && !websiteUrl.isEmpty()) {
                                String finalUrl = websiteUrl.startsWith("http") ? websiteUrl : "http://" + websiteUrl;
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl));
                                startActivity(browserIntent);
                            }
                        });
                        companyWebsite.setTextColor(Color.parseColor("#0D47A1"));
                        companyWebsite.setPaintFlags(companyWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                        String logoUrl = response.getString("logo_url");
                        Glide.with(this)
                                .load(logoUrl)
                                .placeholder(R.drawable.store_icon)
                                .error(R.drawable.store_icon)
                                .into(companyLogo);
                        String nomorSk = response.optString("nomor_sk", "-");
                        String skUrl = response.optString("sk_perusahaan", ""); // url file SK (pdf/jpg)

                        companySkNumber.setText("📑 Nomor SK: " + nomorSk);

                        // Event tombol preview SK
                        btnPreviewSk.setOnClickListener(v -> {
                            if (skUrl != null && !skUrl.isEmpty()) {
                                try {
                                    // Paksa buka browser, jangan tersangkut deep link app
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(skUrl));
                                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(browserIntent);
                                } catch (Exception e) {
                                    Toast.makeText(this, "Tidak dapat membuka dokumen", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(this, "Dokumen SK belum tersedia", Toast.LENGTH_SHORT).show();
                            }
                        });


                        // Tampilkan/hidden layout berdasarkan jenis
                        String jenis = response.getString("jenis_usaha");
                        if ("Kelompok Tani".equalsIgnoreCase(jenis)) {
                            layoutDaftarKaryawan.setVisibility(LinearLayout.VISIBLE);
                            layoutLahan.setVisibility(LinearLayout.VISIBLE);
                            layoutDaftarTraktor.setVisibility(LinearLayout.VISIBLE);
                            layoutDaftarLogistik.setVisibility(LinearLayout.VISIBLE);
                            employeeCount.setVisibility(View.VISIBLE);
                            tractorCount.setVisibility(View.VISIBLE);
                            luasLahan.setVisibility(View.VISIBLE);
                            companyTerbayar.setVisibility(View.GONE);

                        } else {
                            layoutDaftarKaryawan.setVisibility(LinearLayout.GONE);
                            layoutLahan.setVisibility(LinearLayout.GONE);
                            layoutDaftarTraktor.setVisibility(LinearLayout.GONE);
                            layoutDaftarLogistik.setVisibility(LinearLayout.GONE);
                            employeeCount.setVisibility(View.GONE);
                            tractorCount.setVisibility(View.GONE);
                            luasLahan.setVisibility(View.GONE);

                        }
                        if ("Fasilitator".equalsIgnoreCase(jenis)) {
                            layoutDaftarKaryawan.setVisibility(View.VISIBLE);
                            layoutLahan.setVisibility(View.VISIBLE);
                            layoutDaftarTraktor.setVisibility(View.VISIBLE);
                            employeeCount.setVisibility(View.VISIBLE);
                            tractorCount.setVisibility(View.GONE);
                            logistikCount.setVisibility(View.GONE);
                            companyRating.setVisibility(View.GONE);
                            companyTerbayar.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal parsing data perusahaan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal mengambil data perusahaan", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }
    private void loadUlasan() {
        String url = ApiConfig.BASE_URL + "get_ulasan_by_company.php?company_id=" + companyIdValue;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ulasanList.clear();
                        JSONArray arr = response.getJSONArray("ulasan");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String nama = obj.getString("nama_perusahaan");
                            String isi = obj.getString("ulasan");
                            float rating = (float) obj.getDouble("rating");
                            ulasanList.add(new UlasanModel(nama, isi, rating));
                        }
                        ulasanAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Gagal parsing ulasan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal mengambil ulasan", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoScrollHandler.removeCallbacksAndMessages(null);
    }

}

