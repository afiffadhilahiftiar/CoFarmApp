package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SopirDetailActivity extends AppCompatActivity {

    private TextView tvNama, tvNoHp, tvKendaraan, tvPlat, tvKapasitas;
    private ImageView ivFotoSopir, ivFotoKendaraan, ivFotoSim, ivFotoStnk;
    private Button btnExportPdf, btnLihatLokasi;

    private String nama, noHp, kendaraan, platNomor, kapasitas;
    private String fotoSopirUrl, fotoKendaraanUrl, fotoSimUrl, fotoStnkUrl, linkLokasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sopir_detail);

        tvNama = findViewById(R.id.tvDetailNama);
        tvNoHp = findViewById(R.id.tvDetailNoHp);
        tvKendaraan = findViewById(R.id.tvDetailKendaraan);
        tvPlat = findViewById(R.id.tvDetailPlat);
        tvKapasitas = findViewById(R.id.tvDetailKapasitas);

        ivFotoSopir = findViewById(R.id.ivFotoSopir);
        ivFotoKendaraan = findViewById(R.id.ivFotoKendaraan);
        ivFotoSim = findViewById(R.id.ivFotoSim);
        ivFotoStnk = findViewById(R.id.ivFotoStnk);

        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnLihatLokasi = findViewById(R.id.btnLihatLokasi); // tombol baru
        // Ambil data dari Intent
        Intent intent = getIntent();
        nama = intent.getStringExtra("nama");
        noHp = intent.getStringExtra("no_hp");
        kendaraan = intent.getStringExtra("kendaraan");
        platNomor = intent.getStringExtra("plat_nomor");
        kapasitas = intent.getStringExtra("kapasitas");

        fotoSopirUrl = intent.getStringExtra("foto_sopir");
        fotoKendaraanUrl = intent.getStringExtra("foto_kendaraan");
        fotoSimUrl = intent.getStringExtra("foto_sim");
        fotoStnkUrl = intent.getStringExtra("foto_stnk");
        linkLokasi = intent.getStringExtra("link_lokasi"); // ambil link Google Maps

        // Set text
        tvNama.setText("Nama: " + nama);
        tvNoHp.setText("No HP: " + noHp);
        tvKendaraan.setText("Kendaraan: " + kendaraan);
        tvPlat.setText("Plat Nomor: " + platNomor);
        tvKapasitas.setText("Kapasitas: " + kapasitas);

        // Load foto menggunakan Glide
        Glide.with(this).load(fotoSopirUrl).into(ivFotoSopir);
        Glide.with(this).load(fotoKendaraanUrl).into(ivFotoKendaraan);
        Glide.with(this).load(fotoSimUrl).into(ivFotoSim);
        Glide.with(this).load(fotoStnkUrl).into(ivFotoStnk);

        btnExportPdf.setOnClickListener(v -> new Thread(this::exportToPdf).start());
        btnLihatLokasi.setOnClickListener(v -> openGoogleMaps());

    }

    private void openGoogleMaps() {
        if (linkLokasi == null || linkLokasi.trim().isEmpty()) {
            Toast.makeText(this, "Lokasi belum tersedia untuk sopir ini", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = Uri.parse(linkLokasi);

            // Buat Intent tanpa setPackage supaya Android otomatis memilih app
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Cek apakah ada aplikasi yang bisa handle Intent
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback: buka di browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuka lokasi", Toast.LENGTH_SHORT).show();
        }
    }


    private void exportToPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        int pageWidth = 595;  // A4 (72 dpi)
        int pageHeight = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int margin = 40;
        int x = margin;
        int y = margin + 20;

        // Judul
        paint.setTextSize(28f);
        paint.setFakeBoldText(true);
        canvas.drawText("Laporan Detail Sopir", x, y, paint);
        y += 40;

        // Tabel informasi sopir
        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.3f);
        paint.setColor(Color.BLACK);

        String[][] data = {
                {"Nama", nama},
                {"No HP", noHp},
                {"Kendaraan", kendaraan},
                {"Plat Nomor", platNomor},
                {"Kapasitas", kapasitas + " kg"}
        };

        int startX = margin;
        int endX = pageWidth - margin;
        int labelWidth = 150;
        int rowHeight = 35;
        int tableTop = y;

        // Border tabel
        canvas.drawRect(startX, tableTop, endX, tableTop + (data.length * rowHeight), paint);
        canvas.drawLine(startX + labelWidth, tableTop, startX + labelWidth, tableTop + (data.length * rowHeight), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(15f);
        for (int i = 0; i < data.length; i++) {
            int textY = tableTop + (i * rowHeight) + 23;
            canvas.drawText(data[i][0], startX + 10, textY, paint);
            canvas.drawText(": " + data[i][1], startX + labelWidth + 10, textY, paint);
            if (i < data.length - 1) {
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(startX, tableTop + ((i + 1) * rowHeight), endX, tableTop + ((i + 1) * rowHeight), paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        y = tableTop + (data.length * rowHeight) + 30;

        // Label foto
        paint.setTextSize(18f);
        paint.setFakeBoldText(true);
        canvas.drawText("Foto Dokumen", x, y, paint);
        y += 15;
        paint.setFakeBoldText(false);

        // Grid 2x2 foto kecil
        int imageWidth = (pageWidth - (3 * margin)) / 2;  // dua kolom
        int imageHeight = 150;
        int spacing = 20;
        y += 10;

        // Daftar gambar dan label
        String[] labels = {"Foto Sopir", "Foto Kendaraan", "Foto SIM", "Foto STNK"};
        String[] urls = {fotoSopirUrl, fotoKendaraanUrl, fotoSimUrl, fotoStnkUrl};

        for (int i = 0; i < urls.length; i++) {
            try {
                Bitmap bmp = Glide.with(this).asBitmap().load(urls[i]).submit().get();

                // Skala biar muat di grid
                Bitmap scaled = Bitmap.createScaledBitmap(bmp, imageWidth, imageHeight, true);

                int col = i % 2;
                int row = i / 2;
                int imgX = margin + col * (imageWidth + margin / 2);
                int imgY = y + row * (imageHeight + spacing + 20);

                canvas.drawBitmap(scaled, imgX, imgY, null);

                // Label bawah gambar
                paint.setTextSize(14f);
                paint.setColor(Color.BLACK);
                canvas.drawText(labels[i], imgX, imgY + imageHeight + 15, paint);

                bmp.recycle();
                scaled.recycle();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        pdfDocument.finishPage(page);

        // Simpan PDF
        String fileName = "Detail_Sopir_" + nama + ".pdf";

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Android 10 ke atas → MediaStore
                android.content.ContentResolver resolver = getContentResolver();
                android.content.ContentValues contentValues = new android.content.ContentValues();
                contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri pdfUri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues);
                if (pdfUri != null) {
                    try (java.io.OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                        pdfDocument.writeTo(outputStream);
                    }
                    runOnUiThread(() -> Toast.makeText(this, "PDF disimpan di folder Download", Toast.LENGTH_LONG).show());

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                }
            } else {
                // Android 9 ke bawah
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();
                File file = new File(downloadsDir, fileName);
                pdfDocument.writeTo(new FileOutputStream(file));

                runOnUiThread(() ->
                        Toast.makeText(this, "PDF disimpan di: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
                );

                Uri uri = androidx.core.content.FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Gagal menyimpan PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        } finally {
            pdfDocument.close();
        }
    }

    private int addImagePage(PdfDocument pdfDocument, String url, String label,
                             int pageWidth, int pageHeight) {
        try {
            Bitmap bmp = Glide.with(this).asBitmap().load(url).submit().get();

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight,
                    pdfDocument.getPages().size() + 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            int x = 40, y = 60;

            paint.setTextSize(12f);
            canvas.drawText(label, x, y, paint);
            y += 20;

            // Skala gambar supaya muat di A4
            float scale = (float) (pageWidth - 80) / bmp.getWidth();
            float scaledHeight = bmp.getHeight() * scale;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            matrix.postTranslate(x, y);

            canvas.drawBitmap(bmp, matrix, paint);

            pdfDocument.finishPage(page);

            bmp.recycle();

            return (int) (y + scaledHeight + 20);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 60;
    }
}
