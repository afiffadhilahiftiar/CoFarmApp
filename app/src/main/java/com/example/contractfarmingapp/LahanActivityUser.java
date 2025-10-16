package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.LahanAdapter;
import com.example.contractfarmingapp.models.LahanModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LahanActivityUser extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Button btnTambahLahan, btnPeta;
    private ArrayList<LahanModel> lahanList;
    private LahanAdapter adapter;
    private Switch switchFilterCompany;
    private ArrayList<LahanModel> fullLahanList; // simpan semua data

    private String companyId;
    private String companyName;
    private String peran;

    private static final String URL_GET_LAHAN = ApiConfig.BASE_URL + "get_lahan.php?company_id=";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String namaArea = data.getStringExtra("nama_area");
            double luas = data.getDoubleExtra("luas_area", 0);
            String wkt = data.getStringExtra("wkt_polygon");
            // lakukan sesuatu dengan hasil ini
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lahan);

        recyclerView = findViewById(R.id.recyclerViewLahan);
        btnTambahLahan = findViewById(R.id.btnTambahLahan);

        companyId = getIntent().getStringExtra("company_id");
        peran = getIntent().getStringExtra("peran");
        companyName = getIntent().getStringExtra("company_name");

        if (companyId == null || companyId.isEmpty()) {
            Toast.makeText(this, "ID perusahaan tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnPeta = findViewById(R.id.btnPeta);

// Aksi saat tombol ditekan
        btnPeta.setOnClickListener(v -> {
            Intent intent = new Intent(LahanActivityUser.this, MapViewGeofencingActivity.class);
            intent.putExtra("company_id", companyId);
            intent.putExtra("company_name", companyName);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lahanList = new ArrayList<>();
        adapter = new LahanAdapter(this, lahanList, peran, model -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Hapus Lahan")
                    .setMessage("Yakin ingin menghapus lahan \"" + model.getNama() + "\"?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        deleteLahan(model.getId());
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });


        recyclerView.setAdapter(adapter);

        loadLahanData();
        switchFilterCompany = findViewById(R.id.switchFilterCompany);
        btnTambahLahan.setOnClickListener(v -> {
            Intent intent = new Intent(LahanActivityUser.this, MapGeofencingActivity.class);
            intent.putExtra("company_id", companyId);
            startActivityForResult(intent, 1001);
        });
        btnTambahLahan.setVisibility(Button.GONE);


        fullLahanList = new ArrayList<>();

        switchFilterCompany.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Filter hanya yang company_id sama dengan intent
                ArrayList<LahanModel> filtered = new ArrayList<>();
                for (LahanModel l : fullLahanList) {
                    if (companyId.equals(l.getCompany_id())) {
                        filtered.add(l);
                    }
                }
                lahanList.clear();
                lahanList.addAll(filtered);
            } else {
                // Tampilkan semua
                lahanList.clear();
                lahanList.addAll(fullLahanList);
            }
            adapter.notifyDataSetChanged();
        });

    }

    private void loadLahanData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat data lahan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = URL_GET_LAHAN + companyId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    lahanList.clear();
                    fullLahanList.clear(); // simpan semua data

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.getString("id");
                            String company_id = obj.getString("company_id");
                            String nama = obj.getString("nama_lahan");
                            String perusahaan = obj.getString("namaperusahaan");
                            String email = obj.getString("email");
                            String lokasi = obj.getString("lokasi");
                            String status = obj.getString("status_lahan");
                            String komoditas = obj.getString("komoditas");
                            String luas = obj.getString("luas_area");

                            // Sesuaikan urutan parameter dengan constructor LahanModel
                            LahanModel model = new LahanModel(
                                    id, company_id, nama, perusahaan, email, lokasi, status, komoditas, luas
                            );

                            fullLahanList.add(model); // simpan semua data
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Default: tampilkan semua
                    lahanList.clear();
                    lahanList.addAll(fullLahanList);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal memuat data lahan", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void deleteLahan(String lahanId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Menghapus lahan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = ApiConfig.BASE_URL + "delete_lahan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            Toast.makeText(this, "Lahan berhasil dihapus", Toast.LENGTH_SHORT).show();
                            loadLahanData(); // refresh list
                        } else {
                            Toast.makeText(this, "Gagal menghapus lahan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Respon tidak valid dari server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Terjadi kesalahan jaringan", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", lahanId); // Kirim ID sebagai parameter
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }



}
