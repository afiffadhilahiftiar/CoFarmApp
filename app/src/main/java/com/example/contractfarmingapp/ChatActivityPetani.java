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
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.contractfarmingapp.adapters.ChatAdapter;
import com.example.contractfarmingapp.adapters.StickerAdapter;
import com.example.contractfarmingapp.models.ChatMessage;
import com.example.contractfarmingapp.network.VolleyMultipartRequest;

import org.json.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

public class ChatActivityPetani extends AppCompatActivity {

    private TextView namaAdmin;
    private RecyclerView recyclerView, stickerGrid;
    private EditText inputMessage;
    private ImageButton btnSend, btnSticker, btnAttachment;
    private ProgressBar uploadProgressBar;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private List<String> stickerList;

    private int adminId;
    private int petaniId;


    private Timer pollingTimer;
    private boolean isUploading = false;
    private int lastMessageCount = 0;
    private boolean isActivityVisible = false;

    private static final int FILE_PICK_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inisialisasi UI
        namaAdmin = findViewById(R.id.txtNamaPetani);
        recyclerView = findViewById(R.id.recyclerChat);
        inputMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnSticker = findViewById(R.id.btnSticker);
        btnAttachment = findViewById(R.id.btnAttachment);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        stickerGrid = findViewById(R.id.stickerGrid);

        // Ambil ID petani
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", null);
        String companyId = sharedPreferences.getString("company_id", null);
        if (userIdStr == null) {
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        petaniId = Integer.parseInt(userIdStr);
        adminId = Integer.parseInt(companyId);
        String nama = getIntent().getStringExtra("nama_admin");
        namaAdmin.setText("Chat dengan " + (nama != null ? nama : "Admin"));

        // Setup chat
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttachment.setOnClickListener(v -> openFilePicker());
        btnSticker.setOnClickListener(v -> {
            stickerGrid.setVisibility(
                    stickerGrid.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
        });

        initStickerGrid();
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
        btnAttachment.setEnabled(false);

        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            byte[] fileData = readBytesFromInputStream(inputStream);
            inputStream.close();

            String fileName = getFileName(fileUri);
            String url = ApiConfig.BASE_URL + "upload_file_admin.php";

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST, url,
                    response -> {
                        isUploading = false;
                        uploadProgressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        btnAttachment.setEnabled(true);
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
                        btnAttachment.setEnabled(true);
                        String errMsg = (error.networkResponse != null)
                                ? new String(error.networkResponse.data)
                                : error.getMessage();
                        Toast.makeText(this, "Gagal kirim file: " + errMsg, Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("sender_id", String.valueOf(petaniId));
                    params.put("receiver_id", String.valueOf(adminId));
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
                    0, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            Volley.newRequestQueue(this).add(multipartRequest);

        } catch (Exception e) {
            isUploading = false;
            uploadProgressBar.setVisibility(View.GONE);
            btnSend.setEnabled(true);
            btnAttachment.setEnabled(true);
            e.printStackTrace();
            Toast.makeText(this, "Gagal membaca file", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readBytesFromInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data)) != -1) {
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

        String url = ApiConfig.BASE_URL + "send_message_admin.php";
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", petaniId);
            body.put("receiver_id", adminId);
            body.put("message", messageText);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private void loadMessages() {
        String url = ApiConfig.BASE_URL + "get_messages.php?sender_id=" + petaniId + "&receiver_id=" + adminId;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() != lastMessageCount) {
                        messageList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                int senderId = obj.getInt("sender_id");
                                String messageText = obj.getString("message");
                                long timestamp = Timestamp.valueOf(obj.getString("timestamp")).getTime();

                                boolean isSender = senderId == petaniId;
                                ChatMessage message = new ChatMessage(
                                        isSender ? "Petani" : "Admin",
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
                        lastMessageCount = response.length();
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    }
                },
                error -> {
                    // Bisa tambahkan log error
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

    private void sendSticker(String stickerName) {
        String url = ApiConfig.BASE_URL + "send_message_admin.php";
        JSONObject body = new JSONObject();
        try {
            body.put("sender_id", petaniId);
            body.put("receiver_id", adminId);
            body.put("message", "[sticker]" + stickerName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> loadMessages(),
                error -> Toast.makeText(this, "Gagal mengirim stiker", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void startPollingMessages() {
        if (pollingTimer != null) return;

        pollingTimer = new Timer();
        pollingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isActivityVisible && !isFinishing()) {
                    runOnUiThread(ChatActivityPetani.this::loadMessages);
                }
            }
        }, 0, 2000); // 2 detik
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
    protected void onDestroy() {
        super.onDestroy();
        if (pollingTimer != null) pollingTimer.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                Toast.makeText(this, "Hanya satu file yang boleh dipilih", Toast.LENGTH_SHORT).show();
            } else if (data.getData() != null) {
                Uri fileUri = data.getData();
                if (!isUploading) uploadFile(fileUri);
            }
        }
    }
}
