package com.example.contractfarmingapp;

import android.Manifest;
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
import com.mapbox.geojson.Point;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.List;

public class MapForegroundService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();
    private Runnable locationRunnable;

    private List<MapActivity.GeofenceArea> geofenceAreas;
    private List<String> pelangganEmails;
    private String companyId, companyName;
    private String currentGeofenceName = null;
    private boolean isWaitingEnter = false, isWaitingExit = false;
    private Runnable enterRunnable, exitRunnable;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Ambil data dari ServiceDataHolder
        geofenceAreas = ServiceDataHolder.getInstance().getGeofenceAreas();
        pelangganEmails = ServiceDataHolder.getInstance().getEmails();
        companyId = ServiceDataHolder.getInstance().getCompanyId();
        companyName = ServiceDataHolder.getInstance().getCompanyName();

        startForeground(1, createNotification());

        // Update lokasi setiap 5 detik
        locationRunnable = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MapForegroundService.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(location -> {
                                if (location != null) {
                                    Point point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
                                    checkUserLocationAgainstPolygons(point);
                                }
                            });
                }
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(locationRunnable);
    }

    private android.app.Notification createNotification() {
        String channelId = "map_service_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Map Tracking", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Tracking Lokasi Aktif")
                .setContentText("Map tracking berjalan di latar belakang")
                .setSmallIcon(R.drawable.ic_map)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(locationRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) return user.getEmail();
        return "unknown@example.com";
    }

    private void checkUserLocationAgainstPolygons(Point userLocation) {
        String email = getUserEmail();
        boolean insideAnyPolygon = false;
        String enteredAreaName = null;

        for (MapActivity.GeofenceArea area : geofenceAreas) {
            if (pointInPolygon(userLocation, area.polygon)) {
                insideAnyPolygon = true;
                enteredAreaName = area.name;
                break;
            }
        }

        if (insideAnyPolygon) {
            if (isWaitingExit && exitRunnable != null) {
                handler.removeCallbacks(exitRunnable);
                isWaitingExit = false;
            }

            if (!enteredAreaName.equals(currentGeofenceName)) {
                if (!isWaitingEnter) {
                    isWaitingEnter = true;
                    final String finalEnteredAreaName = enteredAreaName;
                    enterRunnable = () -> {
                        currentGeofenceName = finalEnteredAreaName;
                        String msg = email + " dari perusahaan " + companyName + " memasuki lahan: " + currentGeofenceName;
                        sendNotificationToServer(getAllTargetEmails(), "Masuk Area", msg);
                        isWaitingEnter = false;
                        Log.d("GeoService", msg);
                    };
                    handler.postDelayed(enterRunnable, 120000);
                }
            }
        } else {
            if (isWaitingEnter && enterRunnable != null) {
                handler.removeCallbacks(enterRunnable);
                isWaitingEnter = false;
            }

            if (currentGeofenceName != null && !isWaitingExit) {
                isWaitingExit = true;
                String exitedAreaName = currentGeofenceName;

                exitRunnable = () -> {
                    String msg = email + " dari perusahaan " + companyName + " keluar dari lahan: " + exitedAreaName;
                    sendNotificationToServer(getAllTargetEmails(), "Keluar Area", msg);
                    currentGeofenceName = null;
                    isWaitingExit = false;
                    Log.d("GeoService", msg);
                };
                handler.postDelayed(exitRunnable, 120000);
            }
        }
    }

    private List<String> getAllTargetEmails() {
        List<String> all = pelangganEmails;
        String firebaseEmail = getUserEmail();
        if (!all.contains(firebaseEmail)) all.add(firebaseEmail);
        return all;
    }

    private void sendNotificationToServer(List<String> emails, String title, String message) {
        for (String email : emails) {
            JSONObject body = new JSONObject();
            try {
                body.put("email", email);
                body.put("title", title);
                body.put("message", message);
            } catch (Exception e) { e.printStackTrace(); }

            JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,
                    ApiConfig.BASE_URL + "notification_geofence.php",
                    body,
                    resp -> Log.d("NOTIF", "Sent to " + email),
                    err -> Log.e("NOTIF", "Failed " + email + " : " + err)
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            Volley.newRequestQueue(this).add(req);
        }
    }

    private boolean pointInPolygon(Point point, List<Point> polygon) {
        int intersectCount = 0;
        for (int j = 0; j < polygon.size() - 1; j++) {
            double lat1 = polygon.get(j).latitude();
            double lon1 = polygon.get(j).longitude();
            double lat2 = polygon.get(j + 1).latitude();
            double lon2 = polygon.get(j + 1).longitude();

            double lat = point.latitude();
            double lon = point.longitude();

            if (((lat1 > lat) != (lat2 > lat)) &&
                    (lon < (lon2 - lon1) * (lat - lat1) / (lat2 - lat1) + lon1)) {
                intersectCount++;
            }
        }
        return (intersectCount % 2 == 1);
    }
}
