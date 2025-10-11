package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.AnggotaAdapter;
import com.example.contractfarmingapp.models.AnggotaModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DaftarAnggotaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnTambahAnggota;
    private AnggotaAdapter adapter;
    private ArrayList<AnggotaModel> anggotaList;
    private String companyId;
    private String userPeran;
    private ProgressDialog progressDialog;

    private static final String URL_GET_ANGGOTA = ApiConfig.BASE_URL + "get_anggota.php?company_id=";
    private static final String URL_HAPUS_ANGGOTA = ApiConfig.BASE_URL + "hapus_anggota.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_anggota);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat data anggota...");
        progressDialog.setCancelable(false);

        recyclerView = findViewById(R.id.recyclerViewAnggota);
        btnTambahAnggota = findViewById(R.id.btnTambahAnggota);

        companyId = getIntent().getStringExtra("company_id");

        if (companyId == null || companyId.isEmpty()) {
            Toast.makeText(this, "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userPeran = getIntent().getStringExtra("peran");
        if (!"admin".equalsIgnoreCase(userPeran)) {
            btnTambahAnggota.setVisibility(Button.GONE); // Sembunyikan tombol tambah
            Toast.makeText(this, "Peran Anda: " + userPeran, Toast.LENGTH_SHORT).show();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        anggotaList = new ArrayList<>();

        // ✅ Adapter dengan listener hapus
        adapter = new AnggotaAdapter(this, anggotaList, userPeran, (anggota, position) -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Hapus")
                    .setMessage("Apakah Anda yakin ingin menghapus anggota ini?")
                    .setPositiveButton("Ya", (dialog, which) -> hapusAnggota(anggota.getId(), position))
                    .setNegativeButton("Batal", null)
                    .show();
        });

        recyclerView.setAdapter(adapter);

        // Muat data dari server
        loadAnggotaFromServer();

        btnTambahAnggota.setOnClickListener(v -> {
            Intent intent = new Intent(DaftarAnggotaActivity.this, TambahAnggotaActivity.class);
            intent.putExtra("company_id", companyId);
            startActivity(intent);
            finish();
        });
    }

    private void loadAnggotaFromServer() {
        String url = URL_GET_ANGGOTA + companyId;

        progressDialog.show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    anggotaList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            int id = obj.optInt("id", -1);   // ✅ ambil id sebagai int
                            String nama = obj.getString("nama");
                            String peran = obj.getString("peran");
                            String areaSize = obj.optString("area_size", "-");
                            String fotoProfile = obj.optString("foto_profile", ""); // ✅ ambil foto profil
                            int jumlahKontrak = obj.optInt("jumlah_kontrak", 0);    // ✅ ambil jumlah kontrak

                            anggotaList.add(new AnggotaModel(id, nama, peran, areaSize, fotoProfile, jumlahKontrak));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal memuat data anggota", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    // ✅ Fungsi hapus anggota
    private void hapusAnggota(int idAnggota, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, URL_HAPUS_ANGGOTA,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            anggotaList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Anggota dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal menghapus", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Gagal menghubungi server", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(idAnggota)); // ✅ konversi int → String
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
