package com.example.contractfarmingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.ChatAdapter;
import com.example.contractfarmingapp.adapters.StickerAdapter;
import com.example.contractfarmingapp.models.ChatMessage;
import com.example.contractfarmingapp.network.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class ChatActivityPerusahaan extends AppCompatActivity {

    private TextView namaPetani;
    private RecyclerView recyclerView, stickerGrid;
    private EditText inputMessage;
    private ImageButton btnSend, btnSticker, btnAttachment;
    private ProgressBar uploadProgressBar;
    private int lastMessageCount = 0;
    private boolean isActivityVisible = false;


    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private List<String> stickerList;

    private int receiverId;
    private int adminId;

    private Timer pollingTimer;
    private boolean isUploading = false;
    private Timer uploadPollingTimer;
    private int uploadedMessageCount = -1;

    private static final int FILE_PICK_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inisialisasi UI
        namaPetani = findViewById(R.id.txtNamaPetani);
        recyclerView = findViewById(R.id.recyclerChat);
        inputMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSticker = findViewById(R.id.btnSticker);
        stickerGrid = findViewById(R.id.stickerGrid);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        btnAttachment = findViewById(R.id.btnAttachment);

        btnAttachment.setOnClickListener(v -> openFilePicker());


        // Ambil ID Admin dari SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", null);
        String companyIdStr = sharedPreferences.getString("company_id", null);
        if (companyIdStr == null) {
            Toast.makeText(this, "Company ID tidak ditemukan di SharedPreferences", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        adminId = Integer.parseInt(companyIdStr);

        // Ambil ID dan nama penerima (Petani)
        receiverId = getIntent().getIntExtra("receiver_id", -1);
        String nama = getIntent().getStringExtra("nama_admin");
        namaPetani.setText("Chat dengan " + (nama != null ? nama : "Petani"));

        // Setup chat adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        String pdfPath = getIntent().getStringExtra("pdf_path");
        if (pdfPath != null && !pdfPath.isEmpty()) {
            Uri pdfUri = Uri.parse(pdfPath);
            uploadFile(pdfUri);
        }
        // Kirim pesan teks
        btnSend.setOnClickListener(v -> sendMessage());

        // Tampilkan/Hide grid stiker
        btnSticker.setOnClickListener(v -> {
            if (stickerGrid.getVisibility() == View.GONE) {
                stickerGrid.setVisibility(View.VISIBLE);
            } else {
                stickerGrid.setVisibility(View.GONE);
            }
        });

        // Inisialisasi stiker
        initStickerGrid();

        // Mulai polling pesan
        loadMessages();

    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Pilih file"), FILE_PICK_REQUEST);
    }

    private void uploadFile(Uri fileUri) {
        if (isUploading) return;
        isUploading = true;

        uploadProgressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);
        btnAttachment.setEnabled(false); // Nonaktifkan tombol attachment

        try {
            byte[] fileData;
            if ("content".equals(fileUri.getScheme())) {
                try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                    fileData = readBytesFromInputStream(inputStream);
                }
            } else if ("file".equals(fileUri.getScheme()) || fileUri.getPath() != null) {
                try (InputStream inputStream = new java.io.FileInputStream(fileUri.getPath())) {
                    fileData = readBytesFromInputStream(inputStream);
                }
            } else {
                throw new Exception("URI tidak didukung: " + fileUri);
            }
            String fileName = getFileName(fileUri);
            String url = ApiConfig.BASE_URL + "upload_file_perusahaan.php";

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST, url,
                    response -> {
                        isUploading = false;
                        uploadProgressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        btnAttachment.setEnabled(true); // Aktifkan kembali
                        Toast.makeText(this, "File berhasil dikirim", Toast.LENGTH_SHORT).show();
                        loadMessages();
                        new Handler().postDelayed(() -> loadMessages(), 3000);
                        new Handler().postDelayed(() -> loadMessages(), 5000);
                        new Handler().postDelayed(() -> loadMessages(), 10000);
                        new Handler().postDelayed(() -> loadMessages(), 25000);
                        new Handler().postDelayed(() -> loadMessages(), 20000);
                        new Handler().postDelayed(() -> loadMessages(), 30000);
                        new Handler().postDelayed(() -> loadMessages(), 60000);
                        new Handler().postDelayed(() -> loadMessages(), 120000);
                        new Handler().postDelayed(() -> loadMessages(), 180000);
                    },
                    error -> {
                        isUploading = false;
                        uploadProgressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        btnAttachment.setEnabled(true); // Aktifkan kembali
                        error.printStackTrace();
                        String errMsg = (error.networkResponse != null)
                                ? new String(error.networkResponse.data)
                                : error.getMessage();
                        Toast.makeText(this, "Gagal mengirim file: " + errMsg, Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("sender_id", String.valueOf(adminId));
                    params.put("receiver_id", String.valueOf(receiverId));
                    return params;
                }

                @Override
                public Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("file", new DataPart(fileName, fileData));
                    return params;
                }
            };
            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0, // timeout dalam ms, 0 artinya langsung gagal saat timeout
                    0, // max retry count = 0 agar tidak diulang
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            Volley.newRequestQueue(this).add(multipartRequest);

        } catch (Exception e) {
            isUploading = false;
            uploadProgressBar.setVisibility(View.GONE);
            btnSend.setEnabled(true);
            btnAttachment.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan saat membaca file", Toast.LENGTH_SHORT).show();
        }
    }




    // Membaca semua byte dari InputStream
    private byte[] readBytesFromInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }

        return result;
    }



    private void sendMessage() {
        String messageText = inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) return;

        String url = ApiConfig.BASE_URL + "send_message_perusahaan.php";
        JSONObject body = new JSONObject();

        try {
            body.put("sender_id", adminId);
            body.put("receiver_id", receiverId);
            body.put("message", messageText);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    inputMessage.setText("");
                    loadMessages();
                },
                error -> Toast.makeText(this, "Gagal mengirim pesan", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void sendSticker(String stickerName) {
        String url = ApiConfig.BASE_URL + "send_message_perusahaan.php";
        JSONObject body = new JSONObject();

        try {
            body.put("sender_id", adminId);
            body.put("receiver_id", receiverId);
            body.put("message", "[sticker]" + stickerName);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    stickerGrid.setVisibility(View.GONE);
                    loadMessages();
                },
                error -> Toast.makeText(this, "Gagal mengirim stiker", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void loadMessages() {
        String url = ApiConfig.BASE_URL + "get_messages.php?sender_id=" + adminId + "&receiver_id=" + receiverId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    messageList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int senderId = obj.getInt("sender_id");
                            String messageText = obj.getString("message");
                            long timestamp = Timestamp.valueOf(obj.getString("timestamp")).getTime();

                            boolean isSender = senderId == adminId;
                            ChatMessage message = new ChatMessage(
                                    isSender ? "Admin" : "Petani",
                                    obj.getInt("receiver_id"),
                                    messageText,
                                    timestamp
                            );
                            message.isSender = isSender;
                            messageList.add(message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messageList.size() - 1);
                },
                error -> {
                    // Bisa tambahkan log
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void initStickerGrid() {
        stickerList = new ArrayList<>();

        try {
            String[] files = getAssets().list("stickers");
            if (files != null) {
                stickerList.addAll(Arrays.asList(files));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stickerGrid.setLayoutManager(new GridLayoutManager(this, 4));
        stickerGrid.setAdapter(new StickerAdapter(stickerList, this::sendSticker));
    }

    private void startPollingMessages() {
        if (pollingTimer != null) return;

        pollingTimer = new Timer();
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isActivityVisible && !isFinishing()) {
                    runOnUiThread(ChatActivityPerusahaan.this::loadMessages);
                }
            }
        }, 0, 2000); // setiap 2 detik
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pollingTimer != null) {
            pollingTimer.cancel();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
        if (pollingTimer != null) {
            pollingTimer.cancel();
            pollingTimer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple file (tolak)
                Toast.makeText(this, "Hanya satu file yang boleh dipilih", Toast.LENGTH_SHORT).show();
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                if (!isUploading) {
                    uploadFile(fileUri);
                }
            }
        }
    }




}
