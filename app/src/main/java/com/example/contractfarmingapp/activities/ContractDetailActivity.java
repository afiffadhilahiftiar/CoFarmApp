package com.example.contractfarmingapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.PemberkasanActivity;
import com.example.contractfarmingapp.PerusahaanActivity;
import com.example.contractfarmingapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ContractDetailActivity extends AppCompatActivity {

    TextView tvOftakerId, tvHeaderTitle, tvKebutuhan, tvPerusahaan, tvNamaPerusahaan, tvJumlahKebutuhan, tvRating, tvLokasi, tvHargaPerKg,
            tvWaktuDibutuhkan, tvJumlahKontrak, tvPersyaratan, tvDeskripsi, tvTimeUpload, tvTerbayar, tvRiskScore, tvRiskRecommendation, tvGradeB, tvGradeC, tvHargaA, tvHargaB, tvHargaC;
    TextView tvRiskScoreAI, tvRiskRecommendationAI;

    ImageView ivLogo, icVerified;
    ImageButton btnBack, btnBagikan;
    Button btnLaporkan, btnAjukan, btnSimpan, btnLihatPerusahaan;
    private String[] statusList;
    private String satuan = "kg";
    private String oftakerId; // simpan ID perusahaan



    private static final String TAG = "ContractDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contract_detail);

        // Inisialisasi view
        initViews();

        // Ambil ID kontrak dari Intent (jika dibuka dari dalam aplikasi)
        String contractId = getIntent().getStringExtra("contract_id");
        String tipe = getIntent().getStringExtra("tipe");

        // Jika null/empty, cek dari deep link
        if (contractId == null || contractId.isEmpty()) {
            Uri data = getIntent().getData();
            if (data != null) {
                contractId = data.getQueryParameter("contract_id");
                Log.d("DeepLink", "Contract ID dari URL = " + contractId);
            }
        }

        // Validasi terakhir: kalau tetap null/empty → keluar
        if (contractId == null || contractId.isEmpty()) {
            Toast.makeText(this, "ID kontrak tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Jika tipe = petani, tombol dinonaktifkan
        if ("petani".equalsIgnoreCase(tipe)) {
            btnSimpan.setEnabled(false);
            btnAjukan.setEnabled(false);
            btnSimpan.setAlpha(0.5f);
            btnAjukan.setAlpha(0.5f);
        }

        // Buat variabel final supaya bisa dipakai di dalam lambda
        final String finalContractId = contractId;

        // Ambil data kontrak dengan loading
        ProgressDialog progressDialog = ProgressDialog.show(this, "Memuat", "Mengambil data kontrak...", true, false);
        new Handler().postDelayed(() -> {
            loadContractDetail(finalContractId);
            progressDialog.dismiss();
        }, 500);

        // Tombol aksi
        setupButtonListeners();

    }


    private void initViews() {
        tvOftakerId = findViewById(R.id.tvOftakerId);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvKebutuhan = findViewById(R.id.tvKebutuhan);
        tvPerusahaan = findViewById(R.id.tvHeaderCompany);
        tvNamaPerusahaan = findViewById(R.id.tvNamaPerusahaan);
        tvJumlahKebutuhan = findViewById(R.id.tvJumlahKebutuhan);
        tvRating = findViewById(R.id.tvRating);
        tvRiskScoreAI = findViewById(R.id.tvRiskScoreAI);
        tvRiskRecommendationAI = findViewById(R.id.tvRiskRecommendationAI);
        tvRiskScore = findViewById(R.id.tvRiskScore);
        tvRiskRecommendation = findViewById(R.id.tvRiskRecommendation);
        tvLokasi = findViewById(R.id.tvLokasi);
        tvHargaPerKg = findViewById(R.id.tvHargaPerKg);
        tvHargaA = findViewById(R.id.tvHargaA);
        tvHargaB = findViewById(R.id.tvHargaB);
        tvHargaC = findViewById(R.id.tvHargaC);
        tvWaktuDibutuhkan = findViewById(R.id.tvWaktuDibutuhkan);
        tvJumlahKontrak = findViewById(R.id.tvJumlahKontrak);
        tvPersyaratan = findViewById(R.id.tvPersyaratan);
        tvDeskripsi = findViewById(R.id.tvDeskripsi);
        tvTimeUpload = findViewById(R.id.tvTimeUpload);
        tvTerbayar = findViewById(R.id.tvTerbayar);
        tvGradeB = findViewById(R.id.tvGradeB);
        tvGradeC = findViewById(R.id.tvGradeC);
        ivLogo = findViewById(R.id.imgLogo);
        icVerified = findViewById(R.id.icVerifiedDetail);
        btnBack = findViewById(R.id.btnBack);
        btnLaporkan = findViewById(R.id.btnLaporkan);
        btnLihatPerusahaan = findViewById(R.id.btnLihatPerusahaan);
        btnAjukan = findViewById(R.id.btnAjukan);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnBagikan = findViewById(R.id.btnBagikan);
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());


        btnLaporkan.setOnClickListener(v -> showReportDialog());


        btnLihatPerusahaan.setOnClickListener(v -> {
            Intent intent = new Intent(ContractDetailActivity.this, PerusahaanActivity.class);
            // kalau mau bawa data oftaker_id:
            intent.putExtra("company_id", oftakerId);
            startActivity(intent);
        });
        btnSimpan.setOnClickListener(v -> {
            saveContractToServer(); // simpan kontrak ke server
        });
        btnBagikan.setOnClickListener(v -> {
            String contractId = getIntent().getStringExtra("contract_id");

            // fallback kalau null → ambil dari deep link
            if (contractId == null || contractId.isEmpty()) {
                Uri data = getIntent().getData();
                if (data != null) {
                    contractId = data.getQueryParameter("contract_id");
                }
            }

            if (contractId == null || contractId.isEmpty()) {
                Toast.makeText(this, "Contract ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            String shareUrl = "https://sistemcerdasindonesia.com/contract/contract?contract_id=" + contractId;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Kontrak Pertanian");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Lihat detail kontrak di aplikasi: " + shareUrl);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Kontrak via"));
        });

        btnAjukan.setOnClickListener(v -> showUserAgreementDialog());
    }

    private void loadContractDetail(String contractId) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.BASE_URL + "get_contract_by_id.php?contract_id=" + contractId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        Log.e(TAG, "Response code: " + responseCode);
                        return null;
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    return sb.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Error: ", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result == null || result.isEmpty()) {
                    Toast.makeText(ContractDetailActivity.this, "Gagal memuat data kontrak", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject targetObject = new JSONObject(result);
                    if (!targetObject.has("id")) {
                        Toast.makeText(ContractDetailActivity.this, "Kontrak tidak ditemukan", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    NumberFormat formatter = NumberFormat.getInstance(new Locale("id", "ID"));
                    satuan = targetObject.optString("satuan", "kg"); // ambil satuan dari server

                    oftakerId = targetObject.getString("oftaker_id");
                    tvOftakerId.setText(oftakerId);

                    tvHeaderTitle.setText(targetObject.getString("kebutuhan"));
                    tvKebutuhan.setText(targetObject.getString("kebutuhan") + " |");
                    tvNamaPerusahaan.setText(targetObject.getString("namaPerusahaan"));
                    tvPerusahaan.setText(targetObject.getString("namaPerusahaan"));
                    if (targetObject.has("terbayarKontrak")) {
                        tvTerbayar.setText("Terbayar: " + targetObject.getString("terbayarKontrak") + " kontrak");
                    } else {
                        tvTerbayar.setText("Terbayar: 0 kontrak");
                    }

                    if (targetObject.has("certificate") &&
                            !targetObject.isNull("certificate") &&
                            !targetObject.getString("certificate").isEmpty()) {
                        icVerified.setVisibility(View.VISIBLE);
                    } else {
                        icVerified.setVisibility(View.GONE);
                    }

                    tvJumlahKebutuhan.setText(
                            formatter.format(Integer.parseInt(targetObject.getString("jumlahkebutuhan")))
                                    + " " + satuan
                    );
                    tvGradeB.setText(
                            formatter.format(Integer.parseInt(targetObject.getString("gradeB")))
                                    + " " + "%"
                    );
                    tvGradeC.setText(
                            formatter.format(Integer.parseInt(targetObject.getString("gradeC")))
                                    + " " + "%"
                    );
                    tvRating.setText("Rating: " + targetObject.getString("rating") + "⭐");
                    tvLokasi.setText("Lokasi: " + targetObject.getString("lokasi"));

                    int hargaPerKg = Integer.parseInt(targetObject.getString("hargaPerKg"));
                    int gradeB = targetObject.optInt("gradeB", 0);
                    int gradeC = targetObject.optInt("gradeC", 0);

// Hitung harga berdasarkan potongan
                    int hargaB = hargaPerKg - (hargaPerKg * gradeB / 100);
                    int hargaC = hargaPerKg - (hargaPerKg * gradeC / 100);

// Tampilkan hasil di TextView
                    tvHargaPerKg.setText("Rp" + formatter.format(hargaPerKg) + "/" + satuan);
                    tvHargaA.setText("Rp" + formatter.format(hargaPerKg));
                    tvHargaB.setText("Rp" + formatter.format(hargaB));
                    tvHargaC.setText("Rp" + formatter.format(hargaC));

                    tvWaktuDibutuhkan.setText("Waktu Dibutuhkan: " + targetObject.getString("waktuDibutuhkan"));
                    tvJumlahKontrak.setText(targetObject.getString("jumlahKontrak") + " peserta telah ajukan kontrak");
                    tvPersyaratan.setText(targetObject.getString("persyaratan"));
                    tvDeskripsi.setText(targetObject.getString("deskripsi"));
                    tvTimeUpload.setText("Diunggah: " + targetObject.getString("timeUpload"));

                    Glide.with(ContractDetailActivity.this)
                            .load(targetObject.getString("logoUrl"))
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivLogo);
// Tambahkan ini di akhir onPostExecute, setelah mengisi semua TextView
                    calculateContractRiskAi(targetObject);  // versi AI
                    calculateContractRisk(targetObject);    // versi awal



                } catch (JSONException e) {
                    Log.e(TAG, "JSON Error: ", e);
                    Toast.makeText(ContractDetailActivity.this, "Data tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Laporkan Kontrak");

        // Inflate layout custom dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_laporkan_kontrak, null);
        builder.setView(dialogView);

        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        EditText etMessage = dialogView.findViewById(R.id.etMessage);
        TextView tvNama = dialogView.findViewById(R.id.tvNama);
        TextView tvPerusahaan = dialogView.findViewById(R.id.tvNamaPerusahaan);
        TextView tvContractId = dialogView.findViewById(R.id.tvContractId);

        // Ambil data user dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);
        String nama = prefs.getString("nama_pengguna", "");

        // Ambil data kontrak dari TextView
        String contractId = getIntent().getStringExtra("contract_id");
        String namaPerusahaan = tvNamaPerusahaan.getText().toString();

        // Set otomatis ke TextView
        tvNama.setText(nama);
        tvPerusahaan.setText(namaPerusahaan);
        tvContractId.setText(contractId);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String subject = etSubject.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Mohon isi subject dan pesan", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim ke database via PHP
            submitReportToServer(userId, nama, contractId, namaPerusahaan, subject, message);
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void submitReportToServer(int userId, String nama, String contractId, String perusahaan, String subject, String message) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String data = "user_id=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
                            "&nama=" + URLEncoder.encode(nama, "UTF-8") +
                            "&contract_id=" + URLEncoder.encode(contractId, "UTF-8") +
                            "&namaPerusahaan=" + URLEncoder.encode(perusahaan, "UTF-8") +
                            "&subject=" + URLEncoder.encode(subject, "UTF-8") +
                            "&message=" + URLEncoder.encode(message, "UTF-8");

                    URL url = new URL(ApiConfig.BASE_URL + "report_contract.php"); // endpoint PHP
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    return response.toString();
                } catch (Exception e) {
                    Log.e(TAG, "submitReportToServer Error: ", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response == null) {
                    Toast.makeText(ContractDetailActivity.this, "Gagal mengirim laporan", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject result = new JSONObject(response);
                    if (result.has("success") && result.getBoolean("success")) {
                        Toast.makeText(ContractDetailActivity.this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show();
                    } else if (result.has("error")) {
                        Toast.makeText(ContractDetailActivity.this, "Gagal: " + result.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContractDetailActivity.this, "Respon server tidak dikenali", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: ", e);
                    Toast.makeText(ContractDetailActivity.this, "Respon server tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }



    private void sendReportEmail(String subject, String fullMessage) {
        String recipient = "sistemcerdasindonesia@gmail.com";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipient));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, fullMessage);

        try {
            startActivity(Intent.createChooser(emailIntent, "Kirim email..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Tidak ada aplikasi email yang terpasang.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUserAgreementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Persetujuan Kontrak");

        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setTextSize(14);
        textView.setPadding(32, 32, 32, 32);
        textView.setText(
                "Dengan ini saya menyatakan bahwa saya:\n\n" +
                        "1. Telah membaca dan memahami seluruh informasi mengenai kontrak kerja sama pertanian.\n" +
                        "2. Bersedia mengikuti semua ketentuan dalam kontrak termasuk:\n" +
                        "   • Jenis dan jumlah komoditas.\n" +
                        "   • Waktu kontrak dan lokasi lahan.\n" +
                        "   • Tanggung jawab dan kewajiban saya.\n\n" +
                        "3. Tanggung jawab dan kewajiban saya sebagai mitra kontrak adalah:\n" +
                        "   • Menyediakan lahan sesuai dengan yang diajukan.\n" +
                        "   • Melaksanakan budidaya sesuai standar GAP (Good Agricultural Practices).\n" +
                        "   • Melaporkan perkembangan secara berkala kepada perusahaan.\n" +
                        "   • Menyampaikan hasil panen tepat waktu sesuai jadwal kontrak.\n" +
                        "   • Tidak mengalihkan kontrak kepada pihak lain tanpa persetujuan tertulis.\n\n" +
                        "4. Data yang saya berikan adalah benar.\n" +
                        "5. Saya bersedia diverifikasi secara administratif dan lapangan.\n" +
                        "6. Saya menyadari konsekuensi dari pelanggaran kontrak, termasuk pembatalan kerjasama.\n\n" +
                        "Dengan ini saya menyatakan bahwa saya:\n\n" +
                        "1. Telah membaca dan memahami seluruh informasi mengenai kontrak kerja sama pertanian.\n" +
                        "2. Bersedia mengikuti semua ketentuan dalam kontrak termasuk:\n" +
                        "   • Jenis dan jumlah komoditas.\n" +
                        "   • Waktu kontrak dan lokasi lahan.\n" +
                        "   • Tanggung jawab dan kewajiban saya.\n\n" +
                        "3. Tanggung jawab dan kewajiban saya sebagai mitra kontrak adalah:\n" +
                        "   • Menyediakan lahan sesuai dengan yang diajukan.\n" +
                        "   • Melaksanakan budidaya sesuai standar GAP (Good Agricultural Practices).\n" +
                        "   • Melaporkan perkembangan secara berkala kepada perusahaan.\n" +
                        "   • Menyampaikan hasil panen tepat waktu sesuai jadwal kontrak.\n" +
                        "   • Tidak mengalihkan kontrak kepada pihak lain tanpa persetujuan tertulis.\n\n" +
                        "4. Data yang saya berikan adalah benar.\n" +
                        "5. Saya bersedia diverifikasi secara administratif dan lapangan.\n" +
                        "6. Saya menyadari konsekuensi dari pelanggaran kontrak, termasuk pembatalan kerjasama.\n\n" +
                        "7. Saya bersedia membagi hasil sesuai ketentuan.\n" +
                        "8. Saya menyetujui adanya potongan biaya yaitu:\n" +
                        "   • 1% untuk developer sebagai biaya layanan.\n" +
                        "   • 2% untuk admin poktan sebagai biaya administrasi.\n\n" +
                        "Dengan melanjutkan, saya menyetujui dan siap mengajukan kontrak.");

        scrollView.addView(textView);

        builder.setView(scrollView);

        builder.setPositiveButton("Setuju", (dialog, which) -> {
            showAjukanDialog();  // baru muncul dialog pengajuan kontrak
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
    // Data harga normal komoditas (kisaran Rp/kg, kecuali disebut lain)

    private Map<String, int[]> getCommodityPriceRange() {
        Map<String, int[]> data = new HashMap<>();

        // 🌾 Serealia & Pangan Pokok
        data.put("padi", new int[]{5500, 7000});                   // sedikit dinaikkan
        data.put("gabah", new int[]{6000, 8000});
        data.put("beras medium", new int[]{12000, 15000});
        data.put("beras premium", new int[]{14000, 18000});
        data.put("jagung", new int[]{5000, 6500});
        data.put("jagung pipilan", new int[]{5000, 7000});
        data.put("kedelai", new int[]{9000, 11000});
        data.put("singkong", new int[]{2500, 4000});
        data.put("ubi jalar", new int[]{5000, 7000});

        // 🥦 Hortikultura (sayuran)
        data.put("cabai rawit merah", new int[]{60000, 90000});   // sesuai data real ~ 80.000
        data.put("cabai merah keriting", new int[]{35000, 60000});
        data.put("cabai merah besar", new int[]{50000, 70000});
        data.put("tomat", new int[]{7000, 10000});
        data.put("kentang", new int[]{9000, 13000});
        data.put("kubis", new int[]{5000, 8000});
        data.put("wortel", new int[]{8000, 12000});
        data.put("bawang merah", new int[]{30000, 50000});
        data.put("bawang putih", new int[]{25000, 40000});
        data.put("sawi", new int[]{6000, 10000});
        data.put("terong", new int[]{6000, 10000});
        data.put("kacang panjang", new int[]{7000, 11000});
        data.put("bayam", new int[]{5000, 8000});
        data.put("kangkung", new int[]{5000, 7000});
        data.put("brokoli", new int[]{15000, 25000});
        data.put("pakcoy", new int[]{8000, 15000});

        // 🍎 Hortikultura (buah-buahan)
        data.put("pepaya", new int[]{4000, 7000});
        data.put("pisang", new int[]{8000, 12000});
        data.put("jeruk", new int[]{15000, 25000});
        data.put("mangga", new int[]{12000, 22000});
        data.put("apel", new int[]{25000, 40000});
        data.put("semangka", new int[]{4000, 8000});
        data.put("melon", new int[]{8000, 13000});
        data.put("nanas", new int[]{7000, 12000});
        data.put("alpukat", new int[]{15000, 25000});
        data.put("durian", new int[]{50000, 90000});
        data.put("salak", new int[]{10000, 20000});
        data.put("anggur", new int[]{30000, 60000});
        data.put("strawberry", new int[]{50000, 80000});

        // 🌴 Perkebunan
        data.put("kopi arabika", new int[]{120000, 180000});
        data.put("kopi robusta", new int[]{90000, 130000});
        data.put("kakao fermentasi", new int[]{120000, 150000});
        data.put("teh pucuk basah", new int[]{4000, 8000});
        data.put("kelapa", new int[]{8000, 12000});
        data.put("kelapa sawit", new int[]{1200, 2500});   // TBS
        data.put("karet", new int[]{10000, 15000});
        data.put("cengkeh", new int[]{120000, 180000});
        data.put("lada", new int[]{80000, 120000});
        data.put("pala", new int[]{90000, 140000});

        // 🐓 Peternakan
        data.put("ayam broiler", new int[]{35000, 60000});     // pasar = ~ Rp 38.000-40.000
        data.put("ayam kampung", new int[]{60000, 90000});
        data.put("telur ayam", new int[]{25000, 35000});
        data.put("telur ayam kampung", new int[]{40000, 60000});
        data.put("daging sapi", new int[]{120000, 160000});
        data.put("daging kambing", new int[]{120000, 160000});
        data.put("susu sapi segar", new int[]{8000, 12000});
        data.put("bebek", new int[]{40000, 70000});
        data.put("telur bebek", new int[]{30000, 50000});
        data.put("domba", new int[]{100000, 150000});

        // 🐟 Perikanan
        data.put("lele", new int[]{25000, 35000});
        data.put("nila", new int[]{30000, 40000});
        data.put("gurame", new int[]{40000, 60000});
        data.put("patin", new int[]{25000, 35000});
        data.put("bandeng", new int[]{30000, 45000});
        data.put("udang vaname", new int[]{70000, 100000});
        data.put("udang windu", new int[]{100000, 150000});
        data.put("ikan tongkol", new int[]{30000, 50000});
        data.put("ikan tuna", new int[]{60000, 100000});

        return data;
    }
    // ⏳ Lama waktu budidaya/panen/produksi normal tiap komoditas (dalam hari)
    private Map<String, Integer> getCommodityDuration() {
        Map<String, Integer> data = new HashMap<>();

        // 🌾 Serealia & Pangan Pokok
        data.put("padi", 120);          // ± 3-4 bulan
        data.put("gabah", 120);
        data.put("beras medium", 0);    // hasil olahan, bukan budidaya langsung
        data.put("beras premium", 0);
        data.put("jagung", 100);        // ± 3 bulan
        data.put("jagung pipilan", 100);
        data.put("kedelai", 90);        // ± 3 bulan
        data.put("singkong", 210);      // ± 7 bulan
        data.put("ubi jalar", 120);     // ± 4 bulan

        // 🥦 Hortikultura (sayuran) – umumnya pendek
        data.put("cabai rawit merah", 90);
        data.put("cabai merah keriting", 90);
        data.put("cabai merah besar", 90);
        data.put("tomat", 75);
        data.put("kentang", 100);
        data.put("kubis", 90);
        data.put("wortel", 100);
        data.put("bawang merah", 70);
        data.put("bawang putih", 120);
        data.put("sawi", 40);
        data.put("terong", 75);
        data.put("kacang panjang", 55);
        data.put("bayam", 30);
        data.put("kangkung", 25);
        data.put("brokoli", 90);
        data.put("pakcoy", 40);

        // 🍎 Hortikultura (buah-buahan) – banyak tanaman tahunan
        data.put("pepaya", 240);    // mulai panen 8 bulan
        data.put("pisang", 270);    // ± 9 bulan
        data.put("jeruk", 365);     // tahunan
        data.put("mangga", 365);    // tahunan
        data.put("apel", 365);
        data.put("semangka", 75);
        data.put("melon", 70);
        data.put("nanas", 300);
        data.put("alpukat", 365);
        data.put("durian", 1095);   // 3 tahun+
        data.put("salak", 365);
        data.put("anggur", 180);
        data.put("strawberry", 120);

        // 🌴 Perkebunan – umumnya multi-year crop
        data.put("kopi arabika", 1095);    // mulai produksi 3 tahun
        data.put("kopi robusta", 1095);
        data.put("kakao fermentasi", 730); // ± 2 tahun
        data.put("teh pucuk basah", 730);  // panen setelah 2 tahun, rutin dipetik
        data.put("kelapa", 1460);          // 4 tahun
        data.put("kelapa sawit", 1460);    // 4 tahun
        data.put("karet", 2190);           // 6 tahun
        data.put("cengkeh", 1825);         // 5 tahun
        data.put("lada", 730);             // 2 tahun
        data.put("pala", 1825);            // 5 tahun

        // 🐓 Peternakan – produksi harian
        data.put("ayam broiler", 35);      // panen 30–40 hari
        data.put("ayam kampung", 150);     // 5 bulan
        data.put("telur ayam ras", 150);   // mulai produksi 5 bulan
        data.put("telur ayam kampung", 180);
        data.put("daging sapi", 730);      // penggemukan 2 tahun
        data.put("daging kambing", 365);
        data.put("susu sapi segar", 730);  // mulai laktasi 2 tahun
        data.put("bebek", 180);
        data.put("telur bebek", 180);
        data.put("domba", 365);

        // 🐟 Perikanan – umumnya 3–6 bulan
        data.put("lele", 90);
        data.put("nila", 120);
        data.put("gurame", 240);
        data.put("patin", 150);
        data.put("bandeng", 180);
        data.put("udang vaname", 120);
        data.put("udang windu", 180);
        data.put("ikan tongkol", 365);   // tangkap laut, bukan budidaya singkat
        data.put("ikan tuna", 730);

        return data;
    }

    // Catatan karakteristik komoditas
    private Map<String, String> getCommodityNotes() {
        Map<String, String> notes = new HashMap<>();

        // 🌾 Pangan pokok (serealia & umbi)
        notes.put("padi", "Butuh lahan luas dan air cukup, panen musiman (3-4 bulan).");
        notes.put("gabah", "Dipengaruhi musim panen raya, harga sering turun saat surplus.");
        notes.put("beras medium", "Kebutuhan tinggi stabil, kualitas dipengaruhi penggilingan.");
        notes.put("beras premium", "Harga tinggi tapi persaingan ketat dengan impor.");
        notes.put("jagung", "Harga dipengaruhi musim panen dan ketersediaan pakan.");
        notes.put("jagung pipilan", "Rentan kadar air tinggi → rawan jamur aflatoksin.");
        notes.put("kedelai", "Sebagian besar masih impor, harga fluktuatif.");
        notes.put("singkong", "Butuh waktu lama panen (8-12 bulan). Harga rendah saat surplus.");
        notes.put("ubi jalar", "Rentan busuk saat penyimpanan, kualitas dipengaruhi cuaca.");

        // 🥦 Hortikultura (sayuran)
        notes.put("cabai rawit merah", "Rentan hama trips dan layu fusarium, harga sangat fluktuatif.");
        notes.put("cabai merah keriting", "Mudah terserang penyakit antraknosa, harga fluktuatif.");
        notes.put("cabai merah besar", "Permintaan tinggi tapi risiko penyakit tinggi.");
        notes.put("tomat", "Mudah busuk, umur simpan pendek → risiko logistik tinggi.");
        notes.put("kentang", "Butuh suhu dingin, rentan busuk umbi & penyakit hawar daun.");
        notes.put("kubis", "Rentan ulat grayak & busuk basah, butuh suhu sejuk.");
        notes.put("wortel", "Butuh suhu dingin, rentan bercak daun.");
        notes.put("bawang merah", "Harga sangat fluktuatif, rawan busuk saat penyimpanan.");
        notes.put("bawang putih", "Sebagian besar impor, harga dipengaruhi pasokan luar negeri.");
        notes.put("sawi", "Cepat panen, tapi umur simpan pendek.");
        notes.put("terong", "Rentan hama kutu daun & ulat buah.");
        notes.put("kacang panjang", "Butuh perawatan intensif, panen berulang.");
        notes.put("bayam", "Mudah layu, umur simpan sangat singkat.");
        notes.put("kangkung", "Cepat panen, mudah layu setelah dipetik.");
        notes.put("brokoli", "Butuh suhu sejuk, mudah busuk pasca panen.");
        notes.put("pakcoy", "Umur simpan pendek, sensitif suhu panas.");

        // 🍎 Hortikultura (buah-buahan)
        notes.put("pepaya", "Mudah rusak, umur simpan pendek.");
        notes.put("pisang", "Mudah matang sekaligus, rentan kerusakan saat transportasi.");
        notes.put("jeruk", "Harga dipengaruhi musim, rentan busuk saat penyimpanan.");
        notes.put("mangga", "Musiman, kualitas dipengaruhi cuaca.");
        notes.put("apel", "Umur simpan lumayan panjang, sebagian besar impor.");
        notes.put("semangka", "Mudah pecah & rusak saat transportasi.");
        notes.put("melon", "Rentan busuk, harga musiman.");
        notes.put("nanas", "Mudah rusak saat matang penuh, perlu distribusi cepat.");
        notes.put("alpukat", "Harga stabil, umur simpan relatif pendek.");
        notes.put("durian", "Musiman, harga tinggi tapi sangat fluktuatif.");
        notes.put("salak", "Cepat busuk jika kulit luka.");
        notes.put("anggur", "Mudah rusak, rentan jamur.");
        notes.put("strawberry", "Mudah rusak & sensitif cuaca, perlu cold chain.");

        // 🌴 Perkebunan
        notes.put("kopi arabika", "Butuh dataran tinggi, panen 1 kali setahun, harga premium.");
        notes.put("kopi robusta", "Lebih adaptif, harga lebih rendah dibanding arabika.");
        notes.put("kakao fermentasi", "Butuh perawatan fermentasi pasca panen, harga lebih tinggi.");
        notes.put("teh pucuk basah", "Harga stabil tapi margin tipis.");
        notes.put("kelapa", "Permintaan tinggi stabil, panen berulang.");
        notes.put("kelapa sawit", "Butuh lahan luas, harga tergantung ekspor global.");
        notes.put("karet", "Harga dipengaruhi pasar dunia, panen harian.");
        notes.put("cengkeh", "Musiman, harga tinggi tapi fluktuatif.");
        notes.put("lada", "Harga tinggi, rentan penyakit busuk pangkal batang.");
        notes.put("pala", "Harga tinggi, sensitif penyakit busuk buah.");

        // 🐓 Peternakan
        notes.put("ayam broiler", "Butuh manajemen pakan baik, sensitif penyakit, panen cepat (30-40 hari).");
        notes.put("ayam kampung", "Pertumbuhan lebih lama (5-6 bulan), permintaan stabil.");
        notes.put("telur ayam ras", "Pasokan stabil, harga fluktuatif musiman.");
        notes.put("telur ayam kampung", "Harga lebih tinggi, produksi lebih sedikit.");
        notes.put("daging sapi", "Butuh modal besar & pakan intensif, pertumbuhan lama.");
        notes.put("daging kambing", "Permintaan naik saat hari raya, harga stabil tinggi.");
        notes.put("susu sapi segar", "Butuh cold chain, mudah rusak.");
        notes.put("bebek", "Harga stabil, umur panen lebih lama dari ayam broiler.");
        notes.put("telur bebek", "Umur simpan lebih lama dari telur ayam, pasar khusus.");
        notes.put("domba", "Butuh pakan hijauan, sensitif cuaca panas.");

        // 🐟 Perikanan
        notes.put("lele", "Pertumbuhan cepat (2-3 bulan), harga fluktuatif.");
        notes.put("nila", "Mudah dibudidayakan, harga stabil, panen 3-4 bulan.");
        notes.put("gurame", "Pertumbuhan lama (6-8 bulan), harga tinggi.");
        notes.put("patin", "Pertumbuhan sedang, harga menengah.");
        notes.put("bandeng", "Butuh tambak khusus, panen 4-5 bulan.");
        notes.put("udang vaname", "Rentan penyakit (white spot), butuh kualitas air terjaga.");
        notes.put("udang windu", "Harga tinggi, sensitif penyakit.");
        notes.put("ikan tongkol", "Butuh cold chain, harga fluktuatif.");
        notes.put("ikan tuna", "Nilai tinggi tapi butuh rantai dingin (cold chain).");

        return notes;
    }


    private void calculateContractRisk(JSONObject contract) {
        try {
            String komoditas = contract.getString("kebutuhan").toLowerCase();
            int jumlahKebutuhan = Integer.parseInt(contract.getString("jumlahkebutuhan"));
            int hargaPerKg = Integer.parseInt(contract.getString("hargaPerKg"));
            String waktuDibutuhkanStr = contract.getString("waktuDibutuhkan");
            String lokasiKontrak = contract.getString("lokasi");
            String deskripsi = contract.getString("deskripsi").toLowerCase();

            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String lokasiPetani = sharedPreferences.getString("alamat", ""); // ambil alamat petani

            int riskScore = 0;
            StringBuilder recommendation = new StringBuilder();

            // ---- Risiko jumlah kebutuhan ----
            if (jumlahKebutuhan > 1000) {
                riskScore += 30;
                recommendation.append("Jumlah kebutuhan tinggi → pertimbangkan kapasitas lahan.\n");
            } else if (jumlahKebutuhan > 500) {
                riskScore += 15;
            }

            // ---- Risiko harga berdasarkan komoditas ----
            Map<String, int[]> priceRange = getCommodityPriceRange();
            if (priceRange.containsKey(komoditas)) {
                int[] range = priceRange.get(komoditas);
                int min = range[0], max = range[1];

                if (hargaPerKg < min) {
                    riskScore += 20;
                    recommendation.append("Harga kontrak (" + hargaPerKg +
                            ") jauh di bawah harga pasar (" + min + "-" + max +
                            ") → risiko profit rendah.\n");
                } else if (hargaPerKg > max) {
                    riskScore += 5; // bisa positif tapi ada risiko buyer batal
                    recommendation.append("Harga kontrak (" + hargaPerKg +
                            ") di atas pasar → buyer berpotensi membatalkan kontrak.\n");
                } else {
                    recommendation.append("Harga kontrak sesuai pasar (" + min + "-" + max + ").\n");
                }
            } else {
                recommendation.append("Komoditas tidak ada di referensi harga.\n");
            }
// ---- Risiko karakteristik komoditas ----
            Map<String, String> notes = getCommodityNotes();
            if (notes.containsKey(komoditas)) {
                String note = notes.get(komoditas);
                recommendation.append("Catatan komoditas: ").append(note).append("\n");

                // Bisa tambahkan skor ekstra sesuai risiko
                if (note.toLowerCase().contains("mudah rusak") || note.toLowerCase().contains("umur simpan pendek")) {
                    riskScore += 15; // risiko logistik
                }
                if (note.toLowerCase().contains("hama") || note.toLowerCase().contains("penyakit")) {
                    riskScore += 15; // risiko budidaya
                }
                if (note.toLowerCase().contains("butuh waktu lama")) {
                    riskScore += 10; // risiko waktu
                }
                if (note.toLowerCase().contains("fluktuatif")) {
                    riskScore += 10; // risiko harga
                }
            }

            // ---- Risiko waktu ----
            // ---- Risiko waktu ----
            int daysLeft = 0;
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date endDate = sdf.parse(waktuDibutuhkanStr);
                java.util.Date today = new java.util.Date();
                long diff = endDate.getTime() - today.getTime();
                daysLeft = (int) (diff / (1000 * 60 * 60 * 24));
            } catch (Exception e) {
                Log.e(TAG, "Tanggal tidak valid: " + waktuDibutuhkanStr, e);
            }

// cek waktu kontrak berdasarkan durasi budidaya komoditas
            Map<String, Integer> durations = getCommodityDuration();
            if (durations.containsKey(komoditas)) {
                int neededDays = durations.get(komoditas);
                if (daysLeft < neededDays) {
                    riskScore += 20;
                    recommendation.append("Waktu tersisa (" + daysLeft + " hari) lebih singkat daripada durasi budidaya normal (" + neededDays + " hari) → risiko gagal panen.\n");
                } else {
                    recommendation.append("Waktu kontrak (" + daysLeft + " hari) cukup untuk budidaya " + komoditas + ".\n");
                }
            } else {
                // fallback lama (kalau komoditas tidak ada di referensi durasi)
                if (daysLeft < 7) {
                    riskScore += 25;
                    recommendation.append("Waktu kontrak tersisa sangat singkat → risiko gagal lebih tinggi.\n");
                } else if (daysLeft < 14) {
                    riskScore += 10;
                }
            }

            // Debug log
            Log.d(TAG, "Lokasi Kontrak: " + lokasiKontrak);
            Log.d(TAG, "Lokasi Petani: " + lokasiPetani);

            if (!lokasiPetani.isEmpty() && !lokasiKontrak.isEmpty()) {
                String[] kontrakParts = lokasiKontrak.split(",");
                String[] petaniParts = lokasiPetani.split(",");
                String kabupatenKontrak = kontrakParts.length > 0 ? kontrakParts[0].trim() : "";
                String provinsiKontrak = kontrakParts.length > 1 ? kontrakParts[1].trim() : "";
                String kabupatenPetani = petaniParts.length > 0 ? petaniParts[0].trim() : "";
                String provinsiPetani = petaniParts.length > 1 ? petaniParts[1].trim() : "";

                Log.d(TAG, "Kabupaten Kontrak: " + kabupatenKontrak + ", Provinsi Kontrak: " + provinsiKontrak);
                Log.d(TAG, "Kabupaten Petani: " + kabupatenPetani + ", Provinsi Petani: " + provinsiPetani);

                if (kabupatenKontrak.equalsIgnoreCase(kabupatenPetani) &&
                        provinsiKontrak.equalsIgnoreCase(provinsiPetani)) {
                    recommendation.append("Lokasi sama → risiko logistik rendah.\n");
                } else if (!kabupatenKontrak.equalsIgnoreCase(kabupatenPetani) &&
                        provinsiKontrak.equalsIgnoreCase(provinsiPetani)) {
                    riskScore += 10;
                    recommendation.append("Kabupaten berbeda, provinsi sama → pertimbangkan transportasi.\n");
                } else if (kabupatenKontrak.equalsIgnoreCase(kabupatenPetani) &&
                        !provinsiKontrak.equalsIgnoreCase(provinsiPetani)) {
                    riskScore += 15;
                    recommendation.append("Kabupaten sama tapi provinsi berbeda → risiko administrasi.\n");
                } else {
                    riskScore += 25;
                    recommendation.append("Lokasi kontrak dan lahan jauh → risiko logistik tinggi.\n");
                }
            }

            // ---- Risiko deskripsi ----
            if (deskripsi.contains("darurat") || deskripsi.contains("resiko tinggi")) {
                riskScore += 10;
                recommendation.append("Perhatikan catatan risiko kontrak.\n");
            }

            if (riskScore > 100) riskScore = 100;

            String riskLevel;
            if (riskScore >= 60) riskLevel = "Tinggi ⚠️";
            else if (riskScore >= 30) riskLevel = "Sedang ⚠️";
            else riskLevel = "Rendah ✅";

            showManualRiskResult(riskScore, riskLevel, recommendation.toString());


        } catch (JSONException e) {
            Log.e(TAG, "Error calculateContractRisk", e);
            tvRiskScore.setText("Skor Risiko: -");
            tvRiskRecommendation.setText("Rekomendasi: -");
        }
    }
    // Interface callback untuk fetchContractsAiData
    interface Callback {
        void onSuccess(JSONArray response);
        void onError(String error);
    }

    // Ambil dataset kontrak AI dari server
    private void fetchContractsAiData(Callback callback) {
        String url = ApiConfig.BASE_URL + "get_contracts_ai.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> callback.onSuccess(response),
                error -> {
                    Log.e(TAG, "Fetch contracts AI failed", error);
                    callback.onError(error.toString());
                });

        queue.add(request);
    }

    // Hitung risiko kontrak menggunakan AI
    private void calculateContractRiskAi(JSONObject contract) {
        try {
            String komoditas = contract.optString("kebutuhan", "").toLowerCase();
            int jumlahKebutuhan = contract.optInt("jumlahkebutuhan", 0);
            int hargaPerKg = contract.optInt("hargaPerKg", 0);
            String lokasiKontrak = contract.optString("lokasi", "");
            String deskripsi = contract.optString("deskripsi", "").toLowerCase();

            // Ambil lokasi petani dari SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String lokasiPetani = prefs.getString("alamat", "");

            // Ambil dataset kontrak AI
            fetchContractsAiData(new Callback() {
                @Override
                public void onSuccess(JSONArray response) {
                    try {
                        JSONObject payload = new JSONObject();
                        payload.put("current_contract", contract);
                        payload.put("contracts_history", response);

                        Log.d(TAG, "Payload AI: " + payload.toString());

                        String aiUrl = "https://sistemcerdasindonesia.com/ai/predict_contract_risk_ai";
                        JsonObjectRequest aiRequest = new JsonObjectRequest(
                                Request.Method.POST,
                                aiUrl,
                                payload,
                                aiResponse -> {
                                    try {
                                        int riskScore = aiResponse.optInt("riskScore", 0);
                                        String riskLevel = aiResponse.optString("riskLevel", "Tidak diketahui");
                                        String recommendation = aiResponse.optString("recommendation", "-");

                                        // Tampilkan berdampingan (LinearLayout horizontal)
                                        showAiRiskResult(riskScore, riskLevel, recommendation);


                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing AI response", e);
                                    }
                                },
                                error -> {
                                    Log.e(TAG, "AI request failed", error);
                                    Toast.makeText(getApplicationContext(),
                                            "Gagal menghitung risiko AI", Toast.LENGTH_SHORT).show();
                                }
                        );

                        Volley.newRequestQueue(getApplicationContext()).add(aiRequest);

                    } catch (JSONException e) {
                        Log.e(TAG, "Payload JSON error", e);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(getApplicationContext(),
                            "Gagal ambil dataset AI: " + error, Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error calculateContractRiskAi", e);
        }
    }
    // --- Format hasil manual ---
    private void showManualRiskResult(int riskScore, String riskLevel, String recommendationText) {
        String icon = "✅";
        if (riskLevel.contains("Tinggi")) icon = "⚠️";
        else if (riskLevel.contains("Sedang")) icon = "⚠️";

        String result = "🔎 <b>Skor Risiko:</b> " + riskScore + " (" + riskLevel + " " + icon + ")";

        // Ubah rekomendasi ke bullet point
        String[] lines = recommendationText.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                formatted.append("• ").append(line.trim()).append("<br>");
            }
        }

        tvRiskScore.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY));
        tvRiskRecommendation.setText(Html.fromHtml("<b>Rekomendasi:</b><br>" + formatted.toString(),
                Html.FROM_HTML_MODE_LEGACY));
    }

    // --- Format hasil AI ---
    private void showAiRiskResult(int riskScore, String riskLevel, String recommendationText) {
        String icon = "✅";
        if (riskLevel.contains("Tinggi")) icon = "⚠️";
        else if (riskLevel.contains("Sedang")) icon = "⚠️";

        String result = "🤖 <b>AI Skor Risiko:</b> " + riskScore + " (" + riskLevel + " " + icon + ")";

        // Bullet point
        String[] lines = recommendationText.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                formatted.append("• ").append(line.trim()).append("<br>");
            }
        }

        tvRiskScoreAI.setText(Html.fromHtml(result, Html.FROM_HTML_MODE_LEGACY));
        tvRiskRecommendationAI.setText(Html.fromHtml("<b>AI Rekomendasi:</b><br>" + formatted.toString(),
                Html.FROM_HTML_MODE_LEGACY));
    }

    private void showAjukanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ajukan_kontrak, null);
        builder.setView(dialogView);
        builder.setTitle("Ajukan Kontrak");
        CheckBox cbAsuransi = dialogView.findViewById(R.id.cbAsuransi);
        TextView tvInfoAsuransi = dialogView.findViewById(R.id.tvInfoAsuransi);

        Spinner spinnerLahan = dialogView.findViewById(R.id.spinnerLahan);
        EditText etProgress = dialogView.findViewById(R.id.etProgress);
        EditText etCatatan = dialogView.findViewById(R.id.etCatatan);
        EditText etNama = dialogView.findViewById(R.id.etNama);
        EditText etJumlah = dialogView.findViewById(R.id.etJumlah);
        TextView tvNamaPerusahaanDialog = dialogView.findViewById(R.id.tvNamaPerusahaan);
        TextView tvUserId = dialogView.findViewById(R.id.tvUserId);
        TextView tvCompanyId = dialogView.findViewById(R.id.tvCompanyId);
        TextView tvCompanyEmail = dialogView.findViewById(R.id.tvCompanyEmail);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int user_id = prefs.getInt("id", -1);
        int companyId = prefs.getInt("company_id", -1);
        String namaPengguna = prefs.getString("nama_pengguna", "");
        String companyEmail = prefs.getString("company_email", "");
        String namaPerusahaan = tvNamaPerusahaan.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String lokasiPetani = sharedPreferences.getString("alamat", ""); // ambil alamat petani
        int fasilitatorId = sharedPreferences.getInt("fasilitator_id", -1);
        tvInfoAsuransi.setOnClickListener(v -> showDanaCadanganInfo());
        if (fasilitatorId != -1 && fasilitatorId != 0) {
            cbAsuransi.setVisibility(View.VISIBLE);
            tvInfoAsuransi.setVisibility(View.VISIBLE);
        } else {
            cbAsuransi.setVisibility(View.GONE);
            tvInfoAsuransi.setVisibility(View.GONE);
        }
        etNama.setText(namaPengguna);
        tvNamaPerusahaanDialog.setText(namaPerusahaan);
        tvUserId.setText("User ID: " + user_id);
        tvCompanyId.setText("Company ID: " + companyId);
        tvCompanyEmail.setText("Company Email: " + companyEmail);

        // Ambil jumlah kebutuhan dari TextView utama (hapus satuan dinamis + titik format)
        String jumlahKebutuhanStr = tvJumlahKebutuhan.getText().toString()
                .replace(" " + satuan, "") // gunakan satuan global, bukan hardcode "kg"
                .replace(".", "")
                .trim();
        int jumlahKebutuhan = 0;
        try {
            jumlahKebutuhan = Integer.parseInt(jumlahKebutuhanStr);
        } catch (NumberFormatException e) {
            jumlahKebutuhan = 0;
        }

        // Filter input: hanya angka dan maksimal jumlah kebutuhan
        final int finalJumlahKebutuhan = jumlahKebutuhan;
        etJumlah.setFilters(new InputFilter[]{
                (source, start, end, dest, dstart, dend) -> {
                    String newValue = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                    try {
                        int input = Integer.parseInt(newValue);
                        if (input > finalJumlahKebutuhan) {
                            return ""; // blok input jika melebihi
                        }
                    } catch (NumberFormatException e) {
                        // biarkan kosong
                    }
                    return null;
                }
        });

        loadLahanList(spinnerLahan, etProgress);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String lahanDipilih = spinnerLahan.getSelectedItem().toString();
            String progress = etProgress.getText().toString().trim();
            String catatan = etCatatan.getText().toString().trim();
            String jumlah = etJumlah.getText().toString().trim();
            boolean ikutAsuransi = cbAsuransi.isChecked();

            if (progress.isEmpty() || jumlah.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua data", Toast.LENGTH_SHORT).show();
                return;
            }

            int jumlahInt = Integer.parseInt(jumlah);
            if (jumlahInt > finalJumlahKebutuhan) {
                Toast.makeText(this, "Jumlah tidak boleh lebih dari " + finalJumlahKebutuhan + " " + satuan, Toast.LENGTH_SHORT).show();
                return;
            }

            // Hitung total harga kontrak dan potongan asuransi 1%
            String hargaPerKgStr = tvHargaPerKg.getText().toString().replace("Rp", "").replace(".", "").trim();
            int hargaPerKg = 0;
            try {
                hargaPerKg = Integer.parseInt(hargaPerKgStr);
            } catch (NumberFormatException e) {
                hargaPerKg = 0;
            }
            double totalHarga = hargaPerKg * jumlahInt;
            double potonganAsuransi = ikutAsuransi ? totalHarga * 0.01 : 0;

            // Kirim ke server
            sendKontrakDataToServer(user_id, namaPengguna, namaPerusahaan, companyId,
                    lahanDipilih, progress, catatan, jumlah, satuan, ikutAsuransi, potonganAsuransi);
        });


        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showDanaCadanganInfo() {
        AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        infoDialog.setTitle("Penjelasan Dana Cadangan & Asuransi Pertanian");

        String textContent =
                "BAB IV – PENGELOLAAN RISIKO\n\n" +
                        "4.1 Risiko Gagal Panen\n" +
                        "- Petani wajib melaporkan kondisi pertanaman melalui aplikasi secara berkala.\n" +
                        "- Admin poktan mengajukan klaim dana untuk petani\n" +
                        "- Fasilitator memverifikasi laporan lapangan atau melalui dokumentasi foto/video.\n" +
                        "- Jika terjadi gagal panen akibat cuaca ekstrem, hama, atau penyakit, dana cadangan dapat digunakan untuk menutupi sebagian atau seluruh kerugian Petani.\n" +
                        "- Besaran bantuan dana cadangan disesuaikan dengan tingkat kerugian yang diajukan oleh admin poktan:\n" +
                        "  • Kerugian ringan (<30%) → ditanggung 30% oleh dana cadangan.\n" +
                        "  • Kerugian sedang (30–70%) → ditanggung 50% oleh dana cadangan.\n" +
                        "  • Kerugian berat (>70%) → dapat ditanggung hingga 100% sesuai saldo dana cadangan.\n\n" +
                        "4.2 Risiko Keterlambatan Pasokan\n" +
                        "- Penjadwalan ulang dilakukan dengan persetujuan kedua pihak.\n" +
                        "- Jika keterlambatan menyebabkan kerugian bagi Offtaker, dana cadangan dapat menanggung hingga 50% dari total kerugian yang terverifikasi.\n" +
                        "- Fasilitator memediasi apabila penjadwalan ulang tidak mencukupi atau terjadi sengketa antar pihak.\n" +
                        "- Simulasi:\n" +
                        "  • Kontrak: 10 ton padi dijadwalkan 1 Oktober.\n" +
                        "  • Petani hanya mengirim 7 ton, sisa 3 ton dijadwalkan ulang ke 5 Oktober.\n" +
                        "  • Kerugian Offtaker: Rp1.500.000.\n" +
                        "  • Dana cadangan menutup 50% = Rp750.000, sisanya menjadi tanggungan Petani.\n" +
                        "  • Fasilitator memastikan kedua pihak menyetujui penjadwalan ulang dan kompensasi.\n\n" +
                        "BAB V – DANA CADANGAN\n\n" +
                        "5.1 Tujuan\n" +
                        "- Dana cadangan berfungsi sebagai tabungan asuransi digital gotong royong antar pihak.\n" +
                        "- Menutupi risiko kerugian bagi Petani maupun Offtaker sesuai kesepakatan.\n" +
                        "- Bersumber dari potongan 1% dari setiap transaksi kontrak atau sesuai persentase yang disepakati.\n\n" +
                        "5.2 Penggunaan\n" +
                        "- Dana dapat digunakan hingga 100% dari saldo terkumpul untuk modal awal petani.\n" +
                        "- Dana dapat digunakan hingga 100% dari saldo terkumpul untuk menutupi kerugian terverifikasi.\n" +
                        "- Prioritas penggunaan:\n" +
                        "  1. Gagal panen (prioritas utama)\n" +
                        "  2. Keterlambatan pasokan\n" +
                        "  2. Modal awal\n" +
                        "- Setiap pencairan wajib melalui verifikasi dan persetujuan fasilitator.\n" +
                        "- Setiap pengajuan modal awal, hasil kontrak akan otomatis terpotong untuk pembayaran pinjaman modal awal\n" +
                        "- Pencairan dan saldo dikirim email dan diakses secara transparan.\n\n" +
                        "5.3 Pembagian Penggunaan Dana Cadangan\n" +
                        "- Jika saldo mencukupi:\n" +
                        "  • Petani: dapat meminjam dan menutup hingga 100% kerugian gagal panen.\n" +
                        "  • Offtaker: dapat menutup hingga 50% kerugian akibat keterlambatan/pasokan kurang sisanya tanggungjawab petani.\n" +
                        "- Fasilitator dapat mengajukan bantuan tambahan dari lembaga terkait (CSR, Dinas Pertanian, Universitas).\n\n" +
                        "5.4 Simulasi\n" +
                        "- Saldo awal dana cadangan: Rp10.000.000\n" +
                        "- Kasus 1 – Gagal panen: Kerugian Petani Rp2.000.000 → dicairkan penuh → sisa saldo Rp8.000.000.\n" +
                        "- Kasus 2 – Keterlambatan pasokan: Kerugian Offtaker Rp1.500.000 → dana menutup 50% = Rp750.000 → sisa saldo Rp7.250.000.\n" +
                        "- Kasus 3 – Fluktuasi harga: Harga kontrak Rp5.000/kg, harga pasar Rp4.500/kg, volume 10 ton → selisih Rp5.000.000 → dana menutup 50% = Rp2.500.000 → sisa saldo Rp4.750.000.\n\n" +
                        "5.5 Transparansi dan Audit\n" +
                        "- Semua transaksi dicatat otomatis di sistem aplikasi.\n" +
                        "- Setiap pengguna dapat melihat saldo dan riwayat penggunaan tanpa mengungkap identitas pihak lain.\n" +
                        "- Audit digital dilakukan berkala oleh fasilitator utama (misal Dinas Pertanian setempat).\n";

        TextView tv = new TextView(this);
        tv.setText(textContent);
        tv.setPadding(30, 30, 30, 30);
        tv.setTextSize(14);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(tv);

        infoDialog.setView(scrollView);
        infoDialog.setPositiveButton("Tutup", (dialog, which) -> dialog.dismiss());
        infoDialog.show();
    }



    private void loadLahanList(Spinner spinnerLahan, EditText etProgress) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        final int companyId = prefs.getInt("company_id", -1);
        final int userId = prefs.getInt("id", -1); // ambil userId dari prefs dan buat final

        if (companyId <= 0 || userId <= 0) {
            Toast.makeText(this, "Company atau User tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                try {
                    URL url = new URL(ApiConfig.BASE_URL + "get_lahan_list.php?company_id=" + companyId + "&user_id=" + userId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONArray array = new JSONArray(sb.toString());
                    String[] lahanList = new String[array.length()];
                    statusList = new String[array.length()];

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        lahanList[i] = obj.getString("name") + " - " + obj.getString("area_size") + " ha";
                        statusList[i] = obj.getString("statuslahan");
                    }
                    return lahanList;

                } catch (Exception e) {
                    Log.e(TAG, "Error loading lahan", e);
                    return new String[]{};
                }
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (result.length > 0) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(ContractDetailActivity.this, R.layout.spinner_item_1, result);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLahan.setAdapter(adapter);
                    etProgress.setText(statusList[0]);

                    spinnerLahan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            etProgress.setText(statusList[position]);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Toast.makeText(ContractDetailActivity.this, "Gagal memuat daftar lahan", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void saveContractToServer() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int user_id = prefs.getInt("id", -1);
        String contractId = getIntent().getStringExtra("contract_id");

        if (user_id == -1 || contractId == null || contractId.isEmpty()) {
            Toast.makeText(this, "Data user atau kontrak tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    String data = "user_id=" + URLEncoder.encode(String.valueOf(user_id), "UTF-8") +
                            "&contract_id=" + URLEncoder.encode(contractId, "UTF-8");

                    URL url = new URL(ApiConfig.BASE_URL + "save_contract.php"); // endpoint PHP untuk menyimpan
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(data);
                    writer.flush();
                    writer.close();
                    os.close();

                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    return response.toString();

                } catch (Exception e) {
                    Log.e(TAG, "saveContractToServer Error: ", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response == null) {
                    Toast.makeText(ContractDetailActivity.this, "Gagal menyimpan kontrak", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    JSONObject result = new JSONObject(response);
                    if (result.has("success") && result.getBoolean("success")) {
                        Toast.makeText(ContractDetailActivity.this, "Kontrak berhasil disimpan", Toast.LENGTH_SHORT).show();
                    } else if (result.has("error")) {
                        Toast.makeText(ContractDetailActivity.this, "Gagal: " + result.getString("error"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContractDetailActivity.this, "Respon tidak dikenali", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: ", e);
                    Toast.makeText(ContractDetailActivity.this, "Respon server tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void sendKontrakDataToServer(int user_id, String nama, String perusahaan, int companyId,
                                         String lahan, String progress, String catatan, String jumlah,
                                         String satuan, boolean ikutAsuransi, double potonganAsuransi) {


        String contractId = getIntent().getStringExtra("contract_id");
       String  oftakerId = tvOftakerId.getText().toString();

        // Bersihkan jumlah dari satuan dan titik
        String jumlahBersih = jumlah.replace(satuan, "").replace(".", "").trim();

        // Debug: tampilkan semua field
        Log.d(TAG, "DEBUG sendKontrakDataToServer:");
        Log.d(TAG, "user_id=" + user_id);
        Log.d(TAG, "nama=" + nama);
        Log.d(TAG, "perusahaan=" + perusahaan);
        Log.d(TAG, "companyId=" + companyId);
        Log.d(TAG, "lahan=" + lahan);
        Log.d(TAG, "statuslahan=" + progress);
        Log.d(TAG, "catatan=" + catatan);
        Log.d(TAG, "jumlah=" + jumlahBersih);
        Log.d(TAG, "satuan=" + satuan);
        Log.d(TAG, "contractId=" + contractId);
        Log.d(TAG, "oftakerId=" + oftakerId);

        new AsyncTask<Void, Void, String>() {
            int httpResponseCode = -1;

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection conn = null;
                try {
                    // Encode semua field
                    String postData = "user_id=" + URLEncoder.encode(String.valueOf(user_id), "UTF-8") +
                            "&nama=" + URLEncoder.encode(nama, "UTF-8") +
                            "&perusahaan=" + URLEncoder.encode(perusahaan, "UTF-8") +
                            "&company_id=" + URLEncoder.encode(String.valueOf(companyId), "UTF-8") +
                            "&lahan=" + URLEncoder.encode(lahan, "UTF-8") +
                            "&statuslahan=" + URLEncoder.encode(progress, "UTF-8") +
                            "&catatan=" + URLEncoder.encode(catatan, "UTF-8") +
                            "&jumlah=" + URLEncoder.encode(jumlah, "UTF-8") +
                            "&satuan=" + URLEncoder.encode(satuan, "UTF-8") +
                            "&contract_id=" + URLEncoder.encode(contractId, "UTF-8") +
                            "&oftaker_id=" + URLEncoder.encode(oftakerId, "UTF-8") +
                            "&ikut_asuransi=" + URLEncoder.encode(String.valueOf(ikutAsuransi), "UTF-8") +
                            "&asuransi=" + URLEncoder.encode(String.valueOf(potonganAsuransi), "UTF-8");


                    URL url = new URL(ApiConfig.BASE_URL + "ajukan_kontrak.php");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    try (OutputStream os = conn.getOutputStream();
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                        writer.write(postData);
                        writer.flush();
                    }

                    httpResponseCode = conn.getResponseCode();
                    InputStream is = (httpResponseCode >= 200 && httpResponseCode < 400) ?
                            conn.getInputStream() : conn.getErrorStream();

                    BufferedReader in = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    Log.d(TAG, "Server response: " + response.toString());
                    return response.toString();

                } catch (Exception e) {
                    Log.e(TAG, "sendKontrakDataToServer Error: ", e);
                    return null;
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                if (response == null) {
                    String msg = (httpResponseCode == 404) ?
                            "Endpoint tidak ditemukan (404)" :
                            "Gagal mengirim data ke server, kode: " + httpResponseCode;
                    Toast.makeText(ContractDetailActivity.this, msg, Toast.LENGTH_LONG).show();
                    return;
                }

                try {
                    JSONObject result = new JSONObject(response);

                    if (result.has("success") && result.getBoolean("success")) {
                        Toast.makeText(ContractDetailActivity.this, "Kontrak berhasil diajukan", Toast.LENGTH_SHORT).show();
                        // Lanjut ke PemberkasanActivity
                        Intent intent = new Intent(ContractDetailActivity.this, PemberkasanActivity.class);
                        intent.putExtra("nama_pengguna", nama);
                        intent.putExtra("user_id", user_id);
                        intent.putExtra("nama_perusahaan", perusahaan);
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        intent.putExtra("alamat", prefs.getString("alamat", ""));
                        intent.putExtra("kebutuhan", tvHeaderTitle.getText().toString());
                        intent.putExtra("jumlah_kebutuhan", jumlahBersih);
                        intent.putExtra("satuan", satuan);
                        intent.putExtra("lahan", lahan);
                        intent.putExtra("status_lahan", progress);
                        intent.putExtra("waktu_dibutuhkan", tvWaktuDibutuhkan.getText().toString().replace("Waktu Dibutuhkan: ", ""));
                        intent.putExtra("tanggal_pengajuan", new java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new java.util.Date()));
                        intent.putExtra("catatan", catatan);
                        intent.putExtra("ikut_asuransi", ikutAsuransi ? "Ya" : "Tidak");

                        startActivity(intent);
                        finish();

                    } else if (result.has("error")) {
                        String errorMsg = result.getString("error");
                        Toast.makeText(ContractDetailActivity.this, "Gagal: " + errorMsg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ContractDetailActivity.this, "Respon server tidak dikenali", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Unexpected response: " + response);
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: ", e);
                    Toast.makeText(ContractDetailActivity.this, "Respon server tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


}
