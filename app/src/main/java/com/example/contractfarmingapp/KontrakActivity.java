package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.KontrakAdapter;
import com.example.contractfarmingapp.models.KontrakModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class KontrakActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KontrakAdapter kontrakAdapter;
    private ArrayList<KontrakModel> kontrakList;
    private ProgressDialog progressDialog;

    private static final String BASE_URL = ApiConfig.BASE_URL + "kontrak_data_keyword.php";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kontrak);

        recyclerView = findViewById(R.id.recyclerViewKontrak);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        kontrakList = new ArrayList<>();
        kontrakAdapter = new KontrakAdapter(this, kontrakList);
        recyclerView.setAdapter(kontrakAdapter);

        // ambil keyword dari intent
        String keyword = getIntent().getStringExtra("keyword");

        if (keyword != null && !keyword.isEmpty()) {
            loadKontrakData(keyword); // pencarian
        } else {
            loadKontrakData(null); // tampilkan semua kontrak user
        }
    }

    private void loadKontrakData(@Nullable String keyword) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        String requestUrl = BASE_URL + "?user_id=" + userId;
        if (keyword != null && !keyword.isEmpty()) {
            requestUrl += "&keyword=" + Uri.encode(keyword);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                response -> {
                    progressDialog.dismiss();
                    parseKontrakData(response, keyword);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void parseKontrakData(JSONArray response, @Nullable String keyword) {
        try {
            kontrakList.clear();

            // Normalisasi keyword
            String lowerKeyword = (keyword != null) ? keyword.toLowerCase() : null;
            String numericKeyword = (keyword != null) ? keyword.replaceAll("[^0-9]", "") : null;

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);

                KontrakModel kontrak = new KontrakModel(
                        obj.getString("id"),
                        obj.getString("kebutuhan"),
                        obj.getString("jumlahkebutuhan"),
                        obj.getString("satuan"),
                        obj.getString("namaPerusahaan"),
                        obj.getString("rating"),
                        obj.getString("lokasi"),
                        obj.getString("hargaPerKg"),
                        obj.getString("waktuDibutuhkan"),
                        obj.getString("jumlahKontrak"),
                        obj.getString("persyaratan"),
                        obj.getString("timeUpload"),
                        obj.getString("logoUrl"),
                        obj.getString("certificate")
                );

                if (keyword == null || keyword.isEmpty()) {
                    // tidak ada filter → tampil semua
                    kontrakList.add(kontrak);
                } else {
                    // Normalisasi field angka
                    String hargaNumeric = kontrak.getHargaPerKg().replaceAll("[^0-9]", "");
                    String jumlahNumeric = kontrak.getJumlahkebutuhan().replaceAll("[^0-9]", "");

                    // Filter
                    if (
                            kontrak.getKebutuhan().toLowerCase().contains(lowerKeyword) ||
                                    kontrak.getNamaPerusahaan().toLowerCase().contains(lowerKeyword) ||
                                    kontrak.getLokasi().toLowerCase().contains(lowerKeyword) ||
                                    kontrak.getPersyaratan().toLowerCase().contains(lowerKeyword) ||
                                    kontrak.getWaktuDibutuhkan().toLowerCase().contains(lowerKeyword) ||
                                    (!hargaNumeric.isEmpty() && numericKeyword != null && hargaNumeric.contains(numericKeyword)) ||
                                    (!jumlahNumeric.isEmpty() && numericKeyword != null && jumlahNumeric.contains(numericKeyword))
                    ) {
                        kontrakList.add(kontrak);
                    }
                }
            }

            kontrakAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Toast.makeText(this, "Terjadi kesalahan saat parsing data JSON", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
