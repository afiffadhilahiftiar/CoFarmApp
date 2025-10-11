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

public class EditTraktorActivity extends AppCompatActivity {

    EditText etJenisTraktor, etNamaOperator, etNoHpOperator, etKapasitas;
    ImageView imgFotoTraktor;
    Button btnSimpan, btnFotoTraktor;

    Bitmap bitmapTraktor;
    String traktorId;
    private String companyId;

    private static final int PICK_TRUKTOR = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_traktor);

        etJenisTraktor = findViewById(R.id.etJenisTraktor);
        etNamaOperator = findViewById(R.id.etNamaOperator);
        etNoHpOperator = findViewById(R.id.etNoHp);
        etKapasitas = findViewById(R.id.etKapasitas);
        imgFotoTraktor = findViewById(R.id.imgFotoTraktor);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnFotoTraktor = findViewById(R.id.btnFotoTraktor);

        companyId = getIntent().getStringExtra("company_id");

        // Ambil data dari Intent
        Intent intent = getIntent();
        traktorId = intent.getStringExtra("id");
        etJenisTraktor.setText(intent.getStringExtra("jenis_traktor"));
        etNamaOperator.setText(intent.getStringExtra("nama_operator"));
        etNoHpOperator.setText(intent.getStringExtra("no_hp"));
        etKapasitas.setText(intent.getStringExtra("kapasitas"));

        String fotoTraktor = intent.getStringExtra("foto_traktor");
        if (fotoTraktor != null && !fotoTraktor.isEmpty()) {
            Glide.with(this).load(fotoTraktor).into(imgFotoTraktor);
        }

        btnFotoTraktor.setOnClickListener(v -> pilihGambar());

        btnSimpan.setOnClickListener(v -> updateTraktor());
    }

    private void pilihGambar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_TRUKTOR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_TRUKTOR && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                bitmapTraktor = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imgFotoTraktor.setImageBitmap(bitmapTraktor);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void updateTraktor() {
        String jenis = etJenisTraktor.getText().toString().trim();
        String nama = etNamaOperator.getText().toString().trim();
        String noHp = etNoHpOperator.getText().toString().trim();
        String kapasitas = etKapasitas.getText().toString().trim();

        if (TextUtils.isEmpty(jenis) || TextUtils.isEmpty(nama) || TextUtils.isEmpty(noHp)) {
            Toast.makeText(this, "Jenis traktor, nama operator, dan no HP wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tambahkan +62 jika tidak diawali +
        if (!noHp.startsWith("+")) {
            noHp = "+62" + noHp.replaceFirst("^0+(?!$)", "");
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menyimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String finalNoHp = noHp;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                ApiConfig.BASE_URL + "update_traktor.php",
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);
                        Toast.makeText(EditTraktorActivity.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        if ("success".equals(obj.getString("status"))) finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(EditTraktorActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditTraktorActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", traktorId);
                params.put("company_id", companyId);
                params.put("jenis_traktor", jenis);
                params.put("nama_operator", nama);
                params.put("no_hp", finalNoHp);
                params.put("kapasitas", kapasitas);
                params.put("foto_traktor", encodeImage(bitmapTraktor));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
