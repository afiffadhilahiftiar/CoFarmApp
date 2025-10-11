package com.example.contractfarmingapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PemberkasanActivity extends AppCompatActivity {

    TextView tvNamaPoktan, tvIdPoktan, tvLokasiPoktan, tvTanggal, tvKepada,
            tvNamaPetani, tvIdPetani, tvPerusahaan, tvAlamat,
            tvKomoditas, tvJumlah, tvLahan, tvStatusLahan, tvWaktu,
            tvCatatan, tvTtdNama, tvPemberitahuan, tvAsuransi;
    Button btnCetakPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pemberkasan);

        initViews();
        isiDataDariIntent();
        isiDataPoktanDariSharedPref();

        btnCetakPdf.setVisibility(View.GONE);

        // Otomatis buat PDF setelah 2 detik
        new Handler().postDelayed(() -> cetakPemberkasanKePDF(), 2000);

        btnCetakPdf.setOnClickListener(v -> {
            btnCetakPdf.setVisibility(View.GONE);
            cetakPemberkasanKePDF();
            new Handler().postDelayed(() -> btnCetakPdf.setVisibility(View.VISIBLE), 2000);
            new Handler().postDelayed(() -> tvPemberitahuan.setVisibility(View.VISIBLE), 2000);
        });
    }

    private void initViews() {
        tvNamaPoktan = findViewById(R.id.tvNamaPoktan);
        tvIdPoktan = findViewById(R.id.tvIdPoktan);
        tvLokasiPoktan = findViewById(R.id.tvLokasiPoktan);
        tvTanggal = findViewById(R.id.tvTanggal);
        tvKepada = findViewById(R.id.tvKepada);

        tvNamaPetani = findViewById(R.id.tvNamaPetani);
        tvIdPetani = findViewById(R.id.tvIdPetani);
        tvPerusahaan = findViewById(R.id.tvPerusahaan);
        tvAlamat = findViewById(R.id.tvAlamat);

        tvKomoditas = findViewById(R.id.tvKomoditas);
        tvJumlah = findViewById(R.id.tvJumlah);
        tvLahan = findViewById(R.id.tvLahan);
        tvStatusLahan = findViewById(R.id.tvStatusLahan);
        tvWaktu = findViewById(R.id.tvWaktu);
        tvCatatan = findViewById(R.id.tvCatatan);
        tvTtdNama = findViewById(R.id.tvTtdNama);
        tvPemberitahuan = findViewById(R.id.tvPemberitahuan);
        btnCetakPdf = findViewById(R.id.btnCetakPdf);

        // 🔹 Tambahan untuk status Asuransi
        tvAsuransi = findViewById(R.id.tvAsuransi);
    }

    private void isiDataDariIntent() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String namaPetani = extras.getString("nama_pengguna", "-");
            int idPetani = extras.getInt("user_id", -1);
            String perusahaan = extras.getString("nama_perusahaan", "-");
            String alamat = extras.getString("alamat", "-");
            String komoditas = extras.getString("kebutuhan", "-");
            String jumlah = extras.getString("jumlah_kebutuhan", "-");
            String satuan = extras.getString("satuan", "kg");
            String lahan = extras.getString("lahan", "-");
            String status = extras.getString("status_lahan", "-");
            String waktu = extras.getString("waktu_dibutuhkan", "-");
            String tanggal = extras.getString("tanggal_pengajuan", "-");
            String catatan = extras.getString("catatan", "-");
            String ikutAsuransi = extras.getString("ikut_asuransi", "-"); // ✅ ambil status asuransi

            tvNamaPetani.setText("Nama: " + namaPetani);
            tvIdPetani.setText("ID Petani: " + idPetani);
            tvPerusahaan.setText("Nama Perusahaan: " + perusahaan);
            tvAlamat.setText("Alamat: " + alamat);
            tvKomoditas.setText("Komoditas: " + komoditas);
            tvJumlah.setText("Jumlah: " + jumlah + " " + satuan);
            tvLahan.setText("Lahan: " + lahan);
            tvStatusLahan.setText("Status Lahan: " + status);
            tvWaktu.setText("Waktu Dibutuhkan: " + waktu);
            tvTanggal.setText("Tanggal: " + tanggal);
            tvCatatan.setText("Dukungan yang diharapkan dari Perusahaan: " + catatan);
            tvAsuransi.setText("Asuransi: " + ikutAsuransi); // ✅ tampilkan Ya/Tidak
            tvTtdNama.setText("(" + namaPetani + ")");
        }
    }

    private void isiDataPoktanDariSharedPref() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String namaPoktan = prefs.getString("nama_poktan", "-");
        int idPoktan = prefs.getInt("company_id", -1);
        String lokasiPoktan = prefs.getString("lokasi_poktan", "-");

        tvNamaPoktan.setText(namaPoktan.toUpperCase());
        tvIdPoktan.setText("ID POKTAN: " + idPoktan);
        tvLokasiPoktan.setText("LOKASI: " + lokasiPoktan);
        tvKepada.setText("Kepada Yth. Admin Kelompok Tani " + namaPoktan + " di Tempat");
    }

    private void cetakPemberkasanKePDF() {
        ScrollView scrollView = findViewById(R.id.scrollViewPemberkasan);
        View content = scrollView.getChildAt(0);

        int pageWidth = 2480; // A4 @300dpi
        int pageHeight = 3508;
        int margin = 100;

        Bitmap originalBitmap = Bitmap.createBitmap(content.getWidth(), content.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas originalCanvas = new Canvas(originalBitmap);
        content.draw(originalCanvas);

        float scaleX = (float)(pageWidth - 2 * margin) / content.getWidth();
        float scaleY = (float)(pageHeight - 2 * margin) / content.getHeight();
        float scale = Math.min(scaleX, scaleY);

        int finalWidth = (int)(content.getWidth() * scale);
        int finalHeight = (int)(content.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas pdfCanvas = page.getCanvas();

        int left = (pageWidth - finalWidth) / 2;
        int top = (pageHeight - finalHeight) / 2;
        pdfCanvas.drawBitmap(scaledBitmap, left, top, null);
        document.finishPage(page);

        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, "pemberkasan_kontrak_petani_" + System.currentTimeMillis() + ".pdf");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            document.writeTo(fos);
            document.close();
            Toast.makeText(this, "PDF disimpan di: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            openPdfFile(file);
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
}
