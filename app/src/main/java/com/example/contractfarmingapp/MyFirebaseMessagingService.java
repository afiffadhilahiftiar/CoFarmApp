package com.example.contractfarmingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "db_notifications_channel";
    private static final String TAG = "FCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = "Notifikasi";
        String message = "";

        // Ambil dari notification payload
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        // Ambil dari data payload jika ada
        if (!remoteMessage.getData().isEmpty()) {
            title = remoteMessage.getData().getOrDefault("title", title);
            message = remoteMessage.getData().getOrDefault("message", message);
        }

        showNotification(title, message);
    }

    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Buat channel untuk Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Database Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (manager != null) manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_sci) // ganti sesuai icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token: " + token);
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        String email = getUserEmail(); // ambil email user dari SharedPreferences atau session
        if (email == null || email.isEmpty()) return;

        String url = ApiConfig.BASE_URL + "update_fcm_token.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d(TAG, "Token sent to server: " + response),
                error -> Log.e(TAG, "Failed to send token: " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("fcm_token", token);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    // TODO: implementasikan getUserEmail() untuk ambil email login user
    private String getUserEmail() {
        // Contoh ambil dari SharedPreferences
        return getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("email", null);
    }
}
