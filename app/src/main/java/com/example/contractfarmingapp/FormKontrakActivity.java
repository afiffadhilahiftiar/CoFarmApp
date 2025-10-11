package com.example.contractfarmingapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.graphics.pdf.PdfDocument;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Environment;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


import com.bumptech.glide.Glide;

public class FormKontrakActivity extends AppCompatActivity {
    TextView tvNamaPetaniTtd, tvNamaPoktan, tvIdPoktan, tvLokasiPoktan;
    ImageView imgLogoPoktan;
    EditText etNamaPetani, etIdPetani, etNamaPerusahaan, etContractId, etKebutuhan, etJumlahKebutuhan,
            etLahan, etStatusLahan, etSatuan, etWaktuDibutuhkan, etTanggalPengajuan, etCatatan;
    EditText etAsuransi;

    Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_kontrak);

        initViews();
        isiDataDariIntent();
        isiDataPoktanDariSharedPref();

        btnSimpan.setOnClickListener(v -> {
            btnSimpan.setVisibility(View.GONE);
            // Validasi seperti biasa
            if (etNamaPetani.getText().toString().isEmpty() || etIdPetani.getText().toString().isEmpty()) {
                Toast.makeText(this, "Mohon isi minimal Nama dan ID Petani", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cetak PDF
            cetakFormKePDF();
            new Handler().postDelayed(() -> btnSimpan.setVisibility(View.VISIBLE), 2000);
        });

    }

    private void initViews() {
        tvNamaPetaniTtd = findViewById(R.id.tvNamaPetaniTtd);
        tvNamaPoktan = findViewById(R.id.tvNamaPoktan);
        tvLokasiPoktan = findViewById(R.id.tvLokasiPoktan);
        tvIdPoktan = findViewById(R.id.tvIdPoktan);
        imgLogoPoktan = findViewById(R.id.imgLogoPoktan);
        etNamaPetani = findViewById(R.id.etNamaPetani);
        etIdPetani = findViewById(R.id.etIdPetani);
        etNamaPerusahaan = findViewById(R.id.etNamaPerusahaan);
        etContractId = findViewById(R.id.etContractId);
        etKebutuhan = findViewById(R.id.etKebutuhan);
        etJumlahKebutuhan = findViewById(R.id.etJumlahKebutuhan);
        etLahan = findViewById(R.id.etLahan);
        etSatuan = findViewById(R.id.etSatuan);
        etStatusLahan = findViewById(R.id.etStatusLahan);
        etWaktuDibutuhkan = findViewById(R.id.etWaktuDibutuhkan);
        etAsuransi = findViewById(R.id.etAsuransi);

        etTanggalPengajuan = findViewById(R.id.etTanggalPengajuan);
        etCatatan = findViewById(R.id.etCatatan);
        btnSimpan = findViewById(R.id.btnSimpan);
    }

    private void isiDataDariIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvNamaPetaniTtd.setText(extras.getString("nama_pengguna", ""));
            etNamaPetani.setText(extras.getString("nama_pengguna", ""));
            etIdPetani.setText(String.valueOf(extras.getInt("user_id", -1)));
            etNamaPerusahaan.setText(extras.getString("nama_perusahaan", ""));
            etContractId.setText(extras.getString("contract_id", ""));
            etKebutuhan.setText(extras.getString("kebutuhan", ""));
            etJumlahKebutuhan.setText(extras.getString("jumlah_kebutuhan", ""));
            etLahan.setText(extras.getString("lahan", ""));
            etSatuan.setText(extras.getString("satuan", ""));
            etStatusLahan.setText(extras.getString("status_lahan", ""));
            etWaktuDibutuhkan.setText(extras.getString("waktu_dibutuhkan", ""));
            etTanggalPengajuan.setText(extras.getString("tanggal_pengajuan", ""));
            etCatatan.setText(extras.getString("catatan", ""));
            etAsuransi.setText(extras.getString("ikut_asuransi", "Tidak")); // default "Tidak"

        }
    }

    private void isiDataPoktanDariSharedPref() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String namaPoktan = prefs.getString("nama_poktan", "");
        String idPoktan = prefs.getString("id_poktan", "");
        String lokasiPoktan = prefs.getString("lokasi_poktan", "");
        String logoUrl = prefs.getString("logo_poktan", "");

        tvNamaPoktan.setText(namaPoktan.toUpperCase());
        tvIdPoktan.setText("Kelompok Tani " + idPoktan);
        tvLokasiPoktan.setText("Lokasi: " + lokasiPoktan);

        if (!logoUrl.isEmpty()) {
            Glide.with(this)
                    .load(logoUrl)
                    .placeholder(R.drawable.ic_logoapp) // Ganti sesuai drawable kamu
                    .into(imgLogoPoktan);
        }
    }
    private void cetakFormKePDF() {
        ScrollView scrollView = findViewById(R.id.scrollViewForm); // Pastikan ID ScrollView sesuai

        // Ukur dimensi konten sebenarnya
        View content = scrollView.getChildAt(0);
        int totalHeight = content.getHeight();
        int totalWidth = content.getWidth();

        // Buat bitmap dari konten
        Bitmap bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        content.draw(canvas);

        // Inisialisasi dokumen PDF
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(totalWidth, totalHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        // Gambar bitmap ke halaman PDF
        page.getCanvas().drawBitmap(bitmap, 0f, 0f, null);
        document.finishPage(page);

        // Simpan file PDF
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        if (!folder.exists()) folder.mkdirs();

        String namaPetani = etNamaPetani.getText().toString().replaceAll("\\s+", "_");
        String idKontrak = etContractId.getText().toString();
        String tanggal = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(new java.util.Date());
        String fileName = "kontrak_" + namaPetani + "_" + idKontrak + "_" + tanggal + ".pdf";

        File file = new File(folder, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "PDF disimpan di: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();

            // Buka otomatis
            openPdfFile(file);
            uploadPDFToServer(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdfFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Tidak ada aplikasi pembuka PDF", Toast.LENGTH_LONG).show();
        }
    }
    private void uploadPDFToServer(File file) {
        new Thread(() -> {
            try {
                String boundary = "===" + System.currentTimeMillis() + "===";
                String LINE_FEED = "\r\n";

                URL url = new URL(ApiConfig.BASE_URL + "upload_surat_pdf.php"); // ganti sesuai server
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                // Tambahkan sender_id dan receiver_id (bisa ambil dari SharedPreferences atau intent)
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int senderId = prefs.getInt("id", -1);
                int receiverId = prefs.getInt("company_id", -1); // atau id perusahaan

                writeFormField(outputStream, boundary, "sender_id", String.valueOf(senderId));
                writeFormField(outputStream, boundary, "receiver_id", String.valueOf(receiverId));

                // Tambahkan file PDF
                writeFileField(outputStream, boundary, "pdf_file", file);

                // Penutup
                outputStream.writeBytes("--" + boundary + "--" + LINE_FEED);
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(this, "Berhasil mengirim ke server", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Gagal kirim file: " + responseCode, Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void writeFormField(DataOutputStream outputStream, String boundary, String fieldName, String value) throws IOException {
        String LINE_FEED = "\r\n";
        outputStream.writeBytes("--" + boundary + LINE_FEED);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"" + LINE_FEED);
        outputStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
        outputStream.writeBytes(LINE_FEED);
        outputStream.writeBytes(value + LINE_FEED);
    }

    private void writeFileField(DataOutputStream outputStream, String boundary, String fieldName, File file) throws IOException {
        String LINE_FEED = "\r\n";
        String fileName = file.getName();
        FileInputStream inputStream = new FileInputStream(file);

        outputStream.writeBytes("--" + boundary + LINE_FEED);
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"" + LINE_FEED);
        outputStream.writeBytes("Content-Type: application/pdf" + LINE_FEED);
        outputStream.writeBytes(LINE_FEED);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.writeBytes(LINE_FEED);
    }


}
