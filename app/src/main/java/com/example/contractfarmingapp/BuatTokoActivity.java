package com.example.contractfarmingapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.activities.RegionCodeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BuatTokoActivity extends AppCompatActivity {
    TextView tvJenisBank, tvSkPreview;
    EditText etEmail, etNamaPerusahaan, etKodepoktan, etLokasi, etDeskripsi, etWebsite,
            etKoordinat, etSertifikat, etSosialMedia, etRegionid, etKodepos, etNomorSk, etNamaAkunBank, etNoRekening;
    ImageView imgLogoPreview, imgSkPreview;
    Spinner spinnerJenisUsaha;
    Spinner spinnerProvinsi, spinnerKabupaten, spinnerJenisBank;
    ArrayList<String> provinsiList = new ArrayList<>();
    ArrayList<String> kabupatenList = new ArrayList<>();
    Map<String, String> provinsiMap = new HashMap<>();
    Map<String, String> kabupatenMap = new HashMap<>();

    Button btnCekRegion, btnUploadLogo, btnSimpan, btnPilihLokasi, btnCaraSertifikat, btnUploadSk;
    Bitmap logoBitmap = null, skBitmap = null;

    private static final int PICK_IMAGE_LOGO = 1;
    private static final int PICK_IMAGE_SK = 2;
    private static final int MAP_REQUEST_CODE = 101;
    private static final String URL_INSERT = ApiConfig.BASE_URL + "insert_company.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_toko);

        // Inisialisasi komponen UI
        etEmail = findViewById(R.id.etEmail);
        etNamaPerusahaan = findViewById(R.id.etNamaPerusahaan);
        etLokasi = findViewById(R.id.etLokasi);
        etRegionid = findViewById(R.id.etRegionid);
        etKodepos = findViewById(R.id.etKodepos);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etWebsite = findViewById(R.id.etWebsite);
        etKoordinat = findViewById(R.id.etKoordinat);
        etSertifikat = findViewById(R.id.etSertifikat);
        etSosialMedia = findViewById(R.id.etSosialMedia);
        etNamaAkunBank = findViewById(R.id.etNamaAkunBank);
        etNoRekening = findViewById(R.id.etNoRekening);
        etNamaAkunBank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String upper = s.toString().toUpperCase();
                if (!s.toString().equals(upper)) {
                    etNamaAkunBank.removeTextChangedListener(this);
                    etNamaAkunBank.setText(upper);
                    etNamaAkunBank.setSelection(upper.length());
                    etNamaAkunBank.addTextChangedListener(this);
                }
            }
        });
        etKodepoktan = findViewById(R.id.etKodepoktan);
        etNomorSk = findViewById(R.id.etNomorSk);
        tvJenisBank = findViewById(R.id.tvJenisBank);
        imgLogoPreview = findViewById(R.id.imgLogoPreview);
        imgSkPreview = findViewById(R.id.imgSkPreview);
        tvSkPreview = findViewById(R.id.tvSkPreview);
        btnUploadLogo = findViewById(R.id.btnUploadLogo);
        btnUploadSk = findViewById(R.id.btnUploadSk);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnPilihLokasi = findViewById(R.id.btnPilihLokasi);
        btnCekRegion = findViewById(R.id.btnCekRegion);
        btnCaraSertifikat = findViewById(R.id.btnCaraSertifikat);
        spinnerJenisUsaha = findViewById(R.id.spinnerJenisUsaha);
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKabupaten = findViewById(R.id.spinnerKabupaten);
        spinnerJenisBank = findViewById(R.id.spinnerJenisBank);

// Load Provinsi saat pertama kali
        loadProvinsi();

// Listener Provinsi -> load Kabupaten
        spinnerProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String namaProvinsi = provinsiList.get(position);
                String kodeProvinsi = provinsiMap.get(namaProvinsi);
                loadKabupaten(kodeProvinsi, namaProvinsi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

// Listener Kabupaten -> isi etLokasi
        spinnerKabupaten.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String namaKabupaten = kabupatenList.get(position);
                String namaProvinsi = spinnerProvinsi.getSelectedItem().toString();
                etLokasi.setText(namaKabupaten + ", " + namaProvinsi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Isi email otomatis dari Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            etEmail.setText(user.getEmail());
        }

        // Upload logo
        btnUploadLogo.setOnClickListener(v -> openImageChooser(PICK_IMAGE_LOGO));
        // Upload SK
        btnUploadSk.setOnClickListener(v -> openImageChooser(PICK_IMAGE_SK));

        // Simpan data ke server
        btnSimpan.setOnClickListener(v -> submitForm());

        // Pilih lokasi
        btnPilihLokasi.setOnClickListener(v -> {
            Intent intent = new Intent(BuatTokoActivity.this, MapPickerActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        // Cek Region
        etKodepos.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // ketika fokus pindah
                String kodepos = etKodepos.getText().toString().trim();

                if (kodepos.length() < 3) {
                    Toast.makeText(this, "Masukkan kode pos yang valid (min. 3 karakter)", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(BuatTokoActivity.this);
                progressDialog.setMessage("Mencari lokasi...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // langsung request ke RegionCodeActivity via Intent
                Intent intent = new Intent(BuatTokoActivity.this, RegionCodeActivity.class);
                intent.putExtra("kodepos", kodepos);
                startActivityForResult(intent, 1001);

                new android.os.Handler().postDelayed(progressDialog::dismiss, 3000);
            }
        });

        // Sembunyikan default
        etKodepoktan.setVisibility(View.GONE);

        // Spinner Jenis Usaha
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.jenis_bank,
                R.layout.spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisBank.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapterr = ArrayAdapter.createFromResource(
                this,
                R.array.jenis_usaha_array,
                R.layout.spinner_item
        );
        adapterr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisUsaha.setAdapter(adapterr);

        spinnerJenisUsaha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selected = parent.getItemAtPosition(pos).toString();

                if (selected.equalsIgnoreCase("Kelompok Tani")) {
                    etKodepoktan.setVisibility(View.VISIBLE);

                    // Sembunyikan form bank untuk poktan
                    tvJenisBank.setVisibility(View.GONE);
                    etNamaAkunBank.setVisibility(View.GONE);
                    spinnerJenisBank.setVisibility(View.GONE);
                    etNoRekening.setVisibility(View.GONE);

                } else if (selected.equalsIgnoreCase("Fasilitator")) {
                    // Tampilkan semua elemen bank untuk fasilitator
                    tvJenisBank.setVisibility(View.VISIBLE);
                    etNamaAkunBank.setVisibility(View.VISIBLE);
                    spinnerJenisBank.setVisibility(View.VISIBLE);
                    etNoRekening.setVisibility(View.VISIBLE);

                    // Sembunyikan kode poktan
                    etKodepoktan.setVisibility(View.GONE);

                } else {
                    // Default: sembunyikan semua
                    etKodepoktan.setVisibility(View.GONE);
                    tvJenisBank.setVisibility(View.GONE);
                    etNamaAkunBank.setVisibility(View.GONE);
                    spinnerJenisBank.setVisibility(View.GONE);
                    etNoRekening.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Default: sembunyikan semua
                etKodepoktan.setVisibility(View.GONE);
                tvJenisBank.setVisibility(View.GONE);
                etNamaAkunBank.setVisibility(View.GONE);
                spinnerJenisBank.setVisibility(View.GONE);
                etNoRekening.setVisibility(View.GONE);
            }
        });

    }

    private void openImageChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                if (requestCode == PICK_IMAGE_LOGO) {
                    logoBitmap = bitmap;
                    imgLogoPreview.setImageBitmap(logoBitmap);
                } else if (requestCode == PICK_IMAGE_SK) {
                    skBitmap = bitmap;
                    imgSkPreview.setImageBitmap(skBitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String regionId = data.getStringExtra("region_id");
            String kabupaten = data.getStringExtra("kabupaten");
            String provinsi = data.getStringExtra("provinsi");

            etRegionid.setText(regionId);
            etLokasi.setText(kabupaten + ", " + provinsi); // otomatis terisi
        }


        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            etKoordinat.setText(data.getStringExtra("latitude") + ", " + data.getStringExtra("longitude"));
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] imageBytes = stream.toByteArray();

        // Cek ukuran maksimum 2 MB
        int maxSize = 2 * 1024 * 1024; // 2 MB
        if (imageBytes.length > maxSize) {
            runOnUiThread(() -> Toast.makeText(this,
                    "Ukuran gambar terlalu besar, maksimal 2 MB",
                    Toast.LENGTH_LONG).show());
            return ""; // kembalikan kosong supaya tidak dikirim
        }

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    private void submitForm() {
        // Validasi input sebelum submit
        String jenisUsaha = spinnerJenisUsaha.getSelectedItem().toString().trim();

        if (jenisUsaha.equalsIgnoreCase("Fasilitator")) {
            String namaBank = etNamaAkunBank.getText().toString().trim();
            String jenisBank = spinnerJenisBank.getSelectedItem().toString().trim();
            String noRek = etNoRekening.getText().toString().trim();

            if (namaBank.isEmpty() || jenisBank.isEmpty() || noRek.isEmpty()) {
                Toast.makeText(this, "Semua data bank wajib diisi untuk Fasilitator!", Toast.LENGTH_LONG).show();
                return; // hentikan submit jika kosong
            }
        } else if (jenisUsaha.equalsIgnoreCase("Kelompok Tani")) {
            String kodePoktan = etKodepoktan.getText().toString().trim();
            if (kodePoktan.isEmpty()) {
                Toast.makeText(this, "Kode Poktan wajib diisi untuk Kelompok Tani!", Toast.LENGTH_LONG).show();
                return;
            }
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Menyimpan data...");
        dialog.setCancelable(false);
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URL_INSERT,
                response -> {
                    dialog.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.optString("status");
                        if (status.equals("success")) {
                            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(BuatTokoActivity.this, MainActivity.class);
                            intent.putExtra("fragment_to_open", "perusahaan");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = json.optString("error");
                            Toast.makeText(this, "Gagal: " + error, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Response tidak valid dari server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dialog.dismiss();
                    error.printStackTrace();
                    Toast.makeText(this, "Terjadi kesalahan jaringan. Silahkan coba lagi.", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", etEmail.getText().toString().trim());
                params.put("namaperusahaan", etNamaPerusahaan.getText().toString().trim());
                params.put("deskripsiperusahaan", etDeskripsi.getText().toString().trim());
                params.put("website", etWebsite.getText().toString().trim());
                params.put("lokasiperusahaan", etLokasi.getText().toString().trim());
                params.put("kodepos", etKodepos.getText().toString().trim());
                params.put("region_id", etRegionid.getText().toString().trim());
                params.put("coordinate", etKoordinat.getText().toString().trim());
                params.put("certificate", etSertifikat.getText().toString().trim());
                params.put("social_media", etSosialMedia.getText().toString().trim());
                params.put("jenis_usaha", jenisUsaha);
                params.put("id_poktan", etKodepoktan.getText().toString().trim());
                params.put("nomor_sk", etNomorSk.getText().toString().trim());

                if (jenisUsaha.equalsIgnoreCase("Fasilitator")) {
                    params.put("nama_akun_bank", etNamaAkunBank.getText().toString().trim());
                    params.put("jenis_bank", spinnerJenisBank.getSelectedItem().toString().trim());
                    params.put("nomor_rekening", etNoRekening.getText().toString().trim());
                }

                // Upload logo & SK
                params.put("logo_base64", logoBitmap != null ? bitmapToBase64(logoBitmap) : "");
                params.put("sk_base64", skBitmap != null ? bitmapToBase64(skBitmap) : "");

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }

    private void loadProvinsi() {
        String url = ApiConfig.BASE_URL + "get_provinsi.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    provinsiList.clear();
                    provinsiMap.clear();
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String kode = obj.getString("kode");
                            String nama = obj.getString("nama");
                            provinsiList.add(nama);
                            provinsiMap.put(nama, kode);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                R.layout.spinner_item_4, provinsiList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerProvinsi.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal load provinsi", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadKabupaten(String kodeProvinsi, String namaProvinsi) {
        String url = ApiConfig.BASE_URL + "get_kabupaten.php?prov_id=" + kodeProvinsi;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    kabupatenList.clear();
                    kabupatenMap.clear();
                    try {
                        JSONArray arr = new JSONArray(response);
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String kode = obj.getString("kode");
                            String nama = obj.getString("nama");
                            kabupatenList.add(nama);
                            kabupatenMap.put(nama, kode);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                R.layout.spinner_item_4, kabupatenList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerKabupaten.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal load kabupaten", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

}

