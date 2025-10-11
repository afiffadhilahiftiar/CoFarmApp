package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.recyclerview.widget.ItemTouchHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.KontrakAdapter;
import com.example.contractfarmingapp.models.KontrakModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KontrakSimpanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private KontrakAdapter kontrakAdapter;
    private ArrayList<KontrakModel> kontrakList;
    private ProgressDialog progressDialog;

    private static final String BASE_URL = ApiConfig.BASE_URL + "saved_contracts_data.php"; // endpoint baru

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kontrak);

        recyclerView = findViewById(R.id.recyclerViewKontrak);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        kontrakList = new ArrayList<>();
        kontrakAdapter = new KontrakAdapter(this, kontrakList);
        recyclerView.setAdapter(kontrakAdapter);
        showSwipeHint();

// Swipe untuk hapus kontrak
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // tidak ada drag & drop
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                KontrakModel kontrak = kontrakList.get(position);

                // Hapus dari server
                deleteSavedContract(kontrak.getId(), position);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        loadSavedContracts();
    }
    private void showSwipeHint() {
        // Inflate layout overlay
        final View hintView = getLayoutInflater().inflate(R.layout.swipe_hint, recyclerView, false);

        // Tambahkan di atas RecyclerView
        addContentView(hintView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView ivArrow = hintView.findViewById(R.id.ivArrow);

        // Animasi panah bergeser ke kanan dan kembali
        ivArrow.animate()
                .translationX(50f)
                .setDuration(500)
                .withEndAction(() -> ivArrow.animate()
                        .translationX(0f)
                        .setDuration(500)
                        .start())
                .start();

        // Hilangkan overlay setelah 3 detik
        hintView.postDelayed(() -> ((ViewGroup) hintView.getParent()).removeView(hintView), 3000);
    }

    private void loadSavedContracts() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        String requestUrl = BASE_URL + "?user_id=" + userId;

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat kontrak yang disimpan...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                response -> {
                    progressDialog.dismiss();
                    parseKontrakData(response);
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
    private void deleteSavedContract(String contractId, int position) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (userId == -1) return;

        String url = ApiConfig.BASE_URL + "delete_saved_contract.php"; // endpoint untuk hapus

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            kontrakList.remove(position);
                            kontrakAdapter.notifyItemRemoved(position);
                            Toast.makeText(KontrakSimpanActivity.this, "Kontrak dihapus", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(KontrakSimpanActivity.this, "Gagal menghapus kontrak", Toast.LENGTH_SHORT).show();
                            kontrakAdapter.notifyItemChanged(position); // kembalikan item
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        kontrakAdapter.notifyItemChanged(position); // kembalikan item
                    }
                },
                error -> {
                    error.printStackTrace();
                    kontrakAdapter.notifyItemChanged(position); // kembalikan item
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("contract_id", contractId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private void parseKontrakData(JSONArray response) {
        try {
            kontrakList.clear();

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

                kontrakList.add(kontrak);
            }

            kontrakAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Toast.makeText(this, "Terjadi kesalahan saat parsing data JSON", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
