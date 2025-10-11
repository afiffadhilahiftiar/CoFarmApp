package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class TambahAnggotaActivity extends AppCompatActivity {

    private EditText edtEmail;
    private TextView txtNama, txtPeran;
    private Button btnCari, btnTambah;
    private ProgressDialog progressDialog;

    private String companyId;

    private static final String URL_CARI_USER = ApiConfig.BASE_URL + "cari_user.php";
    private static final String URL_TAMBAH_ANGGOTA = ApiConfig.BASE_URL + "tambah_anggota.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_anggota);

        edtEmail = findViewById(R.id.edtEmail);
        txtNama = findViewById(R.id.txtNama);
        txtPeran = findViewById(R.id.txtPeran);
        btnCari = findViewById(R.id.btnCari);
        btnTambah = findViewById(R.id.btnTambah);

        companyId = getIntent().getStringExtra("company_id");

        btnCari.setOnClickListener(v -> cariUser());
        btnTambah.setOnClickListener(v -> tambahUser());
    }

    private void cariUser() {
        String email = edtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan email terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mencari pengguna...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_CARI_USER, params,
                response -> {
                    progressDialog.dismiss();
                    try {
                        String nama = response.getString("nama");
                        String peran = response.getString("peran");

                        txtNama.setText("Nama: " + nama);
                        txtPeran.setText("Peran: " + peran);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal mencari pengguna", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }


    private void tambahUser() {
        String email = edtEmail.getText().toString().trim();
        String peran = txtPeran.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Masukkan email terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menambahkan anggota...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        long startTime = System.currentTimeMillis();

        JSONObject params = new JSONObject();
        try {
            params.put("company_id", companyId);
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_TAMBAH_ANGGOTA, params,
                response -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remainingTime = Math.max(3000 - elapsed, 0); // supaya minimal 3 detik

                    new android.os.Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        try {
                            boolean success = response.getBoolean("success");
                            String message = response.getString("message");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            if (success) {
                                Intent intent = new Intent(TambahAnggotaActivity.this, DaftarAnggotaActivity.class);
                                intent.putExtra("company_id", companyId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Terjadi kesalahan pada response", Toast.LENGTH_SHORT).show();
                        }
                    }, remainingTime);
                },
                error -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remainingTime = Math.max(3000 - elapsed, 0);

                    new android.os.Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        error.printStackTrace();
                        Toast.makeText(this, "Gagal menambahkan anggota", Toast.LENGTH_SHORT).show();
                    }, remainingTime);
                });

        Volley.newRequestQueue(this).add(request);
    }

}
