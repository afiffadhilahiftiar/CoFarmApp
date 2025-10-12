package com.example.contractfarmingapp;



import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.activities.ContractDetailActivity;


import com.example.contractfarmingapp.adapters.PetaniAdapterFasilitator;
import com.example.contractfarmingapp.models.Petani;
import com.example.contractfarmingapp.models.PetaniFasilitator;
import com.example.contractfarmingapp.network.ApiClient;
import com.example.contractfarmingapp.network.UploadService;
import com.example.contractfarmingapp.network.UploadServiceKlaim;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AdminFasilitatorActivity extends AppCompatActivity implements PetaniAdapterFasilitator.OnPetaniActionListener {

    private TextView totalKontrak, totalPetani, totalPanen, statusKontrak;
    private RecyclerView recyclerView;
    private PetaniAdapterFasilitator adapter;
    private List<PetaniFasilitator> listPetani;
    private Button btnUploadPanduan;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ImageView imgPreview;
    private String API_URL = ""; // Awalnya kosong, nanti akan diisi di onCreate


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_fasilitator);

        // Ambil company_id dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int companyId = sharedPreferences.getInt("company_id", -1);

        if (companyId == -1) {
            Toast.makeText(this, "Company ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi URL dengan companyId valid
        API_URL = ApiConfig.BASE_URL + "get_petani_by_fasilitator.php?company_id=" + companyId;


        // Inisialisasi view
        totalKontrak = findViewById(R.id.txtTotalKontrak);
        totalPetani = findViewById(R.id.txtTotalPetani);
        totalPanen = findViewById(R.id.txtTotalPanen);
        statusKontrak = findViewById(R.id.txtStatusKontrak);

        recyclerView = findViewById(R.id.recyclerPetani);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listPetani = new ArrayList<>();
        adapter = new PetaniAdapterFasilitator(listPetani, this);
        recyclerView.setAdapter(adapter);

        // Ambil data petani dari server
        loadDataPetaniFromAPI();

    }

    private void loadDataPetaniFromAPI() {
        // Log URL untuk memastikan company_id benar
        android.util.Log.d("AdminFasilitator", "API_URL: " + API_URL);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    android.util.Log.d("AdminFasilitator", "Response JSON: " + response.toString());

                    listPetani.clear();

                    if (response.length() == 0) {
                        android.util.Log.d("AdminFasilitator", "Data petani kosong!");
                        Toast.makeText(this, "Tidak ada data petani ditemukan", Toast.LENGTH_SHORT).show();
                    }

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // Gunakan optXXX agar tidak crash jika null
                            int id = obj.optInt("id", 0);
                            int userId = obj.optInt("user_id", 0);
                            String nama = obj.optString("nama", "-");
                            String harga = obj.optString("harga", "0");
                            String lahan = obj.optString("lahan", "-");
                            String progres = obj.optString("progres", "0");
                            String catatan = obj.optString("catatan", "-");
                            String companyName = obj.optString("company_name", "-");
                            String poktanName = obj.optString("poktan_name", "-");
                            int companyId = obj.optInt("company_id", 0);
                            int contractId = obj.optInt("contract_id", 0);
                            int oftakerId = obj.optInt("oftaker_id", 0);
                            String statusLahan = obj.optString("statusLahan", "-");
                            String status = obj.optString("status", "-");
                            String kebutuhan = obj.optString("kebutuhan", "-");
                            String jumlahKebutuhan = obj.optString("jumlah_kebutuhan", "0");
                            String satuan = obj.optString("satuan", "-");
                            String waktuDibutuhkan = obj.optString("waktu_dibutuhkan", "-");
                            String ikutAsuransi = obj.optString("ikut_asuransi", "Tidak");
                            String tanggalAjukan = obj.optString("tanggal_ajukan", "-");
                            String jumlahKlaim = obj.optString("jumlah_klaim", "0");
                            String statusKlaim = obj.optString("status_klaim", "-");
                            String catatanKlaim= obj.optString("catatan_klaim", "-");
                            PetaniFasilitator p = new PetaniFasilitator(
                                    id, userId, nama, harga, lahan, progres, catatan,
                                    companyName, poktanName, companyId, contractId, oftakerId, status, statusLahan,
                                    kebutuhan, satuan, waktuDibutuhkan, ikutAsuransi, jumlahKebutuhan, tanggalAjukan, jumlahKlaim, statusKlaim, catatanKlaim
                            );

                            listPetani.add(p);


                            // Opsional: simpan field baru di map / class tambahan jika Petani tidak diubah
                            // p.setKebutuhan(kebutuhan); // kalau sudah ada setter
                            // p.setJumlahKebutuhan(jumlahKebutuhan);
                            // p.setSatuan(satuan);
                            // p.setWaktuDibutuhkan(waktuDibutuhkan);
                            // p.setIkutAsuransi(ikutAsuransi);

                        } catch (JSONException e) {
                            android.util.Log.e("AdminFasilitator", "JSON parsing error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();
                    loadDashboardInfo();
                },
                error -> {
                    android.util.Log.e("AdminFasilitator", "Volley error: " + error.getMessage());
                    Toast.makeText(this, "Gagal mengambil data petani", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }


    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri); // atau simpan preview ke variabel agar bisa digunakan nanti
        }
    }
    private void loadDashboardInfo() {
        totalKontrak.setText("Kontrak Aktif: " + listPetani.size());
        totalPetani.setText("Total Petani: " + listPetani.size());
        totalPanen.setText("Panen Terkumpul: - kg"); // Ganti dengan API jika tersedia
        statusKontrak.setText("Status: Berjalan");
    }
    private void kirimStatusValidasi(int userId, int contractId, String status, String catatan) {
        String url = ApiConfig.BASE_URL + "update_status_kontrak.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Status berhasil dikirim: " + status, Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < listPetani.size(); i++) {
                        PetaniFasilitator p = listPetani.get(i);
                        if (p.user_id == userId && p.contract_id == contractId) {
                            p.status = status;
                            p.catatan = catatan; // simpan catatan jika kamu ingin ditampilkan
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> Toast.makeText(this, "Gagal kirim status: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("contract_id", String.valueOf(contractId));
                params.put("status", status);
                params.put("catatan", catatan);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onValidasiClick(PetaniFasilitator petani) {
        // Inflate layout dialog_validasi
        View view = getLayoutInflater().inflate(R.layout.dialog_validasi, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validasi Kontrak");
        builder.setView(view);

        builder.setPositiveButton("Terima", null); // Kita override di bawah
        builder.setNegativeButton("Tolak", null); // override setelah show()


        AlertDialog dialog = builder.create();
        dialog.show();
// Tangani tombol Tolak (Negative) setelah dialog ditampilkan
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            EditText editCatatan = view.findViewById(R.id.editCatatan);
            String catatan = editCatatan.getText().toString().trim();

            if (catatan.isEmpty()) {
                Toast.makeText(this, "Harap isi alasan penolakan!", Toast.LENGTH_SHORT).show();
                return;
            }

            kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak ditolak fasilitator", catatan);
            dialog.dismiss();
        });

        // Ambil semua checkbox
        List<CheckBox> checkBoxList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("check" + i, "id", getPackageName());
            CheckBox checkBox = view.findViewById(resId);
            if (checkBox != null) checkBoxList.add(checkBox);
        }

        // Override tombol positif setelah dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean semuaCentang = true;
            for (CheckBox cb : checkBoxList) {
                if (!cb.isChecked()) {
                    semuaCentang = false;
                    break;
                }
            }

            if (semuaCentang) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak divalidasi fasilitator", ""); // catatan kosong

                dialog.dismiss();
            } else {
                Toast.makeText(this, "Harap centang semua poin validasi!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onValidasiKlaimClick(PetaniFasilitator petani) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_validasi_klaim, null);

        CheckBox checkVerifikasi = dialogView.findViewById(R.id.checkboxVerifikasi);
        CheckBox checkVerifikasi2 = dialogView.findViewById(R.id.checkboxVerifikasi2);
        CheckBox checkVerifikasi3 = dialogView.findViewById(R.id.checkboxVerifikasi3);
        EditText etCatatan = dialogView.findViewById(R.id.etCatatan);
        Button btnUploadBukti = dialogView.findViewById(R.id.btnUploadBukti);
        Button btnBayar = dialogView.findViewById(R.id.btnBayar);
        Button btnTolak = dialogView.findViewById(R.id.btnTolak);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBarBayar);
        imgPreview = dialogView.findViewById(R.id.imgPreview);
        btnUploadBukti.setOnClickListener(v -> pickImageFromGallery());
        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("Validasi Klaim Dana")
                .setPositiveButton("Kirim", (dialog, which) -> {
                    if (!checkVerifikasi.isChecked() && checkVerifikasi2.isChecked() && checkVerifikasi3.isChecked()) {
                        Toast.makeText(this, "Centang verifikasi klaim terlebih dahulu", Toast.LENGTH_SHORT).show();
                        return;

                    }


                    String catatan = etCatatan.getText().toString().trim();

                    uploadBuktiKlaim(petani, selectedImageUri, catatan);
                    updateStatusKlaim(petani.id, "Dibayar", catatan);
                })
                .setView(dialogView)
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        builder.show();

        // Tombol Upload Bukti Pembayaran


        // Tombol BAYAR
        btnBayar.setOnClickListener(v -> {
            if (!checkVerifikasi.isChecked()) {
                Toast.makeText(this, "Centang verifikasi klaim terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }


            String catatan = etCatatan.getText().toString().trim();

            int jumlahKlaim;
            try {
                jumlahKlaim = Integer.parseInt(petani.jumlahKlaim.replace(".", ""));
            } catch (Exception e) {
                Toast.makeText(this, "Jumlah klaim tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Upload bukti bayar ke server


            // Buat invoice pembayaran klaim
            createInvoicePetani(jumlahKlaim, petani, progressBar);

            // Update status klaim ke "Dibayar"


            builder.dismiss();
        });

        // Tombol TOLAK
        btnTolak.setOnClickListener(v -> {
            if (!checkVerifikasi.isChecked()) {
                Toast.makeText(this, "Centang verifikasi klaim terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            String catatan = etCatatan.getText().toString().trim();
            if (catatan.isEmpty()) {
                Toast.makeText(this, "Berikan alasan penolakan di kolom catatan", Toast.LENGTH_SHORT).show();
                return;
            }

            updateStatusKlaim(petani.id, "Ditolak", catatan);
            builder.dismiss();
        });
    }
    private void createInvoicePetani(int amount, PetaniFasilitator petani, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null) ? user.getEmail() : null;

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email pengguna tidak tersedia", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (amount < 1) {
            Toast.makeText(this, "Jumlah pembayaran tidak valid", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String url = ApiConfig.BASE_URL + "create_invoice_klaim.php";

        new Thread(() -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                String postData = "amount=" + petani.jumlahKlaim
                        + "&email=" + URLEncoder.encode(email, "UTF-8")
                        + "&contract_id=" + petani.contract_id
                        + "&user_id=" + petani.user_id;

                OutputStream os = con.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                String invoiceUrl = jsonResponse.getString("invoice_url");
                String externalId = jsonResponse.getString("external_id");

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra("invoice_url", invoiceUrl);
                    intent.putExtra("external_id", externalId);
                    intent.putExtra("harga", petani.jumlahKlaim);
                    intent.putExtra("payment_method", "Xendit");
                    startActivity(intent);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal membuat invoice", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
    private void uploadBuktiKlaim(PetaniFasilitator petani, Uri imageUri, String catatan) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUserId = prefs.getString("company_id", null); // Ini ID dari pengguna yang login (admin offtaker)
        File file = new File(getPathFromUri(imageUri));
        RequestBody requestFile = RequestBody.create(file, okhttp3.MediaType.parse("image/*"));
        MultipartBody.Part fotoPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

        RequestBody petaniId = RequestBody.create(String.valueOf(petani.id), okhttp3.MediaType.parse("text/plain"));
        RequestBody userId = RequestBody.create(loggedInUserId, okhttp3.MediaType.parse("text/plain")); // Diganti ke loggedInUserId
        RequestBody companyIdBody = RequestBody.create(String.valueOf(petani.companyId), okhttp3.MediaType.parse("text/plain"));
        RequestBody catatanBody = RequestBody.create(catatan, okhttp3.MediaType.parse("text/plain"));

        UploadServiceKlaim service = ApiClient.getClient().create(UploadServiceKlaim.class);
        Call<ResponseBody> call = service.uploadBuktiKlaim(petaniId, userId, companyIdBody, catatanBody, fotoPart);

        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Bukti pembayaran berhasil diunggah", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Upload gagal: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void updateStatusKlaim(int petaniId, String statusKlaim, String catatanKlaim) {
        String url = ApiConfig.BASE_URL + "update_status_klaim.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(this, "Status klaim diperbarui: " + statusKlaim, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal memperbarui klaim", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Respon tidak valid", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Koneksi server gagal", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("petani_id", String.valueOf (petaniId));
                params.put("status_klaim", statusKlaim);
                params.put("catatan_klaim", catatanKlaim);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Tambahkan method baru di AdminPoktanActivity
    public void onKlaimDanaClick(PetaniFasilitator petani) {
        // Inflate layout klaim dana
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_klaim_dana, null);
        EditText editJumlah = dialogView.findViewById(R.id.editJumlahDana);
        EditText editCatatan = dialogView.findViewById(R.id.editCatatanDana);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Klaim Dana")
                .setView(dialogView)
                .setPositiveButton("Kirim", null) // override nanti
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // Override tombol Kirim
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String jumlahStr = editJumlah.getText().toString().trim();
            String catatan = editCatatan.getText().toString().trim();

            if (jumlahStr.isEmpty()) {
                Toast.makeText(this, "Harap masukkan jumlah klaim!", Toast.LENGTH_SHORT).show();
                return;
            }

            double jumlah;
            try {
                jumlah = Double.parseDouble(jumlahStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Jumlah tidak valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (jumlah <= 0) {
                Toast.makeText(this, "Jumlah harus lebih besar dari 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim ke server
            kirimKlaimDanaKeServer(petani, jumlah, catatan);
            dialog.dismiss();
        });
    }

    // Method untuk mengirim klaim dana ke server
    private void kirimKlaimDanaKeServer(PetaniFasilitator petani, double jumlah, String catatan) {
        String url = ApiConfig.BASE_URL + "klaim_dana_cadangan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Klaim dana berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    // Opsional: update status lokal
                    for (int i = 0; i < listPetani.size(); i++) {
                        PetaniFasilitator p = listPetani.get(i);
                        if (p.user_id == petani.user_id && p.contract_id == petani.contract_id) {
                            p.catatan = catatan;
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> {
                    Toast.makeText(this, "Gagal mengirim klaim dana", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String loggedInUserId = prefs.getString("user_id", null);

                Map<String, String> params = new HashMap<>();
                params.put("user_id", loggedInUserId);          // ID admin poktan
                params.put("contract_id", String.valueOf(petani.contract_id));
                params.put("jumlah", String.valueOf(jumlah));
                params.put("catatan", catatan);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    public void onUpdateSopir(PetaniFasilitator petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status Sopir");
        builder.setMessage("Apakah sopir sudah dalam perjalanan?");

        builder.setPositiveButton("Ya", (dialog, which) -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Sedang dalam perjalanan", "");
            Toast.makeText(this, "Status dikirim: Sedang dalam perjalanan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void onValidasiLogistikClick(PetaniFasilitator petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validasi Logistik");
        builder.setMessage("Apakah bersedia pengiriman dilakukan dari pihak poktan?");

        builder.setPositiveButton("Setuju", null); // override setelah show()
        builder.setNegativeButton("Tolak", null);   // override setelah show()
        builder.setNeutralButton("Batal", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Tombol Setuju
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Pengiriman barang dilakukan", "");
            Toast.makeText(this, "Status dikirim: Pengiriman barang dilakukan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Tombol Tolak
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Pengiriman barang ditolak admin poktan", "");
            Toast.makeText(this, "Status dikirim: Pengiriman barang ditolak", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }


    @Override
    public void onChatClick(PetaniFasilitator petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Tujuan Chat");

        builder.setPositiveButton("Chat Petani", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiver_id", petani.user_id); // ID petani
            intent.putExtra("nama_petani", petani.nama);
            intent.putExtra("company_id", petani.companyId);
            startActivity(intent);
        });

        builder.setNegativeButton("Chat Perusahaan", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiver_id", petani.oftaker_id); // ID oftaker/perusahaan
            intent.putExtra("nama_petani", "Perusahaan"); // opsional
            intent.putExtra("company_id", petani.companyId);
            startActivity(intent);
        });

        builder.setNeutralButton("Batal", null);
        builder.show();
    }

    @Override
    public void onLihatKontrakClick(PetaniFasilitator petani) {
        Intent intent = new Intent(this, ContractDetailActivity.class);
        intent.putExtra("tipe", "petani");
        intent.putExtra("contract_id", String.valueOf(petani.contract_id));
        startActivity(intent);
    }
    public void onBeriUlasan(PetaniFasilitator petani) {
        // Inflate layout untuk ulasan
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_berikan_ulasan, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editUlasan = dialogView.findViewById(R.id.editUlasan);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Beri Ulasan")
                .setView(dialogView)
                .setPositiveButton("Kirim", null) // override nanti
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // Override tombol Kirim supaya bisa validasi input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String ulasan = editUlasan.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Harap beri rating bintang!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ulasan.isEmpty()) {
                Toast.makeText(this, "Harap isi ulasan!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim ulasan ke server
            kirimUlasanKeServer(petani, rating, ulasan);
            dialog.dismiss();
        });
    }
    private void kirimStatusValidasiPoktan(int userId, int contractId, String status, String catatan) {
        String url = ApiConfig.BASE_URL + "update_status_kontrak_poktan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Opsional: parsing response JSON
                    Toast.makeText(this, "Status berhasil diupdate: " + status, Toast.LENGTH_SHORT).show();
                    // Update status lokal di listPetani
                    for (int i = 0; i < listPetani.size(); i++) {
                        PetaniFasilitator p = listPetani.get(i);
                        if (p.user_id == userId && p.contract_id == contractId) {
                            p.status = status;
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> {
                    Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("contract_id", String.valueOf(contractId));
                params.put("status", status);
                params.put("catatan", catatan);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void kirimUlasanKeServer(PetaniFasilitator petani, float rating, String ulasan) {
        String url = ApiConfig.BASE_URL + "insert_ulasan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Ulasan berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    // Update status kontrak jadi Review selesai
                    kirimStatusValidasiPoktan(petani.user_id, petani.contract_id, "Review poktan selesai", "");
                },
                error -> {
                    Toast.makeText(this, "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show();
                    error.printStackTrace(); // tampilkan error detail di Logcat
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String loggedInUserId = prefs.getString("user_id", null);

                Map<String, String> params = new HashMap<>();
                params.put("company_id", String.valueOf(petani.oftaker_id));
                params.put("user_id", loggedInUserId);
                params.put("contract_id", String.valueOf(petani.contract_id));
                params.put("rating", String.valueOf(rating));
                params.put("ulasan", ulasan);
                // jangan kirim created_at, biar database handle otomatis pakai NOW()
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
