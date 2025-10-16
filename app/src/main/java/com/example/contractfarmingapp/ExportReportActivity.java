package com.example.contractfarmingapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// ... imports
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.contractfarmingapp.adapters.ImageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ExportReportActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 2001;
    private Bitmap scaleBitmapProportionally(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = (float) height / (float) width;
        int newHeight = Math.round(maxWidth * ratio);
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }

    EditText etTanggal, etDeskripsi, etNamaOfftaker, etLokasiPoktan, etIdPoktan, etNamaPoktan, etNamaPetani, etContractId, etKebutuhan, etLahan;
    Button btnPilihFoto, btnExportPdf, btnKirimEmail, btnKirimAdmin;
    File savedPdfFile;

    Spinner spinnerKegiatan;

    RecyclerView recyclerFoto;
    String statusLahan;
    ArrayList<Bitmap> fotoList = new ArrayList<>();
    ImageAdapter adapter;
    private void writeFormField(DataOutputStream stream, String boundary, String name, String value) throws IOException {
        stream.writeBytes("--" + boundary + "\r\n");
        stream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
        stream.writeBytes(value + "\r\n");
    }

    private void writeFileField(DataOutputStream stream, String boundary, String fieldName, File file) throws IOException {
        String fileName = file.getName();
        stream.writeBytes("--" + boundary + "\r\n");
        stream.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"\r\n");
        stream.writeBytes("Content-Type: application/pdf\r\n\r\n");

        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            stream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        stream.writeBytes("\r\n");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_report);
        etNamaPetani = findViewById(R.id.etNamaPetani);
        etContractId = findViewById(R.id.etContractId);
        etKebutuhan = findViewById(R.id.etKebutuhan);
        etLahan = findViewById(R.id.etLahan);
        etNamaOfftaker = findViewById(R.id.etNamaOfftaker);
        etLokasiPoktan = findViewById(R.id.etLokasiPoktan);
        etIdPoktan = findViewById(R.id.etIdPoktan);
        etNamaPoktan = findViewById(R.id.etNamaPoktan);
        Intent intent = getIntent();
        if (intent != null) {
            etNamaPetani.setText(intent.getStringExtra("nama_petani"));
            etNamaOfftaker.setText(intent.getStringExtra("nama_perusahaan"));
            etNamaPoktan.setText(intent.getStringExtra("nama_poktan"));
            etLokasiPoktan.setText(intent.getStringExtra("lokasi_poktan"));
            int idPoktan = intent.getIntExtra("id_poktan", 0); // 0 = default kalau null
            etIdPoktan.setText(String.valueOf(idPoktan));
            etContractId.setText(intent.getStringExtra("contract_id"));
            etKebutuhan.setText(intent.getStringExtra("kebutuhan"));
            etLahan.setText(intent.getStringExtra("lahan"));
        }
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String companyEmail = prefs.getString("company_email", "");
        int senderId = prefs.getInt("id", -1);
        int receiverId = prefs.getInt("company_id", -1); // atau id perusahaan
        etTanggal = findViewById(R.id.etTanggal);
        etTanggal.setFocusable(false); // agar tidak muncul keyboard
        etTanggal.setOnClickListener(v -> showDatePicker());

        spinnerKegiatan = findViewById(R.id.spinnerKegiatan);

// Set adapter dari string-array
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.jenis_kegiatan, R.layout.spinner_item_2);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKegiatan.setAdapter(spinnerAdapter);
        spinnerKegiatan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();

                switch (selected) {
                    case "Pengolahan Tanah (Siap Tanam)":
                        etDeskripsi.setHint("Catat: jenis olah tanah, alat yang digunakan, pupuk dasar, tenaga kerja.");
                        break;

                    case "Persiapan Kandang":
                        etDeskripsi.setHint("Catat: jenis kandang, bahan konstruksi, kapasitas, sanitasi.");
                        break;

                    case "Persiapan Kolam":
                        etDeskripsi.setHint("Catat: jenis kolam (tanah/terpal), ukuran, kualitas air, perlakuan awal.");
                        break;

                    case "Pengadaan Bibit":
                        etDeskripsi.setHint("Catat: sumber bibit/benih, jumlah, kualitas, tanggal penerimaan.");
                        break;

                    case "Pemberian Pakan":
                        etDeskripsi.setHint("Catat: jenis pakan, jumlah/dosis, frekuensi pemberian.");
                        break;

                    case "Vaksinasi dan Pengobatan":
                        etDeskripsi.setHint("Catat: jenis vaksin/obat, dosis, metode aplikasi, tanggal pemberian.");
                        break;

                    case "Penanaman":
                        etDeskripsi.setHint("Catat: varietas/benih, jumlah bibit/benih, metode tanam.");
                        break;

                    case "Serangan Hama dan Penyakit":
                        etDeskripsi.setHint("Catat: jenis hama/penyakit, tingkat serangan (%), gejala, tindakan pengendalian.");
                        break;

                    case "Perawatan":
                        etDeskripsi.setHint("Catat: jenis perawatan, jenis obat, dosis, metode aplikasi, jadwal penyiraman/penyemprotan/pembersihan.");
                        break;

                    case "Panen":
                        etDeskripsi.setHint("Catat hasil panen: Bruto, Tara, Netto, atau jumlahnya, kadar air, dll.");
                        break;

                    case "Penggilingan":
                        etDeskripsi.setHint("Catat: jumlah hasil, kapasitas mesin, hasil giling.");
                        break;

                    case "Penyosohan":
                        etDeskripsi.setHint("Catat: hasil, rendemen, kualitas.");
                        break;

                    case "Pengemasan":
                        etDeskripsi.setHint("Catat: jenis kemasan, ukuran, label, jumlah kemasan.");
                        break;

                    case "Pengiriman":
                        etDeskripsi.setHint("Catat: tujuan pengiriman, jumlah, tanggal, transportasi.");
                        break;

                    case "Selesai":
                        etDeskripsi.setHint("Kegiatan kontrak sudah selesai.");
                        break;

                    case "Tidak Aktif":
                        etDeskripsi.setHint("Kegiatan sedang tidak aktif.");
                        break;

                    default:
                        etDeskripsi.setHint("Deskripsi kegiatan");
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                etDeskripsi.setHint("Deskripsi kegiatan");
            }
        });

        etDeskripsi = findViewById(R.id.etDeskripsi);
        btnPilihFoto = findViewById(R.id.btnPilihFoto);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        btnKirimAdmin = findViewById(R.id.btnKirimAdmin);
        btnKirimAdmin.setOnClickListener(v -> {
            if (savedPdfFile == null || !savedPdfFile.exists()) {
                Toast.makeText(this, "PDF belum tersedia", Toast.LENGTH_SHORT).show();
                return;
            }



            if (String.valueOf(senderId).isEmpty() || String.valueOf(receiverId).isEmpty()) {
                Toast.makeText(this, "ID pengguna atau perusahaan kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            String uploadUrl = ApiConfig.BASE_URL + "upload_message_pdf.php";

            new Thread(() -> {
                try {
                    String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                    HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

                    // Tambah sender_id dan receiver_id
                    writeFormField(outputStream, boundary, "sender_id", String.valueOf(senderId));
                    writeFormField(outputStream, boundary, "receiver_id", String.valueOf(receiverId));


                    // Tambah file PDF
                    writeFileField(outputStream, boundary, "pdf_file", savedPdfFile);

                    outputStream.writeBytes("--" + boundary + "--\r\n");
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == 200) {
                        runOnUiThread(() -> Toast.makeText(this, "Laporan berhasil dikirim ke admin", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Gagal kirim: " + responseCode, Toast.LENGTH_SHORT).show());
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        recyclerFoto = findViewById(R.id.recyclerFoto);

        adapter = new ImageAdapter(fotoList);
        recyclerFoto.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recyclerFoto.setAdapter(adapter);

        btnPilihFoto.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(pickIntent, REQUEST_IMAGE_PICK);
        });

        btnExportPdf.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                statusLahan = spinnerKegiatan.getSelectedItem().toString();

                if (statusLahan.isEmpty()) {
                    Toast.makeText(this, "Pilih jenis kegiatan", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Kirim email & statusLahan ke server (MySQL)


                // Setelah kirim, generate PDF
                generatePdf();

                if (savedPdfFile == null || !savedPdfFile.exists()) {
                    Toast.makeText(this, "PDF belum tersedia", Toast.LENGTH_SHORT).show();
                    return;
                }



                if (String.valueOf(senderId).isEmpty() || String.valueOf(receiverId).isEmpty()) {
                    Toast.makeText(this, "ID pengguna atau perusahaan kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                String uploadUrl = ApiConfig.BASE_URL + "upload_message_pdf.php";

                new Thread(() -> {
                    try {
                        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                        HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                        DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

                        // Tambah sender_id dan receiver_id
                        writeFormField(outputStream, boundary, "sender_id", String.valueOf(senderId));
                        writeFormField(outputStream, boundary, "receiver_id", String.valueOf(receiverId));


                        // Tambah file PDF
                        writeFileField(outputStream, boundary, "pdf_file", savedPdfFile);

                        outputStream.writeBytes("--" + boundary + "--\r\n");
                        outputStream.flush();
                        outputStream.close();

                        int responseCode = conn.getResponseCode();
                        if (responseCode == 200) {
                            runOnUiThread(() -> Toast.makeText(this, "Laporan berhasil dikirim ke admin", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(this, "Gagal kirim: " + responseCode, Toast.LENGTH_SHORT).show());
                        }
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
            } else {
                Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show();
            }
        });

        btnKirimEmail = findViewById(R.id.btnKirimEmail);
        btnKirimEmail.setOnClickListener(v -> {
            if (savedPdfFile != null) {
                sendEmailWithAttachment(savedPdfFile);
            } else {
                Toast.makeText(this, "PDF belum tersedia", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == REQUEST_IMAGE_PICK && res == RESULT_OK && data != null) {
            fotoList.clear();

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 5);
                for (int i = 0; i < count; i++) {
                    try {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        Bitmap resized = scaleBitmapProportionally(bmp, 500); // max width 500px
                        fotoList.add(resized);

                    } catch (IOException e) { e.printStackTrace(); }
                }
            } else if (data.getData() != null) {
                try {
                    Uri uri = data.getData();
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    fotoList.add(Bitmap.createScaledBitmap(bmp, 500, 300, true));
                } catch (IOException e) { e.printStackTrace(); }
            }

            adapter.notifyDataSetChanged();
        }
    }

    private void generatePdf() {
        String tgl = etTanggal.getText().toString();
        String keg = spinnerKegiatan.getSelectedItem().toString();
        String des = etDeskripsi.getText().toString();

        if (tgl.isEmpty() || keg.isEmpty() || des.isEmpty()) {
            Toast.makeText(this, "Lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument doc = new PdfDocument();
        Paint paint = new Paint();
        int pageWidth = 595, pageHeight = 842;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo_watermark);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 100, 100, true);  // ukuran bisa disesuaikan

        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        // Watermark di tengah halaman, transparan
        Paint watermarkPaint = new Paint();
        watermarkPaint.setAlpha(255); // transparansi: 0 = transparan, 255 = solid

        int rightTopX = pageWidth - scaledLogo.getWidth() - 40;  // 40dp padding kanan
        int rightTopY = 30;  // padding atas

        canvas.drawBitmap(scaledLogo, rightTopX, rightTopY, watermarkPaint);



        int y = 40;
        paint.setTextSize(16f);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Nama Petani: " + etNamaPetani.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Nama Perusahaan: " + etNamaOfftaker.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Kelompok Tani: " + etNamaPoktan.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Lokasi: " + etLokasiPoktan.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Id Poktan: " + etIdPoktan.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Contract ID: " + etContractId.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Lahan: " + etLahan.getText().toString(), 40, y, paint); y += 20;
        canvas.drawText("Kebutuhan: " + etKebutuhan.getText().toString(), 40, y, paint); y += 30;


// ✅ Teks judul bold dan besar
        paint.setTextSize(20f);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Laporan Kegiatan Pertanian", 150, y, paint);
        y += 30;

// 🔄 Kembalikan ke normal
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.DEFAULT);

        canvas.drawText("Tanggal: " + tgl, 40, y, paint); y += 20;
        canvas.drawText("Kegiatan: " + keg, 40, y, paint); y += 20;
        canvas.drawText("Deskripsi:", 40, y, paint); y += 20;


        for (String line : des.split("\n")) {
            canvas.drawText(line, 60, y, paint);
            y += 20;
        }

        y += 10;
        for (Bitmap bmp : fotoList) {
            if (y + 300 > pageHeight) {
                doc.finishPage(page);
                page = doc.startPage(new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, doc.getPages().size() + 1).create());
                canvas = page.getCanvas();
                y = 40;
                canvas.drawBitmap(scaledLogo, rightTopX, rightTopY, watermarkPaint);
            }
            canvas.drawBitmap(bmp, 40, y, paint);
            y += bmp.getHeight() + 20;
        }

        doc.finishPage(page);

        try {
            // Ambil nama petani dari EditText
            String namaPetani = etNamaPetani.getText().toString().trim();

            // Bersihkan nama dari karakter yang tidak boleh ada di nama file
            namaPetani = namaPetani.replaceAll("[^a-zA-Z0-9_\\-]", "_");

            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ContractFarming");
            if (!dir.exists()) dir.mkdirs();

            // Gunakan nama petani di nama file
            savedPdfFile = new File(dir, "Laporan_" + namaPetani + "_" + System.currentTimeMillis() + ".pdf");

            doc.writeTo(new FileOutputStream(savedPdfFile));
            doc.close();

            btnKirimEmail.setVisibility(View.VISIBLE); // tampilkan tombol kirim email

            Toast.makeText(this, "PDF disimpan: " + savedPdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", savedPdfFile);
            openIntent.setDataAndType(uri, "application/pdf");
            openIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            updateStatusLahanToServer(statusLahan);

            try {
                startActivity(openIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Tidak ada aplikasi untuk membuka PDF", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal simpan PDF", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format: dd-MM-yyyy
                    String formattedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                            selectedDay, selectedMonth + 1, selectedYear);
                    etTanggal.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
    private void sendEmailWithAttachment(File file) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String companyEmail = prefs.getString("company_email", "");
        String companyId = prefs.getString("company_id", "");

        if (companyEmail == null || companyEmail.isEmpty()) {
            Toast.makeText(this, "Email perusahaan tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("application/pdf");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{companyEmail});
        String kegiatanDipilih = spinnerKegiatan.getSelectedItem().toString();
        String subjekEmail = "Laporan Kegiatan " + kegiatanDipilih;
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjekEmail);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Berikut terlampir laporan kegiatan dari petani.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(emailIntent, "Kirim email dengan..."));
        } catch (Exception e) {
            Toast.makeText(this, "Gagal membuka aplikasi email", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateStatusLahanToServer(String statusLahan) {
        String lahan =  getIntent().getStringExtra("lahan");

        if (lahan.isEmpty()) {
            Toast.makeText(this, "Lahan tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = ApiConfig.BASE_URL + "update_status_lahan.php";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, response.trim(), Toast.LENGTH_LONG).show(); // tampilkan respon asli
                },
                error -> {
                    Toast.makeText(this, "Gagal update status lahan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("lahan", lahan.trim());

                params.put("statuslahan", statusLahan);
                return params;
            }
        };

        queue.add(request);
    }








}
