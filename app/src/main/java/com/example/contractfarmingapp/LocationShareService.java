package com.example.contractfarmingapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

public class LocationShareService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();
    private Runnable locationRunnable;
    private static final int INTERVAL = 10000; // 10 detik
    private static final long STOP_AFTER_MS = 7L * 24 * 60 * 60 * 1000; // 7 hari

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForeground(1, createNotification());

        // Cek waktu mulai, simpan jika belum ada
        long startTime = getSharedPreferences("location_share", MODE_PRIVATE)
                .getLong("start_time", 0);
        if (startTime == 0) {
            getSharedPreferences("location_share", MODE_PRIVATE)
                    .edit()
                    .putLong("start_time", System.currentTimeMillis())
                    .apply();
        }

        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(LocationShareService.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(location -> {
                                if (location != null) {
                                    sendLocationToServer(location);
                                }
                            });
                }

                // Hentikan otomatis jika sudah lewat 7 hari
                long start = getSharedPreferences("location_share", MODE_PRIVATE)
                        .getLong("start_time", 0);
                if (System.currentTimeMillis() - start >= STOP_AFTER_MS) {
                    stopSelf();
                    return;
                }

                handler.postDelayed(this, INTERVAL);
            }
        };
        handler.post(locationRunnable);
    }

    private Notification createNotification() {
        String channelId = "location_share_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Berbagi Lokasi",
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Berbagi Lokasi Aktif")
                .setContentText("Lokasi kamu sedang dibagikan")
                .setSmallIcon(R.drawable.ic_map)
                .setOngoing(true)
                .build();
    }

    private void sendLocationToServer(Location location) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        JSONObject body = new JSONObject();
        try {
            body.put("email", user.getEmail());
            body.put("latitude", location.getLatitude());
            body.put("longitude", location.getLongitude());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                ApiConfig.BASE_URL + "update_location.php",
                body,
                resp -> Log.d("LocationShare", "Lokasi dikirim: " + location),
                err -> Log.e("LocationShare", "Gagal kirim lokasi", err)
        );

        Volley.newRequestQueue(this).add(req);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(locationRunnable);
        getSharedPreferences("location_share", MODE_PRIVATE)
                .edit()
                .remove("start_time")
                .apply();
        Log.d("LocationShare", "Service dihentikan");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
