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
import com.example.contractfarmingapp.adapters.TraktorAdapter;
import com.example.contractfarmingapp.models.TraktorModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DaftarTraktorActivity extends AppCompatActivity {
    private String companyId;
    private String userPeran;
    private Button btnTambahTraktor;
    private RecyclerView recyclerView;
    private TraktorAdapter traktorAdapter;
    private List<TraktorModel> traktorList = new ArrayList<>();

    private static final String URL_GET_TRAKTOR = ApiConfig.BASE_URL + "get_traktor.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_traktor);

        recyclerView = findViewById(R.id.recyclerViewTraktor);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userPeran = getIntent().getStringExtra("peran");
        companyId = getIntent().getStringExtra("company_id");

        traktorAdapter = new TraktorAdapter(this, traktorList, companyId, userPeran);
        recyclerView.setAdapter(traktorAdapter);

        btnTambahTraktor = findViewById(R.id.btnTambahTraktor);

        btnTambahTraktor.setOnClickListener(v -> {
            Intent intent = new Intent(DaftarTraktorActivity.this, TambahTraktorActivity.class);
            intent.putExtra("company_id", companyId);
            startActivity(intent);
        });

        if (!"user".equalsIgnoreCase(userPeran)) {
            btnTambahTraktor.setVisibility(Button.VISIBLE);
            Toast.makeText(this, "Peran Anda: " + userPeran, Toast.LENGTH_SHORT).show();
        }

        loadTraktor();
    }

    private void loadTraktor() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                URL_GET_TRAKTOR + "?company_id=" + companyId,
                null,
                response -> {
                    traktorList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject traktor = response.getJSONObject(i);

                            TraktorModel model = new TraktorModel(
                                    traktor.getString("id"),
                                    traktor.getString("jenis_traktor"),
                                    traktor.getString("company_id"),
                                    traktor.getString("kapasitas"),
                                    traktor.getString("nama_operator"),
                                    traktor.getString("no_hp"),
                                    traktor.getString("foto_traktor"),
                                    traktor.getString("created_at")
                            );
                            traktorList.add(model);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    traktorAdapter.notifyDataSetChanged();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal memuat data traktor", Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }
}
