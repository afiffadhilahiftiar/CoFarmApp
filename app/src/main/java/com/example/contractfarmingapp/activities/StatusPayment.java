package com.example.contractfarmingapp.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.MainActivity;
import com.example.contractfarmingapp.R;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Locale;

public class StatusPayment extends AppCompatActivity {

    private TextView emailTextView, statusTitle, statusDescription;
    private MaterialButton goHomeButton;
    private Button btnRefresh;
    private ImageView backButton;
    private ProgressBar loadingIndicator;

    private static final String BASE_URL = "http://192.168.1.27:8080/contractfarming/get_status_payment.php";

    private Handler handler = new Handler();
    private Runnable statusChecker;
    private String lastKnownStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_payment);

        initViews();
        setupButtons();

        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            emailTextView.setText(email);

            // Tampilkan loading terlebih dahulu selama 5 detik
            loadingIndicator.setVisibility(View.VISIBLE);
            statusTitle.setText("Memuat...");
            statusDescription.setText("Sedang memeriksa status pembayaran, mohon tunggu...");

            // Delay selama 5 detik sebelum fetch dan polling
            handler.postDelayed(() -> {
                fetchPaymentStatus(email);
                startStatusPolling(email);
            }, 6000);

        } else {
            emailTextView.setText("Email tidak tersedia");
            statusTitle.setText("Gagal");
            statusDescription.setText("Email tidak ditemukan. Silakan coba lagi.");
            loadingIndicator.setVisibility(View.GONE);
        }
    }


    private void initViews() {
        emailTextView = findViewById(R.id.emailTextView);
        statusTitle = findViewById(R.id.statusTitle);
        statusDescription = findViewById(R.id.statusDescription);
        goHomeButton = findViewById(R.id.goHomeButton);
        backButton = findViewById(R.id.backButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        btnRefresh = findViewById(R.id.btnRefresh);
    }

    private void setupButtons() {
        goHomeButton.setOnClickListener(v -> {
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });

        backButton.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {
            String email = emailTextView.getText().toString();
            if (email != null && !email.isEmpty() && !email.equals("Email tidak tersedia")) {
                fetchPaymentStatus(email);
            } else {
                Toast.makeText(this, "Email tidak valid, tidak bisa refresh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPaymentStatus(String email) {
        loadingIndicator.setVisibility(View.VISIBLE);

        String url = BASE_URL + "?email=" + Uri.encode(email);
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    loadingIndicator.setVisibility(View.GONE);
                    handleResponse(response);
                },
                error -> {
                    loadingIndicator.setVisibility(View.GONE);
                    statusTitle.setText("Gagal Terhubung");
                    statusDescription.setText("Periksa koneksi atau server.\n" + error.getMessage());
                });

        queue.add(request);
    }

    private void handleResponse(JSONObject response) {
        try {
            if (response.getBoolean("success")) {
                JSONObject data = response.getJSONObject("data");
                String status = data.getString("status");
                int amount = data.getInt("amount");
                String createdAt = data.getString("created_at");
// Format amount
                NumberFormat numberFormat = NumberFormat.getInstance(new Locale("in", "ID"));
                String formattedAmount = numberFormat.format(amount);
                // tampilkan status
                statusTitle.setText("Status: " + status);
                statusDescription.setText("Jumlah  : Rp " + formattedAmount + "\nTanggal : " + createdAt);

                // notifikasi jika berubah jadi approved
                if (!lastKnownStatus.equals(status)) {
                    if (status.equalsIgnoreCase("approved")) {
                        showLocalNotification("Top-Up Disetujui", "Saldo sebesar Rp " + formattedAmount + " telah disetujui.");
                    }
                    lastKnownStatus = status;
                }

                // ubah warna
                switch (status.toLowerCase()) {
                    case "approved":
                        statusTitle.setTextColor(getColor(R.color.hijau));
                        break;
                    case "rejected":
                        statusTitle.setTextColor(getColor(R.color.red));
                        break;
                    default:
                        statusTitle.setTextColor(getColor(R.color.yellow));
                        break;
                }
            } else {
                statusTitle.setText("Tidak Ada Data");
                statusDescription.setText(response.optString("message", "—"));
            }
        } catch (JSONException e) {
            statusTitle.setText("Kesalahan");
            statusDescription.setText("Gagal parsing data: " + e.getMessage());
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocalNotification(String title, String message) {
        String channelId = "approval_status_channel";
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(this, StatusPayment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24) // Ganti dengan icon kamu
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Status Approval", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        manager.notify(1001, builder.build());
    }

    private void startStatusPolling(String email) {
        statusChecker = new Runnable() {
            @Override
            public void run() {
                fetchPaymentStatus(email);
                handler.postDelayed(this, 10000); // cek tiap 15 detik
            }
        };
        handler.post(statusChecker);
    }

    private void stopStatusPolling() {
        handler.removeCallbacks(statusChecker);
    }

    @Override
    protected void onDestroy() {
        stopStatusPolling();
        super.onDestroy();
    }
}
