package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.KontrakAdapter;
import com.example.contractfarmingapp.models.KontrakModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class KontrakFragment extends Fragment {

    private RecyclerView recyclerView;
    private KontrakAdapter kontrakAdapter;
    private ArrayList<KontrakModel> kontrakList;
    private ProgressDialog progressDialog;
    private TextView tvTanggalSaatIni;
    private EditText etWaktuDibutuhkan, etJarakHari;

    private CardView cardUsahaTani, cardProgresKontrak, cardIntro, cardDeteksiPadi;

    private static final String BASE_URL = ApiConfig.BASE_URL + "kontrak_data.php";

    public KontrakFragment() {
        // Konstruktor kosong
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_kontrak, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView & adapter
        recyclerView = view.findViewById(R.id.recyclerViewKontrak);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        kontrakList = new ArrayList<>();
        kontrakAdapter = new KontrakAdapter(getContext(), kontrakList);
        recyclerView.setAdapter(kontrakAdapter);

        // CardViews (sudah ada)
        cardIntro = view.findViewById(R.id.cardIntro);
        cardIntro.setOnClickListener(v -> startActivity(new Intent(getActivity(), IntroActivity.class)));
        cardUsahaTani = view.findViewById(R.id.cardUsahaTani);

        cardProgresKontrak = view.findViewById(R.id.cardProgresKontrak);
        cardProgresKontrak.setOnClickListener(v -> startActivity(new Intent(getActivity(), RiwayatKontrakActivity.class)));



        // BottomFilter
        etWaktuDibutuhkan = view.findViewById(R.id.etWaktuDibutuhkan);
        etJarakHari = view.findViewById(R.id.etJarakHari);
        tvTanggalSaatIni = view.findViewById(R.id.tvTanggalSaatIni);

        // Set tanggal saat ini
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        tvTanggalSaatIni.setText(sdf.format(new java.util.Date()));

        // Tambahkan listener filter saat user mengetik atau selesai input
        etWaktuDibutuhkan.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilter();
        });
        etJarakHari.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilter();
        });
        etWaktuDibutuhkan.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            int year = c.get(java.util.Calendar.YEAR);
            int month = c.get(java.util.Calendar.MONTH);
            int day = c.get(java.util.Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    getContext(),
                    (vieww, selectedYear, selectedMonth, selectedDay) -> {
                        // Format tanggal menjadi yyyy-MM-dd
                        selectedMonth += 1; // Month dimulai dari 0
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay);
                        etWaktuDibutuhkan.setText(formattedDate);

                        // Terapkan filter otomatis setelah memilih tanggal
                        applyFilter();
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        // Load data
        loadKontrakData();
        loadActiveContracts();
    }
    private void applyFilter() {
        String waktuDibutuhkanStr = etWaktuDibutuhkan.getText().toString().trim();
        String jarakHariStr = etJarakHari.getText().toString().trim();

        int jarakHari;
        if (!jarakHariStr.isEmpty()) {
            jarakHari = Integer.parseInt(jarakHariStr);
        } else {
            jarakHari = 1; // default 1 hari
        }

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        java.util.Date tanggalSekarang = new java.util.Date();

        ArrayList<KontrakModel> filteredList = new ArrayList<>();

        for (KontrakModel kontrak : kontrakList) {
            try {
                java.util.Date kontrakTanggal = sdf.parse(kontrak.getWaktuDibutuhkan());
                long diffMillis = kontrakTanggal.getTime() - tanggalSekarang.getTime();
                long diffDays = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMillis);

                boolean matchesTanggal = waktuDibutuhkanStr.isEmpty() || kontrak.getWaktuDibutuhkan().equals(waktuDibutuhkanStr);
                boolean matchesJarak = (jarakHari == 0 ? true : (diffDays >= jarakHari && diffDays <= 365));

                if (matchesTanggal && matchesJarak) {
                    filteredList.add(kontrak);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        kontrakAdapter.updateList(filteredList);
    }


    private void loadKontrakData() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id",null);
        int userId = Integer.parseInt(userIdStr);
        if (userId == -1) {
            Toast.makeText(getContext(), "User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        String requestUrl = BASE_URL + "?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, requestUrl, null,
                response -> parseKontrakData(response),
                error -> Toast.makeText(getContext(), "Gagal memuat data: " + error.getMessage(), Toast.LENGTH_LONG).show()
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void loadActiveContracts() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id",null);
        int userId = Integer.parseInt(userIdStr);
        if (userId == -1) {
            Toast.makeText(getContext(), "User ID tidak ditemukan. Silakan login ulang.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "get_active_contracts.php?user_id=" + userId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Ambil data dari JSON
                        int totalContracts = response.getInt("totalContracts");
                        int totalCompleted = response.getInt("totalCompleted");

                        // Update TextView
                        TextView tvActive = requireView().findViewById(R.id.tvActiveContracts);
                        TextView tvComplete = requireView().findViewById(R.id.tvCompletedContracts);

                        tvActive.setText(String.valueOf(totalContracts));
                        tvComplete.setText(String.valueOf(totalCompleted));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(getContext(), "Gagal memuat kontrak", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
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
            applyFilter();
        } catch (JSONException e) {
            Toast.makeText(getContext(), "Terjadi kesalahan saat parsing data JSON", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
