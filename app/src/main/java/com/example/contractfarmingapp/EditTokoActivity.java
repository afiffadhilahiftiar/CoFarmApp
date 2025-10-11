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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.activities.RegionCodeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditTokoActivity extends AppCompatActivity {
    TextView tvSkPreview,tvJenisBank;
    EditText etEmail, etNamaPerusahaan, etKodepoktan, etLokasi, etDeskripsi, etWebsite, etKoordinat,
            etSertifikat, etSosialMedia, etRegionid, etKodepos, etNomorSk, etNamaAkunBank, etNoRekening;
    ImageView imgLogoPreview, imgSkPreview;
    Spinner spinnerJenisUsaha;
    Spinner spinnerProvinsi, spinnerKabupaten, spinnerJenisBank;

    Button btnCekRegion, btnUploadLogo, btnUploadSk, btnUpdate, btnPilihLokasi, btnCaraSertifikat;

    Bitmap logoBitmap = null;
    Bitmap skBitmap = null;
    boolean logoDariGaleri = false;
    boolean skDariGaleri = false;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_SK_REQUEST = 2;
    private static final int MAP_REQUEST_CODE = 101;

    private static final String URL_UPDATE = ApiConfig.BASE_URL + "update_company.php";
    private static final String URL_GET = ApiConfig.BASE_URL + "get_company_by_email.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buat_toko);

        // Inisialisasi komponen
        etEmail = findViewById(R.id.etEmail);
        etNamaPerusahaan = findViewById(R.id.etNamaPerusahaan);
        etLokasi = findViewById(R.id.etLokasi);
        etRegionid = findViewById(R.id.etRegionid);
        etKodepos = findViewById(R.id.etKodepos);
        etNamaAkunBank = findViewById(R.id.etNamaAkunBank);
        etNoRekening = findViewById(R.id.etNoRekening);
        etDeskripsi = findViewById(R.id.etDeskripsi);
        etWebsite = findViewById(R.id.etWebsite);
        etKoordinat = findViewById(R.id.etKoordinat);
        etSertifikat = findViewById(R.id.etSertifikat);
        etSosialMedia = findViewById(R.id.etSosialMedia);
        etKodepoktan = findViewById(R.id.etKodepoktan);
        etNomorSk = findViewById(R.id.etNomorSk); // Nomor SK
        tvJenisBank = findViewById(R.id.tvJenisBank);
        btnCekRegion = findViewById(R.id.btnCekRegion);
        btnUploadLogo = findViewById(R.id.btnUploadLogo);
        btnUploadSk = findViewById(R.id.btnUploadSk);
        btnCaraSertifikat = findViewById(R.id.btnCaraSertifikat);
        btnUpdate = findViewById(R.id.btnSimpan);
        btnPilihLokasi = findViewById(R.id.btnPilihLokasi);
        tvSkPreview = findViewById(R.id.tvSkPreview);
        imgLogoPreview = findViewById(R.id.imgLogoPreview);
        imgSkPreview = findViewById(R.id.imgSkPreview);
        spinnerJenisUsaha = findViewById(R.id.spinnerJenisUsaha);
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKabupaten = findViewById(R.id.spinnerKabupaten);
        spinnerJenisBank = findViewById(R.id.spinnerJenisBank);
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

        loadProvinsi();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            etEmail.setText(user.getEmail());
        }

        btnCaraSertifikat.setOnClickListener(v -> {
            String url = "https://sistemcerdasindonesia.com/#sertifikat";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        btnUploadLogo.setOnClickListener(v -> openImageChooser(PICK_IMAGE_REQUEST));
        btnUploadSk.setOnClickListener(v -> openImageChooser(PICK_SK_REQUEST));

        btnPilihLokasi.setOnClickListener(v -> {
            Intent intent = new Intent(EditTokoActivity.this, MapPickerActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        etKodepos.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // ketika fokus pindah
                String kodepos = etKodepos.getText().toString().trim();

                if (kodepos.length() < 3) {
                    Toast.makeText(this, "Masukkan kode pos yang valid (min. 3 karakter)", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProgressDialog progressDialog = new ProgressDialog(EditTokoActivity.this);
                progressDialog.setMessage("Mencari lokasi...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // langsung request ke RegionCodeActivity via Intent
                Intent intent = new Intent(EditTokoActivity.this, RegionCodeActivity.class);
                intent.putExtra("kodepos", kodepos);
                startActivityForResult(intent, 1001);

                new android.os.Handler().postDelayed(progressDialog::dismiss, 3000);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.setOnClickListener(v -> submitUpdate());

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


        loadDataFromServer();
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
                if (requestCode == PICK_IMAGE_REQUEST) {
                    logoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imgLogoPreview.setImageBitmap(logoBitmap);
                    logoDariGaleri = true;
                } else if (requestCode == PICK_SK_REQUEST) {
                    skBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                    imgSkPreview.setImageBitmap(skBitmap);
                    skDariGaleri = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            etRegionid.setText(data.getStringExtra("region_id"));
            etLokasi.setText(data.getStringExtra("kabupaten") + ", " + data.getStringExtra("provinsi"));
        }


        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            etKoordinat.setText(data.getStringExtra("latitude") + ", " + data.getStringExtra("longitude"));
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    private void loadDataFromServer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        String url = URL_GET + "?email=" + email;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONObject data = response.getJSONObject("data");

                            etNamaPerusahaan.setText(data.getString("namaperusahaan"));
                            etDeskripsi.setText(data.getString("deskripsiperusahaan"));
                            etWebsite.setText(data.getString("website"));
                            etKoordinat.setText(data.getString("coordinate"));
                            etSertifikat.setText(data.getString("certificate"));
                            etSosialMedia.setText(data.getString("social_media"));
                            etRegionid.setText(data.getString("region_id"));
                            etKodepos.setText(data.getString("kodepos"));
                            etKodepoktan.setText(data.getString("id_poktan"));
                            etNomorSk.setText(data.optString("nomor_sk", ""));

                            // === TENTUKAN LOKASI LANGSUNG ===
                            String lokasiServer = data.optString("lokasiperusahaan", "");
                            if (!lokasiServer.isEmpty()) {
                                // langsung isi EditText etLokasi tanpa mengubah spinner
                                etLokasi.setText(lokasiServer);
                            }

                            // SET JENIS USAHA
                            String jenis = data.getString("jenis_usaha");
                            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerJenisUsaha.getAdapter();
                            int index = adapter.getPosition(jenis);
                            spinnerJenisUsaha.setSelection(index);
                            spinnerJenisUsaha.setEnabled(false);

                            // LOGO
                            String logoUrl = data.getString("logoperusahaan");
                            if (!logoDariGaleri && logoUrl != null && !logoUrl.isEmpty() && !"null".equalsIgnoreCase(logoUrl)) {
                                Glide.with(this).load(logoUrl).into(imgLogoPreview);
                            }

                            // SK
                            String skUrl = data.optString("sk_perusahaan", "");
                            if (!skDariGaleri && skUrl != null && !skUrl.isEmpty() && !"null".equalsIgnoreCase(skUrl)) {
                                Glide.with(this).load(skUrl).into(imgSkPreview);
                            }
                            // === AMBIL INFO BANK DARI USERPROFILES ===
                            etNamaAkunBank.setText(data.optString("bank_account_name_asuransi", ""));
                            etNoRekening.setText(data.optString("bank_account_number_asuransi", ""));
                            String bankCode = data.optString("bank_code_asuransi", "");
                            if (!bankCode.isEmpty()) {
                                // Set spinner ke bank yang sesuai dengan kode bank
                                ArrayAdapter<CharSequence> bankAdapter = (ArrayAdapter<CharSequence>) spinnerJenisBank.getAdapter();
                                int bankIndex = bankAdapter.getPosition(bankCode);
                                if (bankIndex >= 0) spinnerJenisBank.setSelection(bankIndex);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal mengambil data toko", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(request);
    }


    private void submitUpdate() {
        // Validasi jika jenis usaha Fasilitator
        String jenisUsaha = spinnerJenisUsaha.getSelectedItem().toString().trim();
        if (jenisUsaha.equalsIgnoreCase("Fasilitator")) {
            String namaBank = etNamaAkunBank.getText().toString().trim();
            String jenisBank = spinnerJenisBank.getSelectedItem().toString().trim();
            String noRek = etNoRekening.getText().toString().trim();

            if (namaBank.isEmpty() || jenisBank.isEmpty() || noRek.isEmpty()) {
                Toast.makeText(this, "Semua data bank wajib diisi!", Toast.LENGTH_LONG).show();
                return; // hentikan submit jika kosong
            }
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Mengupdate data...");
        dialog.setCancelable(false);
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URL_UPDATE,
                response -> {
                    dialog.dismiss();
                    try {
                        JSONObject json = new JSONObject(response);
                        if ("success".equalsIgnoreCase(json.optString("status"))) {
                            Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditTokoActivity.this, MainActivity.class);
                            intent.putExtra("fragment_to_open", "perusahaan");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, json.optString("error", "Gagal memperbarui data"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Respon tidak valid dari server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Gagal mengupdate data. Periksa koneksi internet!", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", etEmail.getText().toString().trim());
                params.put("namaperusahaan", etNamaPerusahaan.getText().toString().trim());
                params.put("lokasiperusahaan", etLokasi.getText().toString().trim());
                params.put("kodepos", etKodepos.getText().toString().trim());
                params.put("region_id", etRegionid.getText().toString().trim());
                params.put("deskripsiperusahaan", etDeskripsi.getText().toString().trim());
                params.put("website", etWebsite.getText().toString().trim());
                params.put("coordinate", etKoordinat.getText().toString().trim());
                params.put("certificate", etSertifikat.getText().toString().trim());
                params.put("social_media", etSosialMedia.getText().toString().trim());
                params.put("jenis_usaha", spinnerJenisUsaha.getSelectedItem().toString().trim());
                params.put("id_poktan", etKodepoktan.getText().toString().trim());
                params.put("nomor_sk", etNomorSk.getText().toString().trim());

                params.put("nama_akun_bank", etNamaAkunBank.getText().toString().trim());
                params.put("jenis_bank", spinnerJenisBank.getSelectedItem().toString().trim());
                params.put("nomor_rekening", etNoRekening.getText().toString().trim());

                params.put("logo_base64", logoBitmap != null ? bitmapToBase64(logoBitmap) : "");
                params.put("sk_base64", skBitmap != null ? bitmapToBase64(skBitmap) : "");

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void loadProvinsi() {
        String url = ApiConfig.BASE_URL + "get_provinsi.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    ArrayList<String> provinsiList = new ArrayList<>();
                    ArrayList<String> provinsiKode = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            provinsiList.add(obj.getString("nama"));
                            provinsiKode.add(obj.getString("kode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                           R.layout.spinner_item_4, provinsiList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvinsi.setAdapter(adapter);

                    spinnerProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            String kodeProv = provinsiKode.get(pos);
                            loadKabupaten(kodeProv, provinsiList.get(pos));
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });

                },
                error -> Toast.makeText(this, "Gagal load provinsi", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void loadKabupaten(String provId, String namaProvinsi) {
        String url = ApiConfig.BASE_URL + "get_kabupaten.php?prov_id=" + provId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    ArrayList<String> kabList = new ArrayList<>();
                    ArrayList<String> kabKode = new ArrayList<>();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            kabList.add(obj.getString("nama"));
                            kabKode.add(obj.getString("kode"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            R.layout.spinner_item_4, kabList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKabupaten.setAdapter(adapter);

                    spinnerKabupaten.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            // Isi otomatis ke EditText
                            etLokasi.setText(kabList.get(pos) + ", " + namaProvinsi);
                            etRegionid.setText(kabKode.get(pos));
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                },
                error -> Toast.makeText(this, "Gagal load kabupaten", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

}
