package com.example.contractfarmingapp.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
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
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

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

        Intent intent = getIntent();
        jenisTraktor = intent.getStringExtra("jenis_traktor");
        kapasitas = intent.getStringExtra("kapasitas");
        namaOperator = intent.getStringExtra("nama_operator");
        noHp = intent.getStringExtra("no_hp");
        fotoTraktorUrl = intent.getStringExtra("foto_traktor");

        tvJenisTraktor.setText("Jenis Traktor: " + jenisTraktor);
        tvKapasitas.setText("Kapasitas: " + kapasitas + " kg");
        tvNamaOperator.setText("Operator: " + namaOperator);
        tvNoHp.setText("No HP: " + noHp);

        Glide.with(this).load(fotoTraktorUrl).into(ivFotoTraktor);

        btnExportPdf.setOnClickListener(v -> new Thread(this::exportToPdf).start());
    }

    private void exportToPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        int pageWidth = 595; // A4
        int pageHeight = 842;
        int margin = 40;
        int y = 80;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Judul
        paint.setTextSize(24f);
        paint.setColor(Color.BLACK);
        paint.setFakeBoldText(true);
        canvas.drawText("Laporan Detail Traktor", margin, y, paint);
        y += 40;

        paint.setFakeBoldText(false);
        paint.setTextSize(16f);

        // Data traktor (tabel sederhana)
        String[][] data = {
                {"Jenis Traktor", jenisTraktor},
                {"Kapasitas", kapasitas + " kg"},
                {"Operator", namaOperator},
                {"No HP", noHp}
        };

        int startX = margin;
        int endX = pageWidth - margin;
        int labelWidth = 160;
        int rowHeight = 30;

        // Gambar garis tabel luar
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(startX, y, endX, y + data.length * rowHeight, paint);
        // Garis pemisah kolom
        canvas.drawLine(startX + labelWidth, y, startX + labelWidth, y + data.length * rowHeight, paint);

        // Isi teks
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < data.length; i++) {
            int textY = y + (i * rowHeight) + 20;
            canvas.drawText(data[i][0], startX + 10, textY, paint);
            canvas.drawText(": " + data[i][1], startX + labelWidth + 10, textY, paint);
            if (i < data.length - 1) {
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(startX, y + (i + 1) * rowHeight, endX, y + (i + 1) * rowHeight, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }

        y += data.length * rowHeight + 40;

        // Tambahkan foto dalam tabel mini
        try {
            Bitmap bmp = Glide.with(this).asBitmap().load(fotoTraktorUrl).submit().get();

            int imgSize = 150; // ukuran kecil
            Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, imgSize, imgSize, true);

            // Kotak label dan gambar
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(startX, y, endX, y + imgSize + 30, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(14f);
            canvas.drawText("Foto Traktor:", startX + 10, y + 20, paint);

            canvas.drawBitmap(scaledBmp, startX + 150, y + 10, paint);
            scaledBmp.recycle();
            bmp.recycle();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        pdfDocument.finishPage(page);

        String fileName = "Detail_Traktor_" + jenisTraktor + ".pdf";

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (gunakan MediaStore)
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri pdfUri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues);
                if (pdfUri != null) {
                    try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                        pdfDocument.writeTo(outputStream);
                    }
                    runOnUiThread(() ->
                            Toast.makeText(this, "PDF disimpan di folder Download", Toast.LENGTH_LONG).show()
                    );

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

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
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
}
