package com.example.contractfarmingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class NotificationService extends Service {

    private static final String CHANNEL_ID_SERVICE = "foreground_service_channel";
    private static final String TAG = "NotificationService";

    private boolean isRunning = false;
    private Set<Integer> notifiedIds = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        // Jangan startForeground di sini
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pastikan startForeground dipanggil di onStartCommand
        Notification notification = getForegroundNotification("Service aktif", "Menunggu notifikasi baru...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);
        } else {
            startForeground(1, notification);
        }

        if (!isRunning) {
            isRunning = true;
            startPolling();
        }

        return START_STICKY;
    }

    private void startPolling() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    String email = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            .getString("email", null);
                    if (email == null) {
                        Log.w(TAG, "Email tidak tersedia, hentikan service sementara");
                        stopSelf();
                        break;
                    }

                    checkNotifications(email);

                    try {
                        Thread.sleep(15000); // polling tiap 15 detik
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Polling thread dihentikan", e);
                        Thread.currentThread().interrupt();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Polling error", e);
                }
            }
        }).start();
    }

    private void checkNotifications(String email) {
        try {
            URL url = new URL(ApiConfig.BASE_URL + "get_notifications.php?email=" + email);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = reader.read()) != -1) sb.append((char) c);
            reader.close();

            JSONObject response = new JSONObject(sb.toString());
            if (response.getBoolean("success")) {
                JSONArray notifications = response.getJSONArray("notifications");
                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject notif = notifications.getJSONObject(i);
                    int id = notif.getInt("id");
                    boolean isRead = notif.getBoolean("isRead");

                    if (!isRead && !notifiedIds.contains(id)) {
                        String title = notif.getString("title");
                        String message = notif.getString("message");

                        NotificationHelper.showNotification(this, title, message);

                        markAsRead(id);
                        notifiedIds.add(id);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "checkNotifications error", e);
        }
    }

    private void markAsRead(int notificationId) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "mark_as_read.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String postData = "id=" + notificationId;
                conn.getOutputStream().write(postData.getBytes());
                conn.getOutputStream().flush();
                conn.getOutputStream().close();
                conn.getInputStream().close();
            } catch (Exception e) {
                Log.e(TAG, "markAsRead error", e);
            }
        }).start();
    }

    private Notification getForegroundNotification(String title, String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.baseline_circle_notifications_24)
                .setOngoing(true) // tidak bisa di swipe
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_SERVICE, "Foreground Service", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
