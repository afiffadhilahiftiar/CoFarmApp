package com.example.contractfarmingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.SopirAdapter;
import com.example.contractfarmingapp.models.SopirModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DaftarSopirActivity extends AppCompatActivity {
    private String companyId;
    private String userPeran;
    private Button btnTambahSopir;
    private RecyclerView recyclerView;
    private SopirAdapter sopirAdapter;
    private List<SopirModel> sopirList = new ArrayList<>();

    private static final String URL_GET_SOPIR = ApiConfig.BASE_URL + "get_sopir.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_sopir);

        recyclerView = findViewById(R.id.recyclerViewSopir);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userPeran = getIntent().getStringExtra("peran");
        companyId = getIntent().getStringExtra("company_id");
        // Perbaikan: sertakan Context
        sopirAdapter = new SopirAdapter(this, sopirList, companyId, userPeran);

        recyclerView.setAdapter(sopirAdapter);

        btnTambahSopir = findViewById(R.id.btnTambahSopir);


        btnTambahSopir.setOnClickListener(v -> {
            Intent intent = new Intent(DaftarSopirActivity.this, TambahSopirActivity.class);
            intent.putExtra("company_id", companyId);
            startActivity(intent);
        });


        if (!"user".equalsIgnoreCase(userPeran)) {
            btnTambahSopir.setVisibility(Button.VISIBLE);
            Toast.makeText(this, "Peran Anda: " + userPeran, Toast.LENGTH_SHORT).show();
        }


        loadSopir();
    }

    private void loadSopir() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URL_GET_SOPIR + "?company_id=" + companyId,
                null,
                response -> {
                    sopirList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject sopir = response.getJSONObject(i);

                            SopirModel model = new SopirModel(
                                    sopir.getString("id"),
                                    sopir.getString("nama"),
                                    sopir.getString("no_hp"),
                                    sopir.getString("kendaraan"),
                                    sopir.getString("plat_nomor"),
                                    sopir.getString("kapasitas"),
                                    sopir.getString("foto_sopir"),
                                    sopir.getString("foto_sim"),
                                    sopir.getString("foto_stnk"),
                                    sopir.getString("foto_kendaraan"),
                                    sopir.getString("link_lokasi")
                            );
                            sopirList.add(model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    sopirAdapter.notifyDataSetChanged();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal memuat data sopir", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
}
