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
    private Button btnExportPdf;

    private String nama, noHp, kendaraan, platNomor, kapasitas;
    private String fotoSopirUrl, fotoKendaraanUrl, fotoSimUrl, fotoStnkUrl;

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

        // Set text
        tvNama.setText("Nama: " + nama);
        tvNoHp.setText("No HP: " + noHp);
        tvKendaraan.setText("Kendaraan: " + kendaraan);
        tvPlat.setText("Plat Nomor: " + platNomor);
        tvKapasitas.setText("Kapasitas: " + kapasitas + " kg");

        // Load foto menggunakan Glide
        Glide.with(this).load(fotoSopirUrl).into(ivFotoSopir);
        Glide.with(this).load(fotoKendaraanUrl).into(ivFotoKendaraan);
        Glide.with(this).load(fotoSimUrl).into(ivFotoSim);
        Glide.with(this).load(fotoStnkUrl).into(ivFotoStnk);

        btnExportPdf.setOnClickListener(v -> new Thread(this::exportToPdf).start());
    }

    private void exportToPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        int pageWidth = 595;  // A4 (72 dpi)
        int pageHeight = 842;

        int x = 40, y = 60;

        // Buat halaman pertama
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(36f);
        canvas.drawText("Detail Sopir", x, y, paint);
        y += 50;
        paint.setTextSize(19f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(Color.BLACK);

        int startX = 50;          // posisi kiri tabel
        int endX = 550;           // posisi kanan tabel
        int labelWidth = 180;     // lebar kolom label
        int rowHeight = 40;       // tinggi tiap baris
        int startY = y;           // posisi awal tabel
        int currentY = startY;

// 🔹 Data baris tabel
        String[][] data = {
                {"Nama", nama},
                {"No HP", noHp},
                {"Kendaraan", kendaraan},
                {"Plat Nomor", platNomor},
                {"Kapasitas", kapasitas + " kg"}
        };

// 🔹 Hitung tinggi tabel
        int totalRows = data.length;
        int tableHeight = totalRows * rowHeight;

// 🔹 Gambar border luar tabel
        canvas.drawRect(startX, startY, endX, startY + tableHeight, paint);

// 🔹 Gambar garis kolom (pemisah label dan nilai)
        canvas.drawLine(startX + labelWidth, startY, startX + labelWidth, startY + tableHeight, paint);

// 🔹 Gambar garis baris + isi teks
        paint.setStyle(Paint.Style.FILL); // kembali ke isi teks
        paint.setTextSize(18f);
        int textPaddingX = 10;
        int textPaddingY = 25;

        for (int i = 0; i < totalRows; i++) {
            // Gambar teks label dan isi
            canvas.drawText(data[i][0], startX + textPaddingX, currentY + textPaddingY, paint);
            canvas.drawText(": " + data[i][1], startX + labelWidth + textPaddingX, currentY + textPaddingY, paint);

            // Gambar garis bawah antar baris (kecuali baris terakhir)
            if (i < totalRows - 1) {
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(startX, currentY + rowHeight, endX, currentY + rowHeight, paint);
                paint.setStyle(Paint.Style.FILL);
            }

            currentY += rowHeight;
        }

// 🔹 Update posisi Y setelah tabel
        y = startY + tableHeight + 20;
        pdfDocument.finishPage(page);

        // Tambah foto satu per satu ke halaman baru
        y = addImagePage(pdfDocument, fotoSopirUrl, "Foto Sopir", pageWidth, pageHeight);
        y = addImagePage(pdfDocument, fotoKendaraanUrl, "Foto Kendaraan", pageWidth, pageHeight);
        y = addImagePage(pdfDocument, fotoSimUrl, "Foto SIM", pageWidth, pageHeight);
        y = addImagePage(pdfDocument, fotoStnkUrl, "Foto STNK", pageWidth, pageHeight);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Detail_Sopir_" + nama + ".pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            pdfDocument.close();

            runOnUiThread(() ->
                    Toast.makeText(this, "PDF berhasil disimpan: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show()
            );

            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);

        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(this, "Gagal menyimpan PDF", Toast.LENGTH_SHORT).show()
            );
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
