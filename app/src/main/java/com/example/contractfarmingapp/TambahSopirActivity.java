package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TambahSopirActivity extends AppCompatActivity {

    EditText etNama, etNoHp, etKendaraan, etPlatNomor, etKapasitas, etLinkLokasi;
    ImageView imgFotoSopir, imgFotoSim, imgFotoStnk, imgFotoKendaraan;
    Button btnFotoSopir, btnFotoSim, btnFotoStnk, btnFotoKendaraan, btnSimpan;

    Bitmap bitmapSopir, bitmapSIM, bitmapSTNK, bitmapKendaraan;

    private static final int PICK_IMAGE = 100;
    private int currentImageType = 0;
    private String companyId;

    private static final String URL_ADD_SOPIR = ApiConfig.BASE_URL + "add_sopir.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_sopir);

        // Input
        etNama = findViewById(R.id.etNama);
        etNoHp = findViewById(R.id.etNoHp);
        etKendaraan = findViewById(R.id.etKendaraan);
        etPlatNomor = findViewById(R.id.etPlat);
        etLinkLokasi = findViewById(R.id.etLinkLokasi);
        etKapasitas = findViewById(R.id.etKapasitas);

        // Image preview
        imgFotoSopir = findViewById(R.id.imgFotoSopir);
        imgFotoSim = findViewById(R.id.imgFotoSim);
        imgFotoStnk = findViewById(R.id.imgFotoStnk);
        imgFotoKendaraan = findViewById(R.id.imgFotoKendaraan);

        // Tombol
        btnFotoSopir = findViewById(R.id.btnFotoSopir);
        btnFotoSim = findViewById(R.id.btnFotoSim);
        btnFotoStnk = findViewById(R.id.btnFotoStnk);
        btnFotoKendaraan = findViewById(R.id.btnFotoKendaraan);
        btnSimpan = findViewById(R.id.btnSimpan);

        companyId = getIntent().getStringExtra("company_id");
        if (TextUtils.isEmpty(companyId)) {
            Toast.makeText(this, "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Event pilih foto
        btnFotoSopir.setOnClickListener(v -> openGallery(1));
        btnFotoSim.setOnClickListener(v -> openGallery(2));
        btnFotoStnk.setOnClickListener(v -> openGallery(3));
        btnFotoKendaraan.setOnClickListener(v -> openGallery(4));

        // Event simpan
        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                simpanSopir();
            }
        });
    }

    private void openGallery(int type) {
        currentImageType = type;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    @SuppressWarnings("deprecation") // biar aman di compile API tinggi
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                switch (currentImageType) {
                    case 1:
                        bitmapSopir = bitmap;
                        imgFotoSopir.setImageBitmap(bitmap);
                        break;
                    case 2:
                        bitmapSIM = bitmap;
                        imgFotoSim.setImageBitmap(bitmap);
                        break;
                    case 3:
                        bitmapSTNK = bitmap;
                        imgFotoStnk.setImageBitmap(bitmap);
                        break;
                    case 4:
                        bitmapKendaraan = bitmap;
                        imgFotoKendaraan.setImageBitmap(bitmap);
                        break;
                }
            } catch (IOException e) {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etNama.getText().toString().trim())) {
            etNama.setError("Nama wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etNoHp.getText().toString().trim())) {
            etNoHp.setError("Nomor HP wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etKendaraan.getText().toString().trim())) {
            etKendaraan.setError("Jenis kendaraan wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etPlatNomor.getText().toString().trim())) {
            etPlatNomor.setError("Plat nomor wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etKapasitas.getText().toString().trim())) {
            etKapasitas.setError("Kapasitas wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etLinkLokasi.getText().toString().trim())) {
            etLinkLokasi.setError("Link lokasi wajib diisi");
            return false;
        }
        if (bitmapSopir == null || bitmapSIM == null || bitmapSTNK == null || bitmapKendaraan == null) {
            Toast.makeText(this, "Semua foto wajib diunggah", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void simpanSopir() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URL_ADD_SOPIR,
                response -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    try {
                        // Parsing JSON dari PHP
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // biar activity sebelumnya bisa refresh
                            finish(); // keluar activity
                        } else {
                            Toast.makeText(this, "Gagal: " + message, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Response tidak valid: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    String msg = error.getMessage() != null ? error.getMessage() : "Tidak bisa terhubung ke server";
                    Toast.makeText(this, "Gagal menyimpan: " + msg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama", etNama.getText().toString().trim());
                params.put("company_id", companyId);
                params.put("no_hp", etNoHp.getText().toString().trim());
                params.put("kendaraan", etKendaraan.getText().toString().trim());
                params.put("plat_nomor", etPlatNomor.getText().toString().trim());
                params.put("link_lokasi", etLinkLokasi.getText().toString().trim());
                params.put("kapasitas", etKapasitas.getText().toString().trim());

                params.put("foto_sopir", bitmapToBase64(bitmapSopir));
                params.put("foto_sim", bitmapToBase64(bitmapSIM));
                params.put("foto_stnk", bitmapToBase64(bitmapSTNK));
                params.put("foto_kendaraan", bitmapToBase64(bitmapKendaraan));

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // kompres biar lebih kecil
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
    }
}
