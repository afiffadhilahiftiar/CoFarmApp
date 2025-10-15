package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditSopirActivity extends AppCompatActivity {

    EditText etNama, etNoHp, etKendaraan, etPlat, etKapasitas, etLinkLokasi;
    ImageView imgFotoSopir, imgFotoSim, imgFotoStnk, imgFotoKendaraan;
    Button btnSimpan, btnFotoSopir, btnFotoSim, btnFotoStnk, btnFotoKendaraan;

    Bitmap bitmapSopir, bitmapSim, bitmapStnk, bitmapKendaraan;
    String sopirId;
    private String companyId;

    private static final int PICK_SOPIR = 1;
    private static final int PICK_SIM = 2;
    private static final int PICK_STNK = 3;
    private static final int PICK_KENDARAAN = 4;

    // URL lama untuk validasi jika user tidak upload ulang
    private String oldFotoSopir = "", oldFotoSim = "", oldFotoStnk = "", oldFotoKendaraan = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_sopir);

        etNama = findViewById(R.id.etNama);
        etNoHp = findViewById(R.id.etNoHp);
        etLinkLokasi = findViewById(R.id.etLinkLokasi);
        etKendaraan = findViewById(R.id.etKendaraan);
        etPlat = findViewById(R.id.etPlat);
        etKapasitas = findViewById(R.id.etKapasitas);
        imgFotoSopir = findViewById(R.id.imgFotoSopir);
        imgFotoSim = findViewById(R.id.imgFotoSim);
        imgFotoStnk = findViewById(R.id.imgFotoStnk);
        imgFotoKendaraan = findViewById(R.id.imgFotoKendaraan);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnFotoSopir = findViewById(R.id.btnFotoSopir);
        btnFotoSim = findViewById(R.id.btnFotoSim);
        btnFotoStnk = findViewById(R.id.btnFotoStnk);
        btnFotoKendaraan = findViewById(R.id.btnFotoKendaraan);

        companyId = getIntent().getStringExtra("company_id");

        // Ambil data dari Intent
        Intent intent = getIntent();
        sopirId = intent.getStringExtra("id");
        etNama.setText(intent.getStringExtra("nama"));
        etNoHp.setText(intent.getStringExtra("no_hp"));
        etKendaraan.setText(intent.getStringExtra("kendaraan"));
        etPlat.setText(intent.getStringExtra("plat_nomor"));
        etKapasitas.setText(intent.getStringExtra("kapasitas"));
        etLinkLokasi.setText(intent.getStringExtra("link_lokasi"));

        // Simpan URL lama
        oldFotoSopir = intent.getStringExtra("foto_sopir");
        oldFotoSim = intent.getStringExtra("foto_sim");
        oldFotoStnk = intent.getStringExtra("foto_stnk");
        oldFotoKendaraan = intent.getStringExtra("foto_kendaraan");

        // Load foto dari URL intent pakai Glide
        if (oldFotoSopir != null && !oldFotoSopir.isEmpty())
            Glide.with(this).load(oldFotoSopir).into(imgFotoSopir);
        if (oldFotoSim != null && !oldFotoSim.isEmpty())
            Glide.with(this).load(oldFotoSim).into(imgFotoSim);
        if (oldFotoStnk != null && !oldFotoStnk.isEmpty())
            Glide.with(this).load(oldFotoStnk).into(imgFotoStnk);
        if (oldFotoKendaraan != null && !oldFotoKendaraan.isEmpty())
            Glide.with(this).load(oldFotoKendaraan).into(imgFotoKendaraan);

        // Tombol upload foto (gantikan gambar dengan baru)
        btnFotoSopir.setOnClickListener(v -> pilihGambar(PICK_SOPIR));
        btnFotoSim.setOnClickListener(v -> pilihGambar(PICK_SIM));
        btnFotoStnk.setOnClickListener(v -> pilihGambar(PICK_STNK));
        btnFotoKendaraan.setOnClickListener(v -> pilihGambar(PICK_KENDARAAN));

        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) updateSopir();
        });
    }

    private void pilihGambar(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                switch (requestCode) {
                    case PICK_SOPIR:
                        bitmapSopir = bitmap;
                        imgFotoSopir.setImageBitmap(bitmap);
                        break;
                    case PICK_SIM:
                        bitmapSim = bitmap;
                        imgFotoSim.setImageBitmap(bitmap);
                        break;
                    case PICK_STNK:
                        bitmapStnk = bitmap;
                        imgFotoStnk.setImageBitmap(bitmap);
                        break;
                    case PICK_KENDARAAN:
                        bitmapKendaraan = bitmap;
                        imgFotoKendaraan.setImageBitmap(bitmap);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
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
        if (TextUtils.isEmpty(etPlat.getText().toString().trim())) {
            etPlat.setError("Plat nomor wajib diisi");
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

        // ✅ Validasi semua gambar wajib ada (baik baru upload atau dari URL lama)
        if ((bitmapSopir == null && TextUtils.isEmpty(oldFotoSopir)) ||
                (bitmapSim == null && TextUtils.isEmpty(oldFotoSim)) ||
                (bitmapStnk == null && TextUtils.isEmpty(oldFotoStnk)) ||
                (bitmapKendaraan == null && TextUtils.isEmpty(oldFotoKendaraan))) {
            Toast.makeText(this, "Semua foto wajib tersedia", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateSopir() {
        String nama = etNama.getText().toString().trim();
        String noHp = etNoHp.getText().toString().trim();
        String kendaraan = etKendaraan.getText().toString().trim();
        String plat = etPlat.getText().toString().trim();
        String kapasitas = etKapasitas.getText().toString().trim();
        String linkLokasi = etLinkLokasi.getText().toString().trim();

        // Format nomor HP agar selalu +62
        if (!noHp.startsWith("+")) {
            noHp = "+62" + noHp.replaceFirst("^0+(?!$)", "");
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.show();

        String finalNoHp = noHp;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ApiConfig.BASE_URL + "update_sopir.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        Toast.makeText(EditSopirActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EditSopirActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditSopirActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", sopirId);
                params.put("company_id", companyId);
                params.put("nama", nama);
                params.put("no_hp", finalNoHp);
                params.put("kendaraan", kendaraan);
                params.put("plat_nomor", plat);
                params.put("kapasitas", kapasitas);
                params.put("link_lokasi", linkLokasi);

                // Kirim base64 baru jika ada, kalau tidak kirim string kosong
                params.put("foto_sopir", encodeImage(bitmapSopir));
                params.put("foto_sim", encodeImage(bitmapSim));
                params.put("foto_stnk", encodeImage(bitmapStnk));
                params.put("foto_kendaraan", encodeImage(bitmapKendaraan));

                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
