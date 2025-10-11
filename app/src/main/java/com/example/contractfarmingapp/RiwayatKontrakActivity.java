package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.activities.ContractDetailActivity;
import com.example.contractfarmingapp.adapters.RiwayatKontrakAdapter;
import com.example.contractfarmingapp.models.RiwayatKontrakModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RiwayatKontrakActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<RiwayatKontrakModel> riwayatList;
    RiwayatKontrakAdapter adapter;
    ProgressDialog progressDialog;

    private static final String DATA_URL = ApiConfig.BASE_URL + "get_riwayat_kontrak.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_kontrak);

        recyclerView = findViewById(R.id.recyclerViewRiwayat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        riwayatList = new ArrayList<>();
        adapter = new RiwayatKontrakAdapter(this, riwayatList);
        recyclerView.setAdapter(adapter);
        adapter.setOnRiwayatKontrakClickListener(data -> {
            Intent intent = new Intent(RiwayatKontrakActivity.this, ContractDetailActivity.class);
            intent.putExtra("contract_id", String.valueOf(data.getContract_id()));
            intent.putExtra("tipe", "petani");
            startActivity(intent);
        });
        adapter.setOnChatClickListener(data -> {
            Intent intent = new Intent(RiwayatKontrakActivity.this, ChatActivityPetaniOfftaker.class);
            intent.putExtra("nama_perusahaan", data.getNamaPerusahaan());
            intent.putExtra("oftaker_id", data.getOftaker_id());
            startActivity(intent);
        });
        adapter.setOnCetakClickListener(data -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String namaPengguna = prefs.getString("nama_pengguna", "");
            String namaPerusahaan = data.getNamaPerusahaan();

            Intent intent = new Intent(RiwayatKontrakActivity.this, FormKontrakActivity.class);
            intent.putExtra("user_id", prefs.getInt("id", -1));
            intent.putExtra("nama_pengguna", namaPengguna);
            intent.putExtra("nama_perusahaan", namaPerusahaan);
            intent.putExtra("jumlah_kebutuhan", data.getJumlahKebutuhan());
            intent.putExtra("kebutuhan", data.getKebutuhan());
            intent.putExtra("satuan", data.getSatuan());
            intent.putExtra("lahan", data.getLahan());
            intent.putExtra("status_lahan", data.getStatuslahan());
            intent.putExtra("waktu_dibutuhkan", data.getWaktuDibutuhkan());
            intent.putExtra("contract_id", data.getContract_id());
            intent.putExtra("tanggal_pengajuan", data.getTanggal());
            intent.putExtra("catatan", data.getCatatan());
            String asuransi = (data.getIkutAsuransi() != null && data.getIkutAsuransi().equals("1")) ? "Ya" : "Tidak";
            intent.putExtra("ikut_asuransi", asuransi);
            startActivity(intent);
        });
        adapter.setOnUploadBuktiClickListener(data -> {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String namaPetani = prefs.getString("nama_pengguna", "");
            String namaPerusahaan = data.getNamaPerusahaan();
            String namaPoktan = prefs.getString("nama_poktan", "");
            int idPoktan = prefs.getInt("company_id", -1);
            String lokasiPoktan = prefs.getString("lokasi_poktan", "");
            Intent intent = new Intent(RiwayatKontrakActivity.this, ExportReportActivity.class);
            intent.putExtra("nama_petani", namaPetani);
            intent.putExtra("nama_perusahaan", namaPerusahaan);
            intent.putExtra("nama_poktan", namaPoktan);
            intent.putExtra("id_poktan", idPoktan);
            intent.putExtra("lokasi_poktan", lokasiPoktan);
            intent.putExtra("contract_id", data.getContract_id());
            intent.putExtra("kebutuhan", data.getKebutuhan() + " (" + data.getJumlahKebutuhan() + " kg)");
            intent.putExtra("lahan", data.getLahan());

            startActivity(intent);
        });


        loadRiwayatKontrak();
    }

    private void loadRiwayatKontrak() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String namaPengguna = prefs.getString("nama_pengguna", "");
        int userId = prefs.getInt("id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Memuat riwayat kontrak...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String url = DATA_URL + "?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    progressDialog.dismiss();
                    try {
                        riwayatList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            RiwayatKontrakModel item = new RiwayatKontrakModel(
                                    obj.getString("contract_id"),
                                    obj.getString("oftaker_id"),
                                    obj.getString("kebutuhan"),
                                    obj.getString("jumlahKebutuhan"),
                                    obj.getString("satuan"),
                                    obj.getString("namaPerusahaan"),
                                    obj.getString("waktuDibutuhkan"),
                                    obj.getString("lahan"),
                                    obj.getString("statuslahan"),
                                    obj.getString("status"),
                                    obj.getString("catatan"),
                                    obj.getString("tanggal_ajukan"),
                                    obj.getString("ikut_asuransi")
                            );
                            riwayatList.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Gagal parsing data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
