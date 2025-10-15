package com.example.contractfarmingapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
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
import com.example.contractfarmingapp.adapters.PetaniAdapterOfftaker;
import com.example.contractfarmingapp.models.Petani;
import com.example.contractfarmingapp.network.ApiClient;
import com.example.contractfarmingapp.network.UploadService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AdminOfftakerActivity extends AppCompatActivity implements PetaniAdapterOfftaker.OnPetaniActionListener {

    private TextView totalKontrak, totalPetani, totalPanen, statusKontrak;
    private RecyclerView recyclerView;
    private PetaniAdapterOfftaker adapter;
    private List<Petani> listPetani;
    private Button btnUploadPanduan, btnHistori;
    private String API_URL = "";
    private ProgressBar progressBar;
    private ImageView imgPreview;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private String getPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }


    private void pickImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgPreview.setImageURI(selectedImageUri); // atau simpan preview ke variabel agar bisa digunakan nanti
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_offtaker);

        // Ambil oftaker_id dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String oftakerId = prefs.getString("company_id", null); // company_id == oftaker_id

        if (oftakerId == null) {
            Toast.makeText(this, "Offtaker ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        API_URL = ApiConfig.BASE_URL + "get_petani_by_offtaker.php?oftaker_id=" + oftakerId;

        totalKontrak = findViewById(R.id.txtTotalKontrak);
        totalPetani = findViewById(R.id.txtTotalPetani);
        totalPanen = findViewById(R.id.txtTotalPanen);
        statusKontrak = findViewById(R.id.txtStatusKontrak);
        btnUploadPanduan = findViewById(R.id.btnUploadPanduan);
        btnHistori = findViewById(R.id.btnHistori);
        recyclerView = findViewById(R.id.recyclerPetani);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listPetani = new ArrayList<>();
        adapter = new PetaniAdapterOfftaker(listPetani, this);
        recyclerView.setAdapter(adapter);

        loadDataPetaniFromAPI();

        btnUploadPanduan.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadPanduanActivity.class));
        });
        btnHistori.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });
    }

    private void loadDataPetaniFromAPI() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    listPetani.clear();

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);

                            // Gunakan optString dan optInt agar tidak crash jika field kosong/null
                            int id = obj.optInt("id", 0);
                            int userId = obj.optInt("user_id", 0);
                            String nama = obj.optString("nama", "-");
                            String harga = obj.optString("harga", "0");
                            String lahan = obj.optString("lahan", "-");
                            String progres = obj.optString("progres", "-");
                            String catatan = obj.optString("catatan", "-");
                            String companyName = obj.optString("company_name", "-");
                            int companyId = obj.optInt("company_id", 0);
                            int contractId = obj.optInt("contract_id", 0);
                            int oftakerId = obj.optInt("oftaker_id", 0);
                            String status = obj.optString("status", "-");
                            String statusLahan = obj.optString("statusLahan", "-");
                            // Field tambahan dari API baru
                            String kebutuhan = obj.optString("kebutuhan", "-");
                            String satuan = obj.optString("satuan", "-");
                            String waktuDibutuhkan = obj.optString("waktu_dibutuhkan", "-");
                            String ikutAsuransi = obj.optString("ikut_asuransi", "Tidak");
                            String jumlahKebutuhan = obj.optString("jumlah_kebutuhan", "0");
                            String tanggalAjukan = obj.optString("tanggal_ajukan", "-");
                            String statusKlaim = obj.optString("status_klaim", "-");
                            // 🔄 Konversi nilai ikut_asuransi (1 => Ya, null/0 => Tidak)
                            if (ikutAsuransi.equals("1")) {
                                ikutAsuransi = "Ya";
                            } else if (ikutAsuransi.equals("0") || ikutAsuransi.equalsIgnoreCase("null")) {
                                ikutAsuransi = "Tidak";
                            }
                            int countAsuransi = obj.optInt("count_asuransi", 0);

                            Petani p = new Petani(
                                    id, userId, nama, harga, lahan, progres, catatan,
                                    companyName, companyId, contractId, oftakerId,
                                    status, statusLahan, kebutuhan, satuan, waktuDibutuhkan,
                                    ikutAsuransi, jumlahKebutuhan, tanggalAjukan, statusKlaim, countAsuransi
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
                    error.printStackTrace();
                    Toast.makeText(this, "Gagal mengambil data petani", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void loadDashboardInfo() {
        totalKontrak.setText("Kontrak Aktif: " + listPetani.size());
        totalPetani.setText("Total Petani: " + listPetani.size());
        totalPanen.setText("Panen Terkumpul: - kg");
        statusKontrak.setText("Status: Aktif");
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
                            p.catatan = catatan;
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                },
                error -> Toast.makeText(this, "Gagal kirim status", Toast.LENGTH_SHORT).show()
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
    private void kirimStatusValidasiOfftaker(int userId, int contractId, String status, String catatan) {
        String url = ApiConfig.BASE_URL + "update_status_kontrak_offtaker.php";

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

    @Override
    public void onValidasiClick(Petani petani) {
        View view = getLayoutInflater().inflate(R.layout.dialog_validasi, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validasi Kontrak");
        builder.setView(view);
        builder.setPositiveButton("Terima", null);
        builder.setNegativeButton("Tolak", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            EditText editCatatan = view.findViewById(R.id.editCatatan);
            String catatan = editCatatan.getText().toString().trim();
            if (catatan.isEmpty()) {
                Toast.makeText(this, "Harap isi alasan penolakan!", Toast.LENGTH_SHORT).show();
                return;
            }
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Perusahaan tidak memproses kontrak lebih lanjut", catatan);
            dialog.dismiss();
        });

        List<CheckBox> checkBoxList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("check" + i, "id", getPackageName());
            CheckBox checkBox = view.findViewById(resId);
            if (checkBox != null) checkBoxList.add(checkBox);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean semuaCentang = true;
            for (CheckBox cb : checkBoxList) {
                if (!cb.isChecked()) {
                    semuaCentang = false;
                    break;
                }
            }

            if (semuaCentang) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak diterima oleh perusahaan", "");
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Harap centang semua poin validasi!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onPersetujuanKeterlambatan(Petani petani) {
        View view = getLayoutInflater().inflate(R.layout.dialog_validasi_keterlambatan, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Persetujuan keterlambatan");
        builder.setView(view);
        builder.setPositiveButton("Terima", null);
        builder.setNegativeButton("Tolak", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            EditText editCatatan = view.findViewById(R.id.editCatatan);
            String catatan = editCatatan.getText().toString().trim();
            if (catatan.isEmpty()) {
                Toast.makeText(this, "Harap isi alasan penolakan!", Toast.LENGTH_SHORT).show();
                return;
            }
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Permohonan keterlambatan ditolak", catatan);
            dialog.dismiss();
        });

        List<CheckBox> checkBoxList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            int resId = getResources().getIdentifier("check" + i, "id", getPackageName());
            CheckBox checkBox = view.findViewById(resId);
            if (checkBox != null) checkBoxList.add(checkBox);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean semuaCentang = true;
            for (CheckBox cb : checkBoxList) {
                if (!cb.isChecked()) {
                    semuaCentang = false;
                    break;
                }
            }

            if (semuaCentang) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Permohonan keterlambatan diterima", "");
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Harap centang semua poin persetujuan!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void onAjukanGantiRugiClick(Petani petani) {
        // Inflate layout pengajuan ganti rugi
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_klaim_dana, null);
        EditText editJumlah = dialogView.findViewById(R.id.editJumlahDana);
        EditText editAlasan = dialogView.findViewById(R.id.editCatatanDana);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Ajukan Ganti Rugi")
                .setView(dialogView)
                .setPositiveButton("Buat PDF", null)
                .setNegativeButton("Batal", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String jumlahStr = editJumlah.getText().toString().trim();
            String alasan = editAlasan.getText().toString().trim();

            if (jumlahStr.isEmpty()) {
                Toast.makeText(this, "Harap masukkan jumlah kerugian!", Toast.LENGTH_SHORT).show();
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

            // Buat PDF ganti rugi
            File pdfFile = buatPdfGantiRugi(petani, jumlah, alasan);
            if (pdfFile != null) {
                // Kirim PDF ke chat Petani

                kirimPdfKeChatPetani(petani.user_id, petani.companyId, petani.nama, pdfFile);

// Kirim PDF ke chat Poktan
                kirimPdfKeChatPoktan(petani.companyId, petani.companyName, pdfFile);


                dialog.dismiss();
            }
        });
    }
    private File buatPdfGantiRugi(Petani petani, double jumlah, String alasan) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String companyName = sharedPreferences.getString("company_name", "Perusahaan Pertanian");
            String lokasi = sharedPreferences.getString("lokasi", "-");

            File pdfDir = new File(getExternalFilesDir(null), "pdf_ganti_rugi");
            if (!pdfDir.exists()) pdfDir.mkdirs();

            File file = new File(pdfDir, "GantiRugi_" + petani.nama + "_" + System.currentTimeMillis() + ".pdf");

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

            paint.setStrokeWidth(3);
            canvas.drawLine(50, 90, 545, 90, paint);

            // === JUDUL SURAT ===
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("SURAT PENGAJUAN GANTI RUGI", 297, 130, paint);

            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(12);
            paint.setFakeBoldText(false);

            int y = 155;
            int lineHeight = 25;
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

            canvas.drawText("Kepada Yth,", 50, y, paint);
            canvas.drawText("Petani / Poktan " + petani.companyName, 50, y + lineHeight, paint);


            y += 3 * lineHeight;
            canvas.drawText("Dengan hormat,", 50, y, paint);
            canvas.drawText("Dengan ini, kami mengajukan penggantian kerugian sebagai berikut:", 50, y + lineHeight, paint);

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
                    "Nama Petani", "Lahan", "Jumlah Kerugian (Rp)", "Alasan", "Tanggal Pengajuan"
            };
            String[] values = {
                    petani.nama,
                    petani.lahan,
                    jumlahStr,
                    alasan.isEmpty() ? "-" : alasan,
                    sdf.format(new Date())
            };

            for (int i = 0; i < labels.length; i++) {
                int rowY = tableTop + (i * rowHeight) + 20;
                canvas.drawText(labels[i], tableLeft + 10, rowY, paint);
                canvas.drawLine(250, tableTop + (i * rowHeight), 250, tableTop + ((i + 1) * rowHeight), paint);
                canvas.drawText(values[i], 260, rowY, paint);
                if (i > 0) {
                    canvas.drawLine(tableLeft, tableTop + (i * rowHeight), tableRight, tableTop + (i * rowHeight), paint);
                }
            }

            // === PENUTUP SURAT ===
            int endY = tableTop + (6 * rowHeight) + 30;
            canvas.drawText("Demikian pengajuan ganti rugi ini kami sampaikan.", 50, endY, paint);
            canvas.drawText("Atas perhatian dan kerjasamanya, terima kasih.", 50, endY + lineHeight, paint);

            // === TANDA TANGAN ===
            paint.setTextAlign(Paint.Align.RIGHT);
            int signY = endY + 100;
            canvas.drawText("Hormat Kami,", 540, signY, paint);
            canvas.drawText(petani.nama, 540, signY + 60, paint);
            canvas.drawLine(420, signY + 65, 540, signY + 65, paint);

            document.finishPage(page);
            document.writeTo(new FileOutputStream(file));
            document.close();

            Toast.makeText(this, "PDF ganti rugi berhasil dibuat", Toast.LENGTH_SHORT).show();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal membuat PDF ganti rugi", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
    private void kirimPdfKeChatPetani(int petaniId, int companyId, String nama, File pdfFile) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiver_id", petaniId);
        intent.putExtra("nama_petani", nama);
        intent.putExtra("company_id", companyId);
        intent.putExtra("pdf_path", pdfFile.getAbsolutePath());
        startActivity(intent);
    }

    private void kirimPdfKeChatPoktan(int poktanId, String nama, File pdfFile) {
        Intent intent = new Intent(this, ChatActivityPerusahaan.class);
        intent.putExtra("receiver_id", poktanId);
        intent.putExtra("pdf_path", pdfFile.getAbsolutePath());
        intent.putExtra("nama_admin", nama);
        startActivity(intent);
    }

    @Override
    public void onChatClick(Petani petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Tujuan Chat");

        builder.setPositiveButton("Chat Petani", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiver_id", petani.user_id);
            intent.putExtra("nama_petani", petani.nama);
            intent.putExtra("company_id", petani.companyId);
            startActivity(intent);
        });

        builder.setNegativeButton("Chat Poktan", (dialog, which) -> {
            Intent intent = new Intent(this, ChatActivityPerusahaan.class);
            intent.putExtra("nama_admin", petani.companyName);
            intent.putExtra("receiver_id", petani.companyId); // ubah ke String
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
    @Override
    public void onLihatLahanClick(Petani petani) {
        Intent intent = new Intent(this, MapPetaniActivity.class);
        intent.putExtra("tipe", "petani");
        intent.putExtra("company_id", String.valueOf(petani.companyId)); // Pastikan dikirim sebagai String jika perlu
        intent.putExtra("user_id", String.valueOf(petani.user_id));      // (opsional) jika perlu
        startActivity(intent);
    }
    @Override
    public void onKontrakSelesaiClick(Petani petani) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_kontrak_selesai, null);

        CheckBox checkBox1 = dialogView.findViewById(R.id.checkbox1);
        CheckBox checkBox2 = dialogView.findViewById(R.id.checkbox2);
        Button btnUploadFoto = dialogView.findViewById(R.id.btnUploadFoto);
        Button btnBayar = dialogView.findViewById(R.id.btnBayar);
        ProgressBar progressBarDialog = dialogView.findViewById(R.id.progressBarBayar);
        imgPreview = dialogView.findViewById(R.id.imgPreview);

        // Tambahan Grade & Persenan
        Spinner spinnerGrade = dialogView.findViewById(R.id.spinnerGrade);
        EditText etPersentase = dialogView.findViewById(R.id.etPersentase);
// Batasi input antara 0 - 100
        etPersentase.setFilters(new InputFilter[]{
                new InputFilterMinMax(0, 100)
        });

        // Isi spinner dengan daftar grade
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_3,
                new String[]{"Grade A", "Grade B", "Grade C", "Gagal Panen"}
        );
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGrade.setAdapter(gradeAdapter);

        // Listener spinner untuk Grade
        spinnerGrade.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGrade = parent.getItemAtPosition(position).toString();

                if (selectedGrade.equals("Grade A")) {
                    etPersentase.setText("0"); // 0% potongan
                    etPersentase.setEnabled(false);
                } else if (selectedGrade.equals("Grade B") || selectedGrade.equals("Grade C")) {
                    getGradeValuesFromServer(petani.contract_id, etPersentase, selectedGrade);
                } else if (selectedGrade.equals("Gagal Panen")) {
                    etPersentase.setText("100");
                    etPersentase.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Konfirmasi Selesai Kontrak")
                .setNegativeButton("Batal", null)
                .show();

        btnBayar.setOnClickListener(v -> {
            String selectedGrade = spinnerGrade.getSelectedItem().toString();
            String persenPotonganStr = etPersentase.getText().toString().trim();

            if (!checkBox1.isChecked() || !checkBox2.isChecked() || persenPotonganStr.isEmpty()) {
                Toast.makeText(this, "Harap centang semua dan isi potongan", Toast.LENGTH_SHORT).show();
                return;
            }

            int persenPotongan = Integer.parseInt(persenPotonganStr);
            int hargaDasar = Integer.parseInt(petani.harga.replace(".", ""));
            int hargaSetelahPotongan;

            if (selectedGrade.equals("Gagal Panen")) {
                hargaSetelahPotongan = 0; // tidak ada pembayaran
            } else {
                hargaSetelahPotongan = hargaDasar - (hargaDasar * persenPotongan / 100);
            }

            createInvoicePetani(hargaSetelahPotongan, petani, progressBarDialog);
        });

        btnUploadFoto.setOnClickListener(v -> {
            // Trigger Intent untuk pilih gambar
            pickImageFromGallery();
        });
    }


    public void onKontrakLogistikPetani(Petani petani) {
        Intent intent = new Intent(this, DaftarSopirActivity.class);
        intent.putExtra("company_id", petani.companyId); // Kirim company_id petani
        startActivity(intent);
    }

    public void onKontrakLogistikClick(Petani petani) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Metode Logistik");

        // Layout sederhana dengan RadioGroup
        View view = getLayoutInflater().inflate(R.layout.dialog_logistik, null);
        builder.setView(view);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupLogistik);
        RadioButton rbSendiri = view.findViewById(R.id.rbLogistikSendiri);
        RadioButton rbPetani = view.findViewById(R.id.rbLogistikPetani);

        builder.setPositiveButton("Kirim", null);
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Harap pilih metode logistik!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == rbSendiri.getId()) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Pengiriman barang dilakukan", "");
            } else if (selectedId == rbPetani.getId()) {
                kirimStatusValidasi(petani.user_id, petani.contract_id, "Menunggu persetujuan admin poktan", "");
            }

            dialog.dismiss();
        });
    }

    public void onKontrakSampaiClick(Petani petani) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_upload_foto, null);
        ImageView imgPreviewDialog = dialogView.findViewById(R.id.imgPreview);
        Button btnUploadFoto = dialogView.findViewById(R.id.btnUploadFoto);
        Button btnKirim = dialogView.findViewById(R.id.btnKirim);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Kirim Bukti Sampai")
                .setNegativeButton("Batal", null)
                .create();

        dialog.show();

        // Override tombol Batal
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());

        // Gunakan variabel global selectedImageUri
        btnUploadFoto.setOnClickListener(v -> pickImageFromGallery());

        btnKirim.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Harap upload foto bukti!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kirim status "Pesanan sampai"
            kirimStatusValidasi(petani.user_id, petani.contract_id, "Kontrak selesai", "");
            // Upload foto bukti
            uploadSelesaiKontrak(petani, selectedImageUri);
            dialog.dismiss();
        });

        // Simpan preview ke dialog saat gambar dipilih
        this.imgPreview = imgPreviewDialog;
    }

    public void onUpdateSopir(Petani petani) {
        // Buat dialog konfirmasi
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Penjemputan");
        builder.setMessage("Apakah sopir sedang dalam penjemputan barang?");
        builder.setPositiveButton("Ya", (dialog, which) -> {
            // Kirim status validasi ke server
            kirimStatusValidasi(
                    petani.user_id,        // ID user/petani
                    petani.contract_id,    // ID kontrak
                    "Sedang dalam perjalanan", // status baru
                    ""                     // catatan kosong
            );
            dialog.dismiss();
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        // Tampilkan dialog
        builder.show();
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

    public void onPesananSampai(Petani petani) {
        // Buat dialog konfirmasi (opsional)
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Pesanan");
        builder.setMessage("Apakah pesanan sudah sampai?");
        builder.setPositiveButton("Ya", (dialog, which) -> {
            // Kirim status validasi "Pesanan sampai"
            kirimStatusValidasi(
                    petani.user_id,
                    petani.contract_id,
                    "Pesanan sampai",
                    "" // catatan kosong
            );
            dialog.dismiss();
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void uploadSelesaiKontrak(Petani petani, Uri imageUri) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String loggedInUserId = prefs.getString("company_id", null); // Ini ID dari pengguna yang login (admin offtaker)
        File file = new File(getPathFromUri(imageUri));
        RequestBody requestFile = RequestBody.create(
                file, okhttp3.MediaType.parse("image/*")
        );

        MultipartBody.Part fotoPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);
        RequestBody petaniId = RequestBody.create(String.valueOf(petani.id), okhttp3.MediaType.parse("text/plain"));
        RequestBody userId = RequestBody.create(loggedInUserId, okhttp3.MediaType.parse("text/plain")); // Diganti ke loggedInUserId

        RequestBody companyId = RequestBody.create(String.valueOf(petani.companyId), okhttp3.MediaType.parse("text/plain"));
        RequestBody catatan = RequestBody.create("Kontrak selesai oleh perusahaan", okhttp3.MediaType.parse("text/plain"));

        UploadService service = ApiClient.getClient().create(UploadService.class);
        Call<ResponseBody> call = service.uploadKontrakSelesai(petaniId, userId, companyId, catatan, fotoPart);


        call.enqueue(new retrofit2.Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Upload berhasil!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Upload gagal: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void kirimUlasanKeServer(Petani petani, float rating, String ulasan) {
        String url = ApiConfig.BASE_URL + "insert_ulasan.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Ulasan berhasil dikirim!", Toast.LENGTH_SHORT).show();
                    // Update status kontrak jadi Review selesai
                    kirimStatusValidasiOfftaker(petani.user_id, petani.contract_id, "Review offtaker selesai", "");
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
                params.put("company_id", String.valueOf(petani.companyId));
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

    private void createInvoicePetani(int amount, Petani petani, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = (user != null) ? user.getEmail() : null;

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Email pengguna tidak tersedia", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (amount < 1) {
            Toast.makeText(this, "Jumlah pembayaran tidak valid", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        String url = ApiConfig.BASE_URL + "create_invoice.php";

        new Thread(() -> {
            try {
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                String postData = "amount=" + amount
                        + "&email=" + URLEncoder.encode(email, "UTF-8")
                        + "&contract_id=" + petani.contract_id
                        + "&user_id=" + petani.user_id;

                OutputStream os = con.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());

                String invoiceUrl = jsonResponse.getString("invoice_url");
                String externalId = jsonResponse.getString("external_id");

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(this, WebViewActivity.class);
                    intent.putExtra("invoice_url", invoiceUrl);
                    intent.putExtra("external_id", externalId);
                    intent.putExtra("harga", petani.harga);
                    intent.putExtra("payment_method", "Xendit");
                    startActivity(intent);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal membuat invoice", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
    private void getGradeValuesFromServer(int contractId, EditText etPersentase, String selectedGrade) {
        String url = ApiConfig.BASE_URL +"get_contract_grades.php?contract_id=" + contractId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            int gradeB = jsonObject.getInt("gradeB");
                            int gradeC = jsonObject.getInt("gradeC");

                            if (selectedGrade.equals("Grade B")) {
                                etPersentase.setText(String.valueOf(gradeB));
                            } else if (selectedGrade.equals("Grade C")) {
                                etPersentase.setText(String.valueOf(gradeC));
                            }
                            etPersentase.setEnabled(false); // dikunci agar user tidak ubah manual
                        } else {
                            Toast.makeText(this, "Gagal ambil data grade", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Koneksi gagal", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    public class InputFilterMinMax implements InputFilter {
        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public InputFilterMinMax(String min, String max) {
            this.min = Integer.parseInt(min);
            this.max = Integer.parseInt(max);
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            try {
                String newVal = dest.toString().substring(0, dstart) + source.toString() + dest.toString().substring(dend);
                if (newVal.isEmpty()) return null;
                int input = Integer.parseInt(newVal);
                if (isInRange(min, max, input)) return null;
            } catch (NumberFormatException nfe) {}
            return "";
        }

        private boolean isInRange(int min, int max, int value) {
            return value >= min && value <= max;
        }
    }


}
