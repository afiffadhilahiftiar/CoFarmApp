package com.example.contractfarmingapp.activities;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText etNama, etAlamat, etNoHp, etKtp, etTanggalLahir, etNamaAkunBank, etNoRekening;
    private RadioGroup rgGender;
    private ImageView ivFotoProfil, ivFotoKtp;
    private Button btnSimpan, btnSelectFotoProfil, btnSelectFotoKtp;
    private ProgressBar progressBar;
    private Spinner spinnerJenisBank;
    private Spinner spinnerProvinsi, spinnerKabupaten;
    private String selectedProvinsi = "";
    private String selectedKabupaten = "";
    private SharedPreferences sharedPreferences;

    private Bitmap fotoProfilBitmap, fotoKtpBitmap;
    private String emailUser = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ambil email login dari Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) emailUser = user.getEmail();
        if (emailUser == null || emailUser.isEmpty()) {
            Toast.makeText(this, "Data user tidak tersedia, silakan login ulang.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inisialisasi view
        etNama = findViewById(R.id.etNama);
        etAlamat = findViewById(R.id.etAlamat);
        etNoHp = findViewById(R.id.etNoHp);
        etKtp = findViewById(R.id.etKtp);
        etTanggalLahir = findViewById(R.id.etTanggalLahir);
        rgGender = findViewById(R.id.rgGender);
        ivFotoProfil = findViewById(R.id.imgFotoProfil);
        ivFotoKtp = findViewById(R.id.imgFotoKtp);
        btnSimpan = findViewById(R.id.btnSimpan);
        btnSelectFotoProfil = findViewById(R.id.btnPilihFotoProfil);
        btnSelectFotoKtp = findViewById(R.id.btnPilihFotoKtp);
        progressBar = findViewById(R.id.progressBar);
        spinnerJenisBank = findViewById(R.id.spinnerJenisBank);
        etNamaAkunBank = findViewById(R.id.etNamaAkunBank);
        etNoRekening = findViewById(R.id.etNoRekening);
        ArrayAdapter<CharSequence> bankAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.jenis_bank,
                R.layout.spinner_item
        );
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJenisBank.setAdapter(bankAdapter);
        spinnerProvinsi = findViewById(R.id.spinnerProvinsi);
        spinnerKabupaten = findViewById(R.id.spinnerKabupaten);
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
// Ambil daftar provinsi dari API get_provinsi.php
        new ProfileActivity.LoadProvinsiTask().execute();
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        // 🔹 Ambil nama dari intent jika ada
        String namaDariIntent = getIntent().getStringExtra("nama");
        if (namaDariIntent != null && !namaDariIntent.isEmpty()) {
            etNama.setText(namaDariIntent);
        }
        // Aksi view
        etTanggalLahir.setOnClickListener(v -> showDatePicker());
        btnSelectFotoProfil.setOnClickListener(v -> pilihGambar(1));
        btnSelectFotoKtp.setOnClickListener(v -> pilihGambar(2));
        btnSimpan.setOnClickListener(v -> kirimData());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) ->
                etTanggalLahir.setText(y + "/" + (m + 1) + "/" + d),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pilihGambar(int req) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, req);
    }

    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            try (InputStream is = getContentResolver().openInputStream(data.getData())) {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (requestCode == 1) {
                    fotoProfilBitmap = bmp;
                    ivFotoProfil.setImageBitmap(bmp);
                } else {
                    fotoKtpBitmap = bmp;
                    ivFotoKtp.setImageBitmap(bmp);
                }
            } catch (IOException ignored) {}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private class LoadProvinsiTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "get_provinsi.php");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                return sb.toString();
            } catch (Exception e) { return null; }
        }

        protected void onPostExecute(String resp) {
            if (resp == null) return;
            try {
                JSONArray arr = new JSONArray(resp);
                List<String> provList = new ArrayList<>();
                final Map<String,String> provMap = new HashMap<>();
                for (int i=0;i<arr.length();i++){
                    JSONObject o = arr.getJSONObject(i);
                    provList.add(o.getString("nama"));
                    provMap.put(o.getString("nama"), o.getString("kode"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this,
                        R.layout.spinner_item_5, provList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProvinsi.setAdapter(adapter);

                spinnerProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        selectedProvinsi = parent.getItemAtPosition(pos).toString();
                        String kode = provMap.get(selectedProvinsi);
                        new ProfileActivity.LoadKabupatenTask(kode).execute();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } catch (Exception ignored) {}
        }
    }
    private class LoadKabupatenTask extends AsyncTask<Void, Void, String> {
        private final String provId;
        LoadKabupatenTask(String provId){ this.provId = provId; }

        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "get_kabupaten.php?prov_id=" + provId);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
                return sb.toString();
            } catch (Exception e) { return null; }
        }

        protected void onPostExecute(String resp) {
            if (resp == null) return;
            try {
                JSONArray arr = new JSONArray(resp);
                List<String> kabList = new ArrayList<>();
                for (int i=0;i<arr.length();i++){
                    kabList.add(arr.getJSONObject(i).getString("nama"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ProfileActivity.this,
                        R.layout.spinner_item_5, kabList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerKabupaten.setAdapter(adapter);

                spinnerKabupaten.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        selectedKabupaten = parent.getItemAtPosition(pos).toString();
                        // otomatis isi etAlamat
                        etAlamat.setText(selectedKabupaten + ", " + selectedProvinsi);
                    }
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } catch (Exception ignored) {}
        }
    }
    private void kirimData() {
        // Validasi input
        String nama = etNama.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();
        String noHp = etNoHp.getText().toString().trim();
        String ktp = etKtp.getText().toString().trim();
        String tanggal = etTanggalLahir.getText().toString().trim();
        String selectedJenisBank = spinnerJenisBank.getSelectedItem().toString();
        String namaAkunBank = etNamaAkunBank.getText().toString().trim();
        String noRekening = etNoRekening.getText().toString().trim();
        int sel = rgGender.getCheckedRadioButtonId();
        String gender = sel == R.id.rbLaki ? "Laki-laki" :
                sel == R.id.rbPerempuan ? "Perempuan" : "";

        if (nama.isEmpty() || alamat.isEmpty() || noHp.isEmpty() ||
                ktp.length() != 16 || tanggal.isEmpty() || gender.isEmpty() ||
                fotoProfilBitmap == null || fotoKtpBitmap == null) {
            Toast.makeText(this, "Lengkapi semua data profil!", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("email", emailUser);
            json.put("nama", nama);
            json.put("alamat", alamat);
            json.put("bank_code", selectedJenisBank);
            json.put("bank_account_name", namaAkunBank);
            json.put("bank_account_number", noRekening);
            json.put("no_hp", noHp);
            json.put("ktp", ktp);
            json.put("gender", gender);
            json.put("tanggal_lahir", tanggal);
            json.put("foto_profil", bitmapToBase64(fotoProfilBitmap));
            json.put("foto_ktp", bitmapToBase64(fotoKtpBitmap));

            new KirimDataTask(json.toString()).execute();
        } catch (Exception e) {
            Toast.makeText(this, "Error validasi JSON!", Toast.LENGTH_SHORT).show();
        }
    }

    private String bitmapToBase64(Bitmap bmp) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, os);
        return Base64.encodeToString(os.toByteArray(), Base64.NO_WRAP);
    }

    private class KirimDataTask extends AsyncTask<Void, Void, String> {
        private final String payload;
        KirimDataTask(String payload) { this.payload = payload; }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            btnSimpan.setEnabled(false);
        }
        private String genderFromRadioId(int id) {
            if (id == R.id.rbLaki) return "Laki-laki";
            else if (id == R.id.rbPerempuan) return "Perempuan";
            else return "";
        }

        @Override
        protected String doInBackground(Void... v) {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(ApiConfig.BASE_URL + "upload_profile.php").openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-Type", "application/json");
                c.setDoOutput(true);
                try (OutputStreamWriter w = new OutputStreamWriter(c.getOutputStream())) {
                    w.write(payload);
                }
                int code = c.getResponseCode();
                if (code == 200) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                        return br.readLine();
                    }
                } else {
                    return "{\"status\":\"error\",\"message\":\"HTTP "+code+"\"}";
                }
            } catch (Exception e) {
                return "{\"status\":\"error\",\"message\":\""+e.getMessage()+"\"}";
            }
        }

        @Override
        protected void onPostExecute(String resp) {
            progressBar.setVisibility(View.GONE);
            btnSimpan.setEnabled(true);
            try {
                JSONObject obj = new JSONObject(resp);
                String s = obj.optString("status");
                Toast.makeText(ProfileActivity.this, obj.optString("message"), Toast.LENGTH_LONG).show();
                if ("success".equalsIgnoreCase(s)) {
                    // Simpan ke SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", emailUser);
                    editor.putString("nama", etNama.getText().toString().trim());
                    editor.putString("alamat", etAlamat.getText().toString().trim());
                    editor.putString("no_hp", etNoHp.getText().toString().trim());
                    editor.putString("ktp", etKtp.getText().toString().trim());
                    editor.putString("tanggal_lahir", etTanggalLahir.getText().toString().trim());
                    editor.putString("gender", genderFromRadioId(rgGender.getCheckedRadioButtonId()));
                    editor.apply();

                    simpanGambarKePictures(fotoProfilBitmap, "profil_" + etNama.getText().toString());
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }

            } catch (Exception e) {
                Toast.makeText(ProfileActivity.this, "Respons JSON error", Toast.LENGTH_LONG).show();
            }
        }

        private void simpanGambarKePictures(Bitmap bmp, String name) {
            try (OutputStream fos = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    getContentResolver().openOutputStream(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ContractFarming"))) :
                    new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name+".jpg"))
            ) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            } catch (Exception ignored) {}
        }
    }
}
