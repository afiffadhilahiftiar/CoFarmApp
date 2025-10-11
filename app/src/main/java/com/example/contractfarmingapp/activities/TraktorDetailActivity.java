package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

public class TraktorDetailActivity extends AppCompatActivity {

    private TextView tvJenisTraktor, tvKapasitas, tvNamaOperator, tvNoHp;
    private ImageView ivFotoTraktor;
    private Button btnExportPdf;

    private String jenisTraktor, kapasitas, namaOperator, noHp;
    private String fotoTraktorUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traktor_detail);

        tvJenisTraktor = findViewById(R.id.tvDetailJenisTraktor);
        tvKapasitas = findViewById(R.id.tvDetailKapasitas);
        tvNamaOperator = findViewById(R.id.tvDetailNamaOperator);
        tvNoHp = findViewById(R.id.tvDetailNoHp);
        ivFotoTraktor = findViewById(R.id.ivFotoTraktor);
        btnExportPdf = findViewById(R.id.btnExportPdf);

        // Ambil data dari Intent
        Intent intent = getIntent();
        jenisTraktor = intent.getStringExtra("jenis_traktor");
        kapasitas = intent.getStringExtra("kapasitas");
        namaOperator = intent.getStringExtra("nama_operator");
        noHp = intent.getStringExtra("no_hp");
        fotoTraktorUrl = intent.getStringExtra("foto_traktor");

        // Set text
        tvJenisTraktor.setText("Jenis Traktor: " + jenisTraktor);
        tvKapasitas.setText("Kapasitas: " + kapasitas + " kg");
        tvNamaOperator.setText("Operator: " + namaOperator);
        tvNoHp.setText("No HP: " + noHp);

        // Load foto
        Glide.with(this).load(fotoTraktorUrl).into(ivFotoTraktor);

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
        canvas.drawText("Detail Traktor", x, y, paint);
        y += 50;

        paint.setTextSize(19f);
        canvas.drawText("Jenis Traktor: " + jenisTraktor, x, y, paint); y += 30;
        canvas.drawText("Kapasitas: " + kapasitas + " kg", x, y, paint); y += 30;
        canvas.drawText("Operator: " + namaOperator, x, y, paint); y += 30;
        canvas.drawText("No HP: " + noHp, x, y, paint); y += 40;

        pdfDocument.finishPage(page);

        // Tambah halaman foto traktor
        addImagePage(pdfDocument, fotoTraktorUrl, "Foto Traktor", pageWidth, pageHeight);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Detail_Traktor_" + jenisTraktor + ".pdf");

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

    private void addImagePage(PdfDocument pdfDocument, String url, String label,
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
