package com.example.contractfarmingapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.app.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.example.contractfarmingapp.adapters.ContractAdapter;
import com.example.contractfarmingapp.adapters.UlasanAdapter;
import com.example.contractfarmingapp.models.ContractModel;
import com.example.contractfarmingapp.models.UlasanModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PerusahaanFragment extends Fragment {

    private ImageView companyLogo;
    private TextView companyNomorSk, companyContractsPaid;

    private TextView companyName, companyJenis, companyPeran, companyId, companyDescription, companyWebsite, companyLocation, companyKoordinat,
            companyCertificate, companySocialMedia, companyRating, employeeCount, logistikCount, tractorCount, luasLahan, tvUlasanTitle;
    private ImageButton btnTambahKontrak, btnDashboard, btnPesanAdmin, btnEditCompany, btnDaftarKaryawan, btnLahan, btnDaftarTraktor, btnDaftarLogistik;
    private LinearLayout layoutAdmin, layoutPesan, layoutContracts, layoutBuatToko, layoutDaftarKaryawan, layoutLahan, layoutDaftarLogistik, layoutDaftarTraktor;
    private Button btnBuatToko, companySkPerusahaan, btnKeluarPoktan, btnGabungPoktan,btnPeta, btnPetaPetani;
    private String currentCompanyId;
    private String fasilitatorId;
    private String currentUserId;
    private String currentLokasi;
    private String currentCompanyName;
    private String currentPeran;
    private String currentRating;
    private String currentLogoUrl;
    private RecyclerView recyclerViewContracts;
    private ProgressDialog progressDialog;
    private List<ContractModel> contractList = new ArrayList<>();
    private ContractAdapter contractAdapter;
    private RecyclerView rvUlasan;
    private UlasanAdapter ulasanAdapter;
    private List<UlasanModel> ulasanList = new ArrayList<>();
    private Handler autoScrollHandler = new Handler();
    private int currentPosition = 0;

    // Ganti URL ini dengan alamat server lokal kamu
    private static final String URL_PROFILE = ApiConfig.BASE_URL + "get_profil_company.php";

    public PerusahaanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perusahaan, container, false);

        // Inisialisasi View
        layoutBuatToko = view.findViewById(R.id.layoutBuatToko);
        layoutLahan = view.findViewById(R.id.layoutLahan);
        layoutContracts = view.findViewById(R.id.layoutContracts);
        layoutAdmin = view.findViewById(R.id.layoutAdmin);
        layoutPesan = view.findViewById(R.id.layoutPesan);
        layoutDaftarLogistik = view.findViewById(R.id.layoutDaftarLogistik);
        layoutDaftarKaryawan = view.findViewById(R.id.layoutDaftarKaryawan);
        layoutDaftarTraktor = view.findViewById(R.id.layoutDaftarTraktor);
        btnBuatToko = view.findViewById(R.id.btnBuatToko);
        btnGabungPoktan = view.findViewById(R.id.btnGabungPoktan);
        btnKeluarPoktan = view.findViewById(R.id.btnKeluarPoktan);
        btnLahan = view.findViewById(R.id.btnLahan);
        btnPeta = view.findViewById(R.id.btnPeta);
        btnPesanAdmin = view.findViewById(R.id.btnPesanAdmin);
        companyLogo = view.findViewById(R.id.companyLogo);
        companyName = view.findViewById(R.id.companyName);
        companyRating = view.findViewById(R.id.companyRating);
        companyJenis = view.findViewById(R.id.companyJenis);
        companyId = view.findViewById(R.id.companyId);
        companyPeran = view.findViewById(R.id.companyPeran);

        companyDescription = view.findViewById(R.id.companyDescription);
        companyWebsite = view.findViewById(R.id.companyWebsite);
        companyLocation = view.findViewById(R.id.companyLocation);
        companyKoordinat = view.findViewById(R.id.companyKoordinat);
        companyCertificate = view.findViewById(R.id.companyCertificate);
        companyNomorSk = view.findViewById(R.id.companyNomorSk);
        companySkPerusahaan = view.findViewById(R.id.btnPreviewSk);
        companyContractsPaid = view.findViewById(R.id.companyContractsPaid);

        companySocialMedia = view.findViewById(R.id.companySocialMedia);
        employeeCount = view.findViewById(R.id.employeeCount);
        logistikCount = view.findViewById(R.id.logistikCount);
        tractorCount = view.findViewById(R.id.tractorCount);
        luasLahan = view.findViewById(R.id.luasLahan);
        btnEditCompany = view.findViewById(R.id.btnEditCompany);
        btnPetaPetani = view.findViewById(R.id.btnPetaPetani);
        btnDaftarKaryawan = view.findViewById(R.id.btnDaftarKaryawan);
        btnDaftarTraktor = view.findViewById(R.id.btnDaftarTraktor);
        btnDaftarLogistik = view.findViewById(R.id.btnDaftarLogistik);
        btnDashboard = view.findViewById(R.id.btnAdmin);
        btnTambahKontrak = view.findViewById(R.id.btnTambahKontrak);
        recyclerViewContracts = view.findViewById(R.id.recyclerViewContracts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewContracts.setLayoutManager(layoutManager);

        contractAdapter = new ContractAdapter(getContext(), contractList);
        recyclerViewContracts.setAdapter(contractAdapter);
        rvUlasan = view.findViewById(R.id.rvUlasan);
        tvUlasanTitle = view.findViewById(R.id.tvUlasanTitle);
        LinearLayoutManager layoutManagerUlasan = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvUlasan.setLayoutManager(layoutManagerUlasan);

        ulasanAdapter = new UlasanAdapter(ulasanList);
        rvUlasan.setAdapter(ulasanAdapter);

// Auto-scroll
        autoScrollHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ulasanList.size() > 0) {
                    if (currentPosition == ulasanList.size()) {
                        currentPosition = 0; // reset ke awal
                    }
                    rvUlasan.smoothScrollToPosition(currentPosition++);
                    autoScrollHandler.postDelayed(this, 3000); // scroll setiap 3 detik
                }
            }
        }, 3000);
        // Load Data
        loadCompanyProfile();

        // Aksi tombol edit
        btnEditCompany.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditTokoActivity.class);
            startActivity(intent);
            // startActivity(new Intent(getContext(), EditCompanyActivity.class));
        });
        btnBuatToko.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), BuatTokoActivity.class);
            startActivity(intent);
        });
        btnPeta.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapActivity.class);
            intent.putExtra("company_id", currentCompanyId);
            intent.putExtra("company_name", currentCompanyName);
            startActivity(intent);
        });
        btnPetaPetani.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapPetaniAllActivity.class);
            intent.putExtra("company_id", currentCompanyId);
            intent.putExtra("company_name", currentCompanyName);
            startActivity(intent);
        });
        btnGabungPoktan = view.findViewById(R.id.btnGabungPoktan);

// Tombol gabung poktan hanya muncul jika belum punya company
        btnGabungPoktan.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_gabung_poktan, null);
            EditText etIdPoktan = dialogView.findViewById(R.id.etIdPoktan);
            TextView tvNamaPoktan = dialogView.findViewById(R.id.tvNamaPoktan);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Gabung Poktan")
                    .setView(dialogView)
                    .setPositiveButton("Cari", (dialog, which) -> {
                        String idPoktan = etIdPoktan.getText().toString().trim();
                        if (idPoktan.isEmpty()) {
                            Toast.makeText(requireContext(), "Masukkan ID Poktan", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String url = ApiConfig.BASE_URL + "get_poktan_by_id.php?id_poktan=" + idPoktan;

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                                response -> {
                                    try {
                                        if (response.getBoolean("success")) {
                                            String namaPoktan = response.getString("company_name");
                                            String companyId = response.getString("company_id");

                                            // tampilkan nama + ID poktan
                                            tvNamaPoktan.setText("Nama Poktan: " + namaPoktan + "\nID Poktan: " + companyId);

                                            // Tampilkan dialog konfirmasi gabung
                                            new AlertDialog.Builder(requireContext())
                                                    .setTitle("Konfirmasi")
                                                    .setMessage("Gabung dengan poktan: " + namaPoktan + " (ID: " + companyId + ")?")
                                                    .setPositiveButton("Gabung", (d, w) -> {
                                                        gabungPoktan(companyId);
                                                    })
                                                    .setNegativeButton("Batal", null)
                                                    .show();
                                        } else {
                                            Toast.makeText(requireContext(), "Poktan tidak ditemukan", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    Toast.makeText(requireContext(), "Gagal mencari poktan", Toast.LENGTH_SHORT).show();
                                }
                        );

                        Volley.newRequestQueue(requireContext()).add(request);

                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnKeluarPoktan.setOnClickListener(v -> {
            if (currentUserId == null || currentUserId.isEmpty()) {
                Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(requireContext())
                    .setTitle("Konfirmasi")
                    .setMessage("Apakah Anda yakin ingin keluar dari Poktan?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        String url = ApiConfig.BASE_URL + "keluar_poktan.php"; // buat endpoint baru

                        StringRequest request = new StringRequest(Request.Method.POST, url,
                                response -> {
                                    try {
                                        JSONObject json = new JSONObject(response);
                                        boolean success = json.getBoolean("success");
                                        String message = json.getString("message");
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                                        if (success) {
                                            // Reset tampilan kembali ke awal (seolah belum punya company)
                                            layoutBuatToko.setVisibility(View.VISIBLE);
                                            btnGabungPoktan.setVisibility(View.VISIBLE);
                                            layoutLahan.setVisibility(View.GONE);
                                            layoutDaftarKaryawan.setVisibility(View.GONE);
                                            layoutDaftarTraktor.setVisibility(View.GONE);
                                            layoutDaftarLogistik.setVisibility(View.GONE);
                                            layoutContracts.setVisibility(View.GONE);
                                            btnKeluarPoktan.setVisibility(View.GONE);
                                            companyName.setText("-");
                                            companyId.setText("-");
                                            currentCompanyId = null;

                                            // update SharedPreferences
                                            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                            prefs.edit().remove("company_id").apply();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    Toast.makeText(requireContext(), "Gagal keluar poktan", Toast.LENGTH_SHORT).show();
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("user_id", currentUserId);
                                return params;
                            }
                        };

                        Volley.newRequestQueue(requireContext()).add(request);
                    })
                    .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        btnTambahKontrak.setOnClickListener(v -> {


            View formView = inflater.inflate(R.layout.formtambahkontrak, null);

            EditText etNamaPerusahaan = formView.findViewById(R.id.etNamaPerusahaan);
            EditText etLokasi = formView.findViewById(R.id.etLokasi);
            EditText etRating = formView.findViewById(R.id.etRating);
            EditText etKebutuhan = formView.findViewById(R.id.etKebutuhan);
            EditText etJumlahKebutuhan = formView.findViewById(R.id.etJumlahKebutuhan);
            EditText etHargaPerKg = formView.findViewById(R.id.etHargaPerKg);
            EditText etHargaPerKgFinal = formView.findViewById(R.id.etHargaPerKgFinal);
            EditText etHargaB = formView.findViewById(R.id.etHargaB);
            EditText etHargaC = formView.findViewById(R.id.etHargaC);
            EditText etGradeB = formView.findViewById(R.id.etGradeB);
            EditText etGradeC = formView.findViewById(R.id.etGradeC);
            // Batasi input grade maksimum 100
            InputFilter[] gradeFilter = new InputFilter[]{
                    (source, start, end, dest, dstart, dend) -> {
                        try {
                            String newVal = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());
                            if (newVal.isEmpty()) return null;
                            int value = Integer.parseInt(newVal);
                            if (value >= 0 && value <= 100) return null; // valid
                        } catch (NumberFormatException ignored) {}
                        return ""; // tolak input jika lebih dari 100
                    }
            };

            etGradeB.setFilters(gradeFilter);
            etGradeC.setFilters(gradeFilter);

            EditText etWaktuDibutuhkan = formView.findViewById(R.id.etWaktuDibutuhkan);
            etWaktuDibutuhkan.setFocusable(false);
            etWaktuDibutuhkan.setOnClickListener(dummy -> {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        (view1, selectedYear, selectedMonth, selectedDay) -> {
                            // Format tanggal: YYYY-MM-DD
                            String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                            etWaktuDibutuhkan.setText(selectedDate);
                        },
                        year, month, day
                );
                datePickerDialog.show();
            });

            EditText etJumlahKontrak = formView.findViewById(R.id.etJumlahKontrak);
            EditText etPersyaratan = formView.findViewById(R.id.etPersyaratan);
            EditText etDeskripsi = formView.findViewById(R.id.etDeskripsi);
            EditText etLogoUrl = formView.findViewById(R.id.etLogoUrl);
            Spinner spinnerSatuan = formView.findViewById(R.id.spinnerSatuan);;
            // Inisialisasi adapter dari resources
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    requireContext(),
                    R.array.satuan, // array di res/values/arrays.xml
                    R.layout.spinner_item_3 // layout custom spinner
            );

// Set dropdown view bawaan Android
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Pasang adapter ke spinner
            spinnerSatuan.setAdapter(adapter);
            etLogoUrl.setText(currentLogoUrl);
            etNamaPerusahaan.setText(currentCompanyName);
            etRating.setText(currentRating);
            etLokasi.setText(currentLokasi);
            // Tambahkan listener otomatis untuk menghitung harga final dan grade
            TextWatcher hargaWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double hargaPerKg = Double.parseDouble(etHargaPerKg.getText().toString());
                        double gradeB = etGradeB.getText().toString().isEmpty() ? 0 : Double.parseDouble(etGradeB.getText().toString());
                        double gradeC = etGradeC.getText().toString().isEmpty() ? 0 : Double.parseDouble(etGradeC.getText().toString());

                        // Potongan tetap 0.5% untuk harga final
                        double hargaFinal = hargaPerKg + (hargaPerKg * 0.5 / 100);
                        etHargaPerKgFinal.setText(String.format(Locale.getDefault(), "%.0f", hargaFinal));

                        // Hitung harga grade B dan C
                        double hargaB = hargaFinal - (hargaFinal * gradeB / 100);
                        double hargaC = hargaFinal - (hargaFinal * gradeC / 100);

                        etHargaB.setText(String.format(Locale.getDefault(), "%.0f", hargaB));
                        etHargaC.setText(String.format(Locale.getDefault(), "%.0f", hargaC));
                    } catch (NumberFormatException e) {
                        // Kosongkan kalau belum lengkap
                        etHargaPerKgFinal.setText("");
                        etHargaB.setText("");
                        etHargaC.setText("");
                    }
                }
            };

// Pasang watcher ke tiga input penting
            etHargaPerKg.addTextChangedListener(hargaWatcher);
            etGradeB.addTextChangedListener(hargaWatcher);
            etGradeC.addTextChangedListener(hargaWatcher);

            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Tambah Data Kontrak Offtaker")
                    .setView(formView)
                    .setPositiveButton("Simpan", (dialog, which) -> {
                        String namaPerusahaan = etNamaPerusahaan.getText().toString().trim();
                        String lokasi = etLokasi.getText().toString().trim();
                        String rating = etRating.getText().toString().trim();
                        String kebutuhan = etKebutuhan.getText().toString().trim();
                        String jumlahKebutuhan = etJumlahKebutuhan.getText().toString().trim();
                        String selectedSatuan = spinnerSatuan.getSelectedItem().toString();
                        String hargaPerKg = etHargaPerKg.getText().toString().trim();
                        String gradeB = etGradeB.getText().toString().trim();
                        String gradeC = etGradeC.getText().toString().trim();
                        String waktuDibutuhkan = etWaktuDibutuhkan.getText().toString().trim();
                        String jumlahKontrak = etJumlahKontrak.getText().toString().trim();
                        String persyaratan = etPersyaratan.getText().toString().trim();
                        String deskripsi = etDeskripsi.getText().toString().trim();
                        String logoUrl = etLogoUrl.getText().toString().trim();

                        // Validasi wajib
                        if (namaPerusahaan.isEmpty() || kebutuhan.isEmpty() || jumlahKebutuhan.isEmpty() || hargaPerKg.isEmpty() || gradeB.isEmpty() || gradeC.isEmpty() || persyaratan.isEmpty() || deskripsi.isEmpty() || waktuDibutuhkan.isEmpty()) {
                            Toast.makeText(requireContext(), "Isi semua", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String url = ApiConfig.BASE_URL + "insert_kontrak.php";

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                response -> {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        boolean success = jsonObject.getBoolean("success");
                                        String message = jsonObject.getString("message");
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show();
                                    }
                                },
                                error -> {
                                    error.printStackTrace();
                                    Toast.makeText(requireContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                                }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<>();
                                params.put("oftaker_id", currentCompanyId); // bisa pakai Firebase UID atau ID user
                                params.put("namaPerusahaan", namaPerusahaan);
                                params.put("lokasi", lokasi);
                                params.put("rating", rating);
                                params.put("kebutuhan", kebutuhan);
                                params.put("jumlahKebutuhan", jumlahKebutuhan);
                                params.put("satuan", selectedSatuan);
                                params.put("hargaPerKg", hargaPerKg);
                                params.put("gradeB", gradeB);
                                params.put("gradeC", gradeC);
                                params.put("waktuDibutuhkan", waktuDibutuhkan);
                                params.put("jumlahKontrak", jumlahKontrak);
                                params.put("persyaratan", persyaratan);
                                params.put("deskripsi", deskripsi);
                                params.put("logoUrl", logoUrl);
                                return params;
                            }
                        };

                        Volley.newRequestQueue(requireContext()).add(stringRequest);


                        Toast.makeText(requireContext(), "Data Kontrak berhasil disiapkan", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        btnDaftarKaryawan.setOnClickListener(v -> {
            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                String jenis = companyJenis.getText().toString().trim(); // ambil jenis usaha

                Intent intent;
                if ("Fasilitator".equalsIgnoreCase(jenis)) {
                    // Jika jenis usaha fasilitator, buka activity khusus
                    intent = new Intent(requireContext(), DaftarAnggotaFasilitatorActivity.class);
                } else {
                    // Default: buka daftar anggota biasa
                    intent = new Intent(requireContext(), DaftarAnggotaActivity.class);
                }

                intent.putExtra("peran", currentPeran);
                intent.putExtra("company_id", currentCompanyId); // Kirim company_id via intent
                startActivity(intent);

            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });

        btnDaftarLogistik.setOnClickListener(v -> {
            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                Intent intent = new Intent(requireContext(), DaftarSopirActivity.class);
                intent.putExtra("peran", currentPeran);
                intent.putExtra("company_id", currentCompanyId); // Kirim company_id via intent
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        btnDaftarTraktor.setOnClickListener(v -> {
            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                Intent intent = new Intent(requireContext(), DaftarTraktorActivity.class);
                intent.putExtra("peran", currentPeran);
                intent.putExtra("company_id", currentCompanyId); // Kirim company_id via intent
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        btnLahan.setOnClickListener(v -> {
            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                Intent intent = new Intent(requireContext(), LahanActivity.class);
                intent.putExtra("peran", currentPeran);
                intent.putExtra("company_id", currentCompanyId);
                intent.putExtra("company_name", currentCompanyName);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        btnDashboard.setOnClickListener(v -> {
            String jenis = companyJenis.getText().toString(); // Ambil teks dari TextView

            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                Intent intent;

                if ("Offtaker".equalsIgnoreCase(jenis)) {
                    intent = new Intent(requireContext(), AdminOfftakerActivity.class);
                } else if ("Kelompok Tani".equalsIgnoreCase(jenis)) {
                    intent = new Intent(requireContext(), AdminPoktanActivity.class);
                } else if ("Fasilitator".equalsIgnoreCase(jenis)) {
                    intent = new Intent(requireContext(), AdminFasilitatorActivity.class);
                } else {
                    Toast.makeText(getContext(), "Jenis perusahaan tidak dikenali", Toast.LENGTH_SHORT).show();
                    return;
                }

                intent.putExtra("peran", currentPeran);
                intent.putExtra("company_id", currentCompanyId);
                intent.putExtra("user_id", currentUserId);
                intent.putExtra("company_name", currentCompanyName);
                startActivity(intent);

            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });
        btnPesanAdmin.setOnClickListener(v -> {
            if (currentCompanyId != null && !currentCompanyId.isEmpty()) {
                Intent intent;

                // Ambil teks dari TextView companyJenis
                String jenis = (companyJenis != null) ? companyJenis.getText().toString() : "";

                if (!jenis.isEmpty() && jenis.equalsIgnoreCase("Offtaker")) {
                    // Jika perusahaan jenis Offtaker, buka DaftarChatPetaniActivity
                    intent = new Intent(requireContext(), DaftarChatPetaniActivity.class);
                    intent.putExtra("company_id", currentCompanyId);

                } else if (currentPeran != null && currentPeran.toLowerCase().contains("admin")) {
                    // Jika peran admin, buka ChatActivityFasilitator
                    intent = new Intent(requireContext(), DaftarChatPoktanPetaniActivity.class);
                    intent.putExtra("fasilitator_id", fasilitatorId); // pastikan fasilitatorId sudah diambil
                    intent.putExtra("peran", currentPeran);
                    intent.putExtra("nama_admin", "Admin Kontrak");
                    intent.putExtra("company_id", currentCompanyId);

                } else {
                    // Default: buka ChatActivityPetani
                    intent = new Intent(requireContext(), ChatActivityPetani.class);
                    intent.putExtra("peran", currentPeran);
                    intent.putExtra("nama_admin", "Admin Kontrak");
                    intent.putExtra("company_id", currentCompanyId);
                }

                startActivity(intent);

            } else {
                Toast.makeText(getContext(), "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        });





        return view;
    }

    private void loadCompanyProfile() {
        // Ambil email dari Firebase user yang login
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Pengguna belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail(); // ambil email pengguna

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Memuat profil organisasi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Kirim parameter email via POST
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL_PROFILE, params,
                response -> {
                    progressDialog.dismiss();

                    try {
                        layoutBuatToko.setVisibility(View.GONE); // Sembunyikan jika data ada
                        btnGabungPoktan.setVisibility(View.GONE); // Sembunyikan jika data ada
                        btnKeluarPoktan.setVisibility(View.VISIBLE); // Sembunyikan jika data ada
                        companyNomorSk.setText("📑 Nomor SK: " + response.optString("nomor_sk", "-"));
                        String skUrl = response.optString("sk_perusahaan", "");
                        companySkPerusahaan.setOnClickListener(v -> {
                            if (skUrl != null && !skUrl.isEmpty()) {
                                try {
                                    // Paksa buka browser, jangan tersangkut deep link app
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(skUrl));
                                    browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(browserIntent);
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Tidak dapat membuka dokumen", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(requireContext(), "Dokumen SK belum tersedia", Toast.LENGTH_SHORT).show();
                            }
                        });


                        companyContractsPaid.setText("✅ Kontrak Terbayar: " + response.optString("contracts_paid", "0"));

                        companyName.setText(response.getString("company_name"));
                        currentCompanyName = response.getString("company_name");
                        currentLokasi= response.getString("location");
                        companyJenis.setText(response.getString("jenis_usaha"));
                        companyId.setText(response.getString("company_id"));
                        currentCompanyId = response.getString("company_id"); // Simpan company_id ke variabel global
                        fasilitatorId = response.getString("fasilitator_id"); // Simpan fasiliatator_id ke variabel global
                        companyId.setText(currentCompanyId);
                        String ratingStr = response.optString("company_rating", "Belum ada rating");
                        companyRating.setText("⭐ Rating: " + ratingStr);
                        currentRating = response.optString("company_rating", "Belum ada rating");
                        currentUserId = response.getString("user_id"); // Simpan user_id ke variabel global
// Simpan user_id ke SharedPreferences
                        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("user_id", currentUserId);
                        editor.putString("company_id", currentCompanyId);
                        editor.putString("company_name", currentCompanyName);
                        editor.putString("lokasi", currentLokasi);
                        editor.putString("fasilitator_id", fasilitatorId);
                        editor.apply(); // Gunakan commit() jika ingin sinkron

                        companyPeran.setText(response.getString("peran"));
                        currentPeran = response.getString("peran");
                        companyDescription.setText(response.getString("description"));
                        companyWebsite.setText("🌐 " + response.getString("website"));
                        companyLocation.setText("🗺️ " + response.getString("location"));

                        companyKoordinat.setText("📍 " + response.getString("coordinate"));
                        companyCertificate.setText("📄 Sertifikasi: " + response.getString("certificate"));
                        companySocialMedia.setText("📱 IG: " + response.getString("social_media"));
                        employeeCount.setText("👥 Jumlah Anggota: " + response.getString("employee_count"));
                        logistikCount.setText("🚚 Jumlah Kendaraan Logistik: " + response.getString("logistik_count"));
                        tractorCount.setText("🚜 Jumlah Traktor: " + response.getString("tractor_count"));
                        luasLahan.setText("🏞️ Luas Lahan: " + response.getString("land_area"));
                        String websiteUrl = response.getString("website");
                        companyWebsite.setText("🌐 " + websiteUrl);

// Biar bisa diklik
                        companyWebsite.setOnClickListener(v -> {
                            if (websiteUrl != null && !websiteUrl.isEmpty()) {
                                String finalUrl = websiteUrl.startsWith("http://") || websiteUrl.startsWith("https://")
                                        ? websiteUrl
                                        : "http://" + websiteUrl; // tambahkan http jika belum ada

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(finalUrl));
                                startActivity(browserIntent);
                            } else {
                                Toast.makeText(requireContext(), "Website tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        });
                        companyWebsite.setTextColor(Color.parseColor("#0D47A1"));
                        companyWebsite.setPaintFlags(companyWebsite.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                        companyWebsite.setClickable(true);

                        String logoUrl = response.getString("logo_url");
                        currentLogoUrl = response.getString("logo_url"); // simpan untuk form nanti

                        if (logoUrl != null && !logoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(logoUrl)
                                    .placeholder(R.drawable.store_icon)
                                    .error(R.drawable.store_icon)
                                    .into(companyLogo);
                        }
                        String companyJenis = response.getString("jenis_usaha");

// Tampilkan layoutContracts hanya jika jenis usaha adalah Offtaker
                        if ("Offtaker".equalsIgnoreCase(companyJenis)) {
                            layoutContracts.setVisibility(View.VISIBLE);
                            rvUlasan.setVisibility(View.VISIBLE);
                        } else {
                            layoutContracts.setVisibility(View.GONE);
                        }


// Logika untuk kelompok tani
                        if ("Kelompok Tani".equalsIgnoreCase(companyJenis)) {
                            layoutDaftarKaryawan.setVisibility(View.VISIBLE);
                            layoutLahan.setVisibility(View.VISIBLE);
                            layoutDaftarTraktor.setVisibility(View.VISIBLE);
                            employeeCount.setVisibility(View.VISIBLE);
                            tractorCount.setVisibility(View.VISIBLE);
                            luasLahan.setVisibility(View.VISIBLE);
                            companyContractsPaid.setVisibility(View.GONE);
                            btnKeluarPoktan.setVisibility(View.VISIBLE);
                            companySkPerusahaan.setVisibility(View.VISIBLE);
                            rvUlasan.setVisibility(View.VISIBLE);
                        } else {
                            layoutDaftarKaryawan.setVisibility(View.GONE);
                            layoutLahan.setVisibility(View.GONE);
                            layoutDaftarTraktor.setVisibility(View.GONE);
                            employeeCount.setVisibility(View.GONE);
                            tractorCount.setVisibility(View.GONE);
                            luasLahan.setVisibility(View.GONE);
                            btnKeluarPoktan.setVisibility(View.GONE);
                            companySkPerusahaan.setVisibility(View.GONE);
                        }

                        if ("Fasilitator".equalsIgnoreCase(companyJenis)) {
                            layoutDaftarKaryawan.setVisibility(View.VISIBLE);
                            layoutLahan.setVisibility(View.VISIBLE);
                            layoutDaftarTraktor.setVisibility(View.VISIBLE);
                            employeeCount.setVisibility(View.VISIBLE);
                            tractorCount.setVisibility(View.GONE);
                            logistikCount.setVisibility(View.GONE);
                            companyRating.setVisibility(View.GONE);
                            companyPeran.setVisibility(View.GONE);
                            luasLahan.setVisibility(View.GONE);
                            companyContractsPaid.setVisibility(View.GONE);
                            tvUlasanTitle.setVisibility(View.GONE);
                            btnKeluarPoktan.setVisibility(View.VISIBLE);
                            companySkPerusahaan.setVisibility(View.VISIBLE);
                        }

                        // Tampilkan tombol edit hanya jika peran = admin
                        String peran = response.getString("peran");
                        if ("admin".equalsIgnoreCase(peran)) {
                            btnEditCompany.setVisibility(View.VISIBLE);
                            layoutAdmin.setVisibility(View.VISIBLE);
                            layoutPesan.setVisibility(View.GONE);
                            btnTambahKontrak.setVisibility(View.VISIBLE); // hanya admin bisa tambah kontrak
                            btnTambahKontrak.setEnabled(true);
                            btnKeluarPoktan.setVisibility(View.GONE);
                            btnPeta.setVisibility(View.VISIBLE);
                            layoutPesan.setVisibility(View.VISIBLE);
                            btnEditCompany.setOnClickListener(v -> {
                                Intent intent = new Intent(requireContext(), EditTokoActivity.class);
                                startActivity(intent);
                            });
                        } else {
                            btnEditCompany.setVisibility(View.GONE);
                            layoutAdmin.setVisibility(View.GONE);
                            layoutPesan.setVisibility(View.VISIBLE);
                            btnTambahKontrak.setVisibility(View.GONE); // sembunyikan dari non-admin
                        }
                        if ("fasilitator".equalsIgnoreCase(peran)) {
                            btnEditCompany.setVisibility(View.VISIBLE);
                            layoutAdmin.setVisibility(View.VISIBLE);
                            layoutPesan.setVisibility(View.GONE);
                            btnPeta.setVisibility(View.VISIBLE);
                            btnTambahKontrak.setVisibility(View.VISIBLE); // hanya admin bisa tambah kontrak
                            btnTambahKontrak.setEnabled(true);
                            companyPeran.setVisibility(View.GONE);
                            btnKeluarPoktan.setVisibility(View.GONE);
                            btnEditCompany.setOnClickListener(v -> {
                                Intent intent = new Intent(requireContext(), EditTokoActivity.class);
                                startActivity(intent);
                            });
                        } else {
                            layoutPesan.setVisibility(View.VISIBLE);
                        }
                        if ("user".equalsIgnoreCase(peran)) {
                            btnPetaPetani.setVisibility(View.VISIBLE);
                            companyPeran.setVisibility(View.VISIBLE);
                            btnKeluarPoktan.setVisibility(View.VISIBLE);
                        }
                        loadContracts();
                        loadUlasan();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        layoutBuatToko.setVisibility(View.VISIBLE);
                        btnGabungPoktan.setVisibility(View.VISIBLE);
                        btnKeluarPoktan.setVisibility(View.GONE);
                        companySkPerusahaan.setVisibility(View.GONE);

                        Toast.makeText(getContext(), "Data perusahaan belum dibuat", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    progressDialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Gagal mengambil data profil perusahaan", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireContext()).add(request);


    }
    private void loadContracts() {
        if (currentCompanyId == null || currentCompanyId.isEmpty()) {
            Toast.makeText(getContext(), "ID perusahaan tidak tersedia untuk memuat kontrak", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "get_kontrak_by_oftaker_id.php?oftaker_id=" + currentCompanyId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray kontrakArray = response.getJSONArray("contracts");
                        contractList.clear();
                        for (int i = 0; i < kontrakArray.length(); i++) {
                            JSONObject obj = kontrakArray.getJSONObject(i);
                            ContractModel contract = new ContractModel(
                                    obj.getString("id"),
                                    obj.getString("oftaker_id"),
                                    obj.getString("namaPerusahaan"),
                                    obj.getString("kebutuhan"),
                                    obj.getString("jumlahKebutuhan"),
                                    obj.getString("satuan"),
                                    obj.getString("hargaPerKg"),
                                    obj.getString("waktuDibutuhkan"),
                                    obj.getString("persyaratan"),
                                    obj.getString("jumlahKontrak"),
                                    obj.getString("timeUpload"),
                                    obj.getString("rating"),
                                    obj.getString("lokasi"),
                                    obj.getString("deskripsi"),
                                    obj.getString("logoUrl"),
                                    obj.getString("gradeB"),
                                    obj.getString("gradeC")
                            );

                            contractList.add(contract);
                        }
                        contractAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Gagal parsing data kontrak", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Gagal memuat kontrak", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadUlasan() {
        String url = ApiConfig.BASE_URL + "get_ulasan_by_company.php?company_id=" + currentCompanyId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ulasanList.clear();
                        JSONArray arr = response.getJSONArray("ulasan");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String nama = obj.getString("nama_perusahaan");
                            String isi = obj.getString("ulasan");
                            float rating = (float) obj.getDouble("rating");
                            ulasanList.add(new UlasanModel(nama, isi, rating));
                        }
                        ulasanAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Gagal parsing ulasan", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Gagal mengambil ulasan", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(getContext()).add(request);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        autoScrollHandler.removeCallbacksAndMessages(null);
    }

    private void gabungPoktan(String companyId) {
        // Ambil user_id dari SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (userId == -1) {
            Toast.makeText(requireContext(), "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "gabung_poktan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean success = json.getBoolean("success");
                        String message = json.getString("message");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                        if (success) {
                            // Refresh tampilan agar update
                            loadCompanyProfile();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Gagal gabung poktan", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId)); // kirim userId dari SharedPreferences
                params.put("company_id", companyId);
                return params;
            }
        };
        Log.d("GabungPoktan", "user_id=" + userId + ", company_id=" + companyId);
        Volley.newRequestQueue(requireContext()).add(request);
    }


}
