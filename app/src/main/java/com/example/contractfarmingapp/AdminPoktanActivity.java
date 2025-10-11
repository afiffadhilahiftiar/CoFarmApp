package com.example.contractfarmingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.contractfarmingapp.activities.ContractDetailActivity;
import com.example.contractfarmingapp.adapters.PetaniAdapter;
import com.example.contractfarmingapp.models.Petani;

import org.json.*;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminPoktanActivity extends AppCompatActivity implements PetaniAdapter.OnPetaniActionListener {

    private TextView totalKontrak, totalPetani, totalPanen, statusKontrak;
    private RecyclerView recyclerView;
    private PetaniAdapter adapter;
    private List<Petani> listPetani;
    private Button btnUploadPanduan;

    private String API_URL = ""; // Awalnya kosong, nanti akan diisi di onCreate

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_poktan);

        // Ambil company_id dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int companyId = sharedPreferences.getInt("company_id", -1);

        if (companyId == -1) {
            Toast.makeText(this, "Company ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi URL dengan companyId valid
        API_URL = ApiConfig.BASE_URL + "get_petani_by_admin.php?company_id=" + companyId;


        // Inisialisasi view
        totalKontrak = findViewById(R.id.txtTotalKontrak);
        totalPetani = findViewById(R.id.txtTotalPetani);
        totalPanen = findViewById(R.id.txtTotalPanen);
        statusKontrak = findViewById(R.id.txtStatusKontrak);
        btnUploadPanduan = findViewById(R.id.btnUploadPanduan);
        recyclerView = findViewById(R.id.recyclerPetani);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listPetani = new ArrayList<>();
        adapter = new PetaniAdapter(listPetani, this);
        recyclerView.setAdapter(adapter);

        // Ambil data petani dari server
        loadDataPetaniFromAPI();

        btnUploadPanduan.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadPanduanActivity.class);
            startActivity(intent);
        });
    }

    private void loadDataPetaniFromAPI() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    listPetani.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            Petani p = new Petani(
                                    obj.getInt("id"),
                                    obj.getInt("user_id"),
                                    obj.getString("nama"),
                                    obj.optString("harga", "0"), // default "0" kalau null
                                    obj.getString("lahan"),
                                    obj.optString("progres", "-"),
                                    obj.optString("catatan", ""),
                                    obj.getString("company_name"),
                                    obj.getInt("company_id"),
                                    obj.getInt("contract_id"),
                                    obj.getInt("oftaker_id"),
                                    obj.optString("status", "Belum ada status"),
                                    obj.optString("statusLahan", "Belum ada status"),
                                    obj.optString("kebutuhan", "-"),
                                    obj.optString("satuan", "-"),
                                    obj.optString("waktuDibutuhkan", "-"),
                                    obj.optString("ikut_asuransi", "Tidak"),
                                    obj.optString("jumlahKebutuhan", "0"),
                                    obj.optString("tanggal_ajukan", "-")
                            );

                            listPetani.add(p);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    loadDashboardInfo();
                },
                error -> {
                    Toast.makeText(this, "Gagal mengambil data petani", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }


    private void loadDashboardInfo() {
        totalKontrak.setText("Kontrak Aktif: " + listPetani.size());
        totalPetani.setText("Total Petani: " + listPetani.size());
        totalPanen.setText("Panen Terkumpul: - kg"); // Ganti dengan API jika tersedia
        statusKontrak.setText("Status: Berjalan");
    }
    private void kirimStatusValidasi(int userId, int contractId, String status, String catatan) {
        String url = ApiConfig.BASE_URL + "update_status_kontrak.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Status berhasil dikirim: " + status, Toast.LENGTH_SHORT).show();

                    for (int i = 0; i < listPetani.size(); i++) {
                        Petani p = listPetani.get(i);
                        if (p.user_id == userId && p.contract_id == contractId) {
                            p.status = status;
                            p.catatan = catatan; // simpan catatan jika kamu ingin ditampilkan
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> Toast.makeText(this, "Gagal kirim status: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("contract_id", String.valueOf(contractId));
                params.put("status", status);
                params.put("catatan", catatan);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    @Override
    public void onValidasiClick(Petani petani) {
        // Inflate layout dialog_validasi
        View view = getLayoutInflater().inflate(R.layout.dialog_validasi, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validasi Kontrak");
        builder.setView(view);

        builder.setPositiveButton("Terima", null); // Kita override di bawah
        builder.setNegativeButton("Tolak", null); // override setelah show()


        AlertDialog dialog = builder.create();
        dialog.show();
// Tangani tombol Tolak (Negative) setelah dialog ditampilkan
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            EditText editCatatan = view.findViewById(R.id.editCatatan);
            String catatan = editCatatan.getText().toString().trim();

            if (catatan.isEmpty()) {
                Toast.makeText(this, "Harap isi alasan penolakan!", Toast.LENGTH_SHORT).show();
                return;
            }

            kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak ditolak admin poktan", catatan);
            dialog.dismiss();
        });

        // Ambil semua checkbox
        List<CheckBox> checkBoxList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("check" + i, "id", getPackageName());
            CheckBox checkBox = view.findViewById(resId);
            if (checkBox != null) checkBoxList.add(checkBox);
        }

        // Override tombol positif setelah dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean semuaCentang = true;
            for (CheckBox cb : checkBoxList) {
                if (!cb.isChecked()) {
                    semuaCentang = false;
                    break;
                }
            }

            if (semuaCentang) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak divalidasi admin poktan", ""); // catatan kosong

                dialog.dismiss();
            } else {
                Toast.makeText(this, "Harap centang semua poin validasi!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Tambahkan method baru di AdminPoktanActivity
    public void onKlaimDanaClick(Petani petani) {
        // Ambil fasilitatorId dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fasilitatorId = sharedPreferences.getString("fasilitator_id", null);
        String companyName = sharedPreferences.getString("company_name", null);
        String lokasi = sharedPreferences.getString("lokasi", null);

        if (fasilitatorId == null || fasilitatorId.isEmpty()) {
            Toast.makeText(this, "ID fasilitator tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }

        // Inflate layout klaim dana
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_klaim_dana, null);
        EditText editJumlah = dialogView.findViewById(R.id.editJumlahDana);
        EditText editCatatan = dialogView.findViewById(R.id.editCatatanDana);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Klaim Dana")
                .setView(dialogView)
                .setPositiveButton("Buat PDF", null) // override nanti
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String jumlahStr = editJumlah.getText().toString().trim();
            String catatan = editCatatan.getText().toString().trim();

            if (jumlahStr.isEmpty()) {
                Toast.makeText(this, "Harap masukkan jumlah klaim!", Toast.LENGTH_SHORT).show();
                return;
            }

            double jumlah;
            try {
                jumlah = Double.parseDouble(jumlahStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Jumlah tidak valid!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (jumlah <= 0) {
                Toast.makeText(this, "Jumlah harus lebih besar dari 0!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Buat PDF
            File pdfFile = buatPdfKlaim(petani, jumlah, catatan);
            if (pdfFile != null) {
                // Kirim PDF ke chat fasilitator
                kirimPdfKeChat(fasilitatorId, pdfFile);
            }

            dialog.dismiss();
        });
    }
    @Override
    public void onItemClick(Petani petani) {
        Intent intent = new Intent(this, FormKontrakActivity.class);

        intent.putExtra("nama_pengguna", petani.nama);
        intent.putExtra("user_id", petani.user_id);
        intent.putExtra("nama_perusahaan", petani.companyName);
        intent.putExtra("contract_id", String.valueOf(petani.contract_id));

        // Data kebutuhan
        intent.putExtra("kebutuhan", petani.kebutuhan);
        intent.putExtra("jumlah_kebutuhan", petani.jumlahKebutuhan);
        intent.putExtra("satuan", petani.satuan);
        intent.putExtra("waktu_dibutuhkan", petani.waktuDibutuhkan);

        // Data lahan dan status
        intent.putExtra("lahan", petani.lahan);
        intent.putExtra("status_lahan", petani.statusLahan);
        intent.putExtra("satuan", petani.satuan);
        // Data tambahan
        intent.putExtra("catatan", petani.catatan);
        intent.putExtra("tanggal_pengajuan", petani.tanggalAjukan);

        // Tentukan ikut_asuransi: 1 -> "Ya", null/0 -> "Tidak"
        String asuransi = (petani.ikutAsuransi != null && petani.ikutAsuransi.equals("1")) ? "Ya" : "Tidak";
        intent.putExtra("ikut_asuransi", asuransi);

        startActivity(intent);
    }




    /** Method untuk membuat PDF klaim dana */
    private File buatPdfKlaim(Petani petani, double jumlah, String catatan) {
        try {
            // Ambil nama perusahaan dan lokasi dari SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String companyName = sharedPreferences.getString("company_name", "Perusahaan Pertanian");
            String lokasi = sharedPreferences.getString("lokasi", "-");

            File pdfDir = new File(getExternalFilesDir(null), "pdf_klaim");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File file = new File(pdfDir, "Klaim_" + petani.nama + "_" + System.currentTimeMillis() + ".pdf");

            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4
            PdfDocument.Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // === KOP SURAT ===
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(20);
            paint.setFakeBoldText(true);
            canvas.drawText(companyName.toUpperCase(), 297, 60, paint);

            paint.setTextSize(14);
            paint.setFakeBoldText(false);
            canvas.drawText("Lokasi: " + lokasi, 297, 80, paint);

            // Garis pembatas
            paint.setStrokeWidth(3);
            canvas.drawLine(50, 90, 545, 90, paint);

            // === JUDUL SURAT ===
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("FORMULIR PENGAJUAN KLAIM DANA", 297, 130, paint);

            paint.setFakeBoldText(false);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(12);

            int y = 155;
            int lineHeight = 25;
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

            // === PEMBUKA SURAT ===
            canvas.drawText("Kepada Yth,", 50, y, paint);
            canvas.drawText("Manajer Fasilitator Keuangan", 50, y + lineHeight, paint);

            y += (3 * lineHeight);
            canvas.drawText("Dengan hormat,", 50, y, paint);
            canvas.drawText("Dengan ini, kami mengajukan bantuan dana atas kegiatan pertanian yang dilakukan oleh:", 50, y + lineHeight, paint);

            // === TABEL INFORMASI ===
            int tableTop = y + (3 * lineHeight);
            int tableLeft = 50;
            int tableRight = 545;
            int rowHeight = 30;

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            canvas.drawRect(tableLeft, tableTop, tableRight, tableTop + (5 * rowHeight), paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.LEFT);
            NumberFormat rupiahFormat = NumberFormat.getInstance(new Locale("id", "ID"));
            String jumlahStr = "Rp " + rupiahFormat.format(jumlah);
            String[] labels = {
                    "Nama Petani", "Lahan", "Jumlah Klaim (Rp)", "Alasan", "Tanggal Pengajuan"
            };
            String[] values = {
                    petani.nama,
                    petani.lahan,
                    jumlahStr,
                    catatan.isEmpty() ? "-" : catatan,
                    sdf.format(new Date())
            };

            for (int i = 0; i < labels.length; i++) {
                int rowY = tableTop + (i * rowHeight) + 20;
                // Label kiri
                canvas.drawText(labels[i], tableLeft + 10, rowY, paint);
                // Garis tengah
                canvas.drawLine(250, tableTop + (i * rowHeight), 250, tableTop + ((i + 1) * rowHeight), paint);
                // Nilai kanan
                canvas.drawText(values[i], 260, rowY, paint);
                // Garis antar baris
                if (i > 0) {
                    canvas.drawLine(tableLeft, tableTop + (i * rowHeight), tableRight, tableTop + (i * rowHeight), paint);
                }
            }

            // === PENUTUP SURAT ===
            int endY = tableTop + (6 * rowHeight) + 30;
            canvas.drawText("Demikian pengajuan klaim ini kami sampaikan. Atas perhatian dan kerjasamanya,", 50, endY, paint);
            canvas.drawText("kami ucapkan terima kasih.", 50, endY + lineHeight, paint);

            // === TANDA TANGAN ===
            paint.setTextAlign(Paint.Align.RIGHT);
            int signY = endY + 100;
            canvas.drawText("Hormat Kami,", 540, signY, paint);
            canvas.drawText(petani.nama, 540, signY + 60, paint);
            canvas.drawLine(420, signY + 65, 540, signY + 65, paint);

            document.finishPage(page);

            // Simpan ke file
            document.writeTo(new FileOutputStream(file));
            document.close();

            Toast.makeText(this, "PDF klaim berhasil dibuat", Toast.LENGTH_SHORT).show();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF klaim", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    /** Method untuk mengirim PDF ke chat fasilitator */
    private void kirimPdfKeChat(String fasilitatorId, File pdfFile) {
        Intent intent = new Intent(this, ChatActivityFasilitator.class);
        intent.putExtra("fasilitator_id", fasilitatorId);
        intent.putExtra("pdf_path", pdfFile.getAbsolutePath());
        startActivity(intent);
    }


    public void onUpdateSopir(Petani petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Status Sopir");
        builder.setMessage("Apakah sopir sudah dalam perjalanan?");

        builder.setPositiveButton("Ya", (dialog, which) -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Sedang dalam perjalanan", "");
            Toast.makeText(this, "Status dikirim: Sedang dalam perjalanan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    public void onValidasiLogistikClick(Petani petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validasi Logistik");
        builder.setMessage("Apakah bersedia pengiriman dilakukan dari pihak poktan?");

        builder.setPositiveButton("Setuju", null); // override setelah show()
        builder.setNegativeButton("Tolak", null);   // override setelah show()
        builder.setNeutralButton("Batal", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Tombol Setuju
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Pengiriman barang dilakukan", "");
            Toast.makeText(this, "Status dikirim: Pengiriman barang dilakukan", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // Tombol Tolak
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Pengiriman barang ditolak admin poktan", "");
            Toast.makeText(this, "Status dikirim: Pengiriman barang ditolak", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }


    @Override
    public void onChatClick(Petani petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Tujuan Chat");

        builder.setPositiveButton("Chat Petani", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiver_id", petani.user_id); // ID petani
            intent.putExtra("nama_petani", petani.nama);
            intent.putExtra("company_id", petani.companyId);
            startActivity(intent);
        });

        builder.setNegativeButton("Chat Perusahaan", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiver_id", petani.oftaker_id); // ID oftaker/perusahaan
            intent.putExtra("nama_petani", petani.companyName); // opsional
            intent.putExtra("company_id", petani.companyId);
            startActivity(intent);
        });

        builder.setNeutralButton("Batal", null);
        builder.show();
    }

    @Override
    public void onLihatKontrakClick(Petani petani) {
        Intent intent = new Intent(this, ContractDetailActivity.class);
        intent.putExtra("tipe", "petani");
        intent.putExtra("contract_id", String.valueOf(petani.contract_id));
        startActivity(intent);
    }
    public void onBeriUlasan(Petani petani) {
        // Inflate layout untuk ulasan
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_berikan_ulasan, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText editUlasan = dialogView.findViewById(R.id.editUlasan);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Beri Ulasan")
                .setView(dialogView)
                .setPositiveButton("Kirim", null) // override nanti
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        // Override tombol Kirim supaya bisa validasi input
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String ulasan = editUlasan.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Harap beri rating bintang!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ulasan.isEmpty()) {
                Toast.makeText(this, "Harap isi ulasan!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kirim ulasan ke server
            kirimUlasanKeServer(petani, rating, ulasan);
            dialog.dismiss();
        });
    }
    private void kirimStatusValidasiPoktan(int userId, int contractId, String status, String catatan) {
        String url = ApiConfig.BASE_URL + "update_status_kontrak_poktan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Opsional: parsing response JSON
                    Toast.makeText(this, "Status berhasil diupdate: " + status, Toast.LENGTH_SHORT).show();
                    // Update status lokal di listPetani
                    for (int i = 0; i < listPetani.size(); i++) {
                        Petani p = listPetani.get(i);
                        if (p.user_id == userId && p.contract_id == contractId) {
                            p.status = status;
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> {
                    Toast.makeText(this, "Gagal update status", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("contract_id", String.valueOf(contractId));
                params.put("status", status);
                params.put("catatan", catatan);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void kirimUlasanKeServer(Petani petani, float rating, String ulasan) {
        String url = ApiConfig.BASE_URL + "insert_ulasan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Ulasan berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    // Update status kontrak jadi Review selesai
                    kirimStatusValidasiPoktan(petani.user_id, petani.contract_id, "Review poktan selesai", "");
                },
                error -> {
                    Toast.makeText(this, "Gagal mengirim ulasan", Toast.LENGTH_SHORT).show();
                    error.printStackTrace(); // tampilkan error detail di Logcat
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String loggedInUserId = prefs.getString("user_id", null);

                Map<String, String> params = new HashMap<>();
                params.put("company_id", String.valueOf(petani.oftaker_id));
                params.put("user_id", loggedInUserId);
                params.put("contract_id", String.valueOf(petani.contract_id));
                params.put("rating", String.valueOf(rating));
                params.put("ulasan", ulasan);
                // jangan kirim created_at, biar database handle otomatis pakai NOW()
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
