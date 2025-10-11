package com.example.contractfarmingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationHelper {

    private static final String CHANNEL_ID = "db_notifications_channel";

    // SharedPreferences untuk mencatat ID terakhir
    private static int getLastShownId(Context context) {
        return context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
                .getInt("lastShownId", 0);
    }

    private static void setLastShownId(Context context, int id) {
        context.getSharedPreferences("notif_prefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("lastShownId", id)
                .apply();
    }

    // Tampilkan notifikasi
    public static void showNotification(Context context, String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Database Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_sci)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Polling notifikasi terbaru
    public static void pollNotifications(Context context, String email) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "get_is_notifications.php?email=" + email);
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
                    int lastId = getLastShownId(context);

                    for (int i = 0; i < notifications.length(); i++) {
                        JSONObject notif = notifications.getJSONObject(i);
                        int notifId = notif.getInt("id");

                        // Hanya tampilkan yang ID > lastId
                        if (notifId > lastId) {
                            String title = notif.getString("title");
                            String message = notif.getString("message");

                            showNotification(context, title, message);

                            // Tandai sudah dibaca di server
                            markAsRead(notifId);

                            // Update lastShownId
                            setLastShownId(context, notifId);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void markAsRead(int notificationId) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "mark_as_read.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                String postData = "id=" + notificationId;
                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();
                conn.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
