package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TambahTraktorActivity extends AppCompatActivity {

    EditText etJenis, etKapasitas, etNamaOperator, etNoHp;
    ImageView imgFotoTraktor;
    Button btnFotoTraktor, btnSimpan;

    Bitmap bitmapTraktor;

    private static final int PICK_IMAGE = 100;
    private String companyId;

    private static final String URL_ADD_TRAKTOR = ApiConfig.BASE_URL + "add_traktor.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_traktor);

        // Input
        etJenis = findViewById(R.id.etJenisTraktor);
        etKapasitas = findViewById(R.id.etKapasitas);
        etNamaOperator = findViewById(R.id.etNamaOperator);
        etNoHp = findViewById(R.id.etNoHp);

        // Image preview
        imgFotoTraktor = findViewById(R.id.imgFotoTraktor);

        // Tombol
        btnFotoTraktor = findViewById(R.id.btnFotoTraktor);
        btnSimpan = findViewById(R.id.btnSimpan);

        companyId = getIntent().getStringExtra("company_id");
        if (TextUtils.isEmpty(companyId)) {
            Toast.makeText(this, "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Event pilih foto
        btnFotoTraktor.setOnClickListener(v -> openGallery());

        // Event simpan
        btnSimpan.setOnClickListener(v -> {
            if (validateInput()) {
                simpanTraktor();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                bitmapTraktor = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgFotoTraktor.setImageBitmap(bitmapTraktor);
            } catch (IOException e) {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(etJenis.getText().toString().trim())) {
            etJenis.setError("Jenis traktor wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etKapasitas.getText().toString().trim())) {
            etKapasitas.setError("Kapasitas wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etNamaOperator.getText().toString().trim())) {
            etNamaOperator.setError("Nama operator wajib diisi");
            return false;
        }
        if (TextUtils.isEmpty(etNoHp.getText().toString().trim())) {
            etNoHp.setError("Nomor HP operator wajib diisi");
            return false;
        }
        if (bitmapTraktor == null) {
            Toast.makeText(this, "Foto traktor wajib diunggah", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void simpanTraktor() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URL_ADD_TRAKTOR,
                response -> {
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        String status = obj.getString("status");
                        String message = obj.getString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
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
                params.put("jenis_traktor", etJenis.getText().toString().trim());
                params.put("company_id", companyId);
                params.put("kapasitas", etKapasitas.getText().toString().trim());
                params.put("nama_operator", etNamaOperator.getText().toString().trim());
                params.put("no_hp", etNoHp.getText().toString().trim());
                params.put("foto_traktor", bitmapToBase64(bitmapTraktor));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);
    }
}
