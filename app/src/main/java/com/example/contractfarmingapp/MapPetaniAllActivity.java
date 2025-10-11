package com.example.contractfarmingapp;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapPetaniAllActivity extends AppCompatActivity {
    public static class GeofenceArea {
        String name;
        String komoditas;
        String statusLahan;
        List<Point> polygon;
        String color;

        GeofenceArea(String name, String komoditas, String statusLahan, List<Point> polygon, String color) {
            this.name = name;
            this.komoditas = komoditas;
            this.statusLahan = statusLahan;
            this.polygon = polygon;
            this.color = color;
        }
    }


    private String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getEmail();
        } else {
            return "unknown@example.com";
        }
    }
    private List<String> pelangganEmails = new ArrayList<>();
    private List<String> getAllTargetEmails() {
        List<String> all = new ArrayList<>(pelangganEmails); // salin semua pelanggan
        String firebaseEmail = getUserEmail();
        if (!all.contains(firebaseEmail)) {
            all.add(firebaseEmail); // tambahkan email Firebase jika belum ada
        }
        return all;
    }
    private enum LabelMode {
        NAME,
        KOMODITAS,
        STATUS
    }private Location myLocation = null;
    private LabelMode currentLabelMode = LabelMode.NAME;
    private List<GeofenceArea> filteredAreas = new ArrayList<>();

    private String getLabelText(String name, String komoditas, String statusLahan) {
        switch (currentLabelMode) {
            case KOMODITAS: return komoditas;
            case STATUS: return statusLahan;
            case NAME:
            default: return name;
        }
    }

    private MapView mapView;
    private MapboxMap mapboxMap;
    private String companyId;
    private String companyName;
    private Handler geofenceHandler = new Handler();
    private Runnable enterRunnable = null;
    private Runnable exitRunnable = null;
    private boolean isWaitingEnter = false;
    private boolean isWaitingExit = false;

    FloatingActionButton floatingActionButton;
    private FusedLocationProviderClient fusedLocationClient;
    private PolygonAnnotationManager polygonAnnotationManager;
    private List<GeofenceArea> geofenceAreas = new ArrayList<>();
    private String currentGeofenceName = null; // untuk deteksi keluar area

    private Point calculateCentroid(List<Point> points) {
        double lonSum = 0.0;
        double latSum = 0.0;
        int total = points.size();

        for (Point point : points) {
            lonSum += point.longitude();
            latSum += point.latitude();
        }

        return Point.fromLngLat(lonSum / total, latSum / total);
    }
    private com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager pointAnnotationManager;
    private final ActivityResultLauncher<String> activityResultLauncher =registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(MapPetaniAllActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapPetaniAllActivity.this, "Permission not Granted", Toast.LENGTH_SHORT).show();
            }
        }
    });
    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };
    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(15.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };
    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            floatingActionButton.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };
    private Handler locationHandler = new Handler();
    private Runnable locationUpdater;

    // Fungsi untuk memuat ulang lokasi pengguna lain (manual refresh)
    private void startOtherUsersLocationUpdates() {
        locationUpdater = new Runnable() {
            @Override
            public void run() {
                fetchAndDisplayOtherUsers();
                locationHandler.postDelayed(this, 5000);
            }
        };
        locationHandler.post(locationUpdater);
    }

    private boolean isSharingLocation = false;
    private boolean showUserMarkers = true;
    private Switch switchShareLocation, switchShowMarkers;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Map<String, PointAnnotation> userMarkers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        findViewById(R.id.btnFilterName).setOnClickListener(v -> {
            currentLabelMode = LabelMode.NAME;
            refreshLabels();
        });

        findViewById(R.id.btnFilterKomoditas).setOnClickListener(v -> {
            currentLabelMode = LabelMode.KOMODITAS;
            refreshLabels();
        });

        findViewById(R.id.btnFilterStatus).setOnClickListener(v -> {
            currentLabelMode = LabelMode.STATUS;
            refreshLabels();
        });

        companyId = getIntent().getStringExtra("company_id");
        companyName = getIntent().getStringExtra("company_name");
        mapView = findViewById(R.id.mapView);
        mapboxMap = mapView.getMapboxMap();
        // Inisialisasi SharedPreferences
        SharedPreferences prefs = getSharedPreferences("map_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

// Ambil switch dari layout
        SwitchMaterial switchShareLocation = findViewById(R.id.switchShareLocation);

// Set status terakhir dari SharedPreferences
        boolean lastStatus = prefs.getBoolean("share_location_on", false);
        switchShareLocation.setChecked(lastStatus);

// Setup listener
        switchShareLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Intent serviceIntent = new Intent(MapPetaniAllActivity.this, LocationShareService.class);

            if (isChecked) {
                startForegroundService(serviceIntent);
                Toast.makeText(MapPetaniAllActivity.this, "Berbagi lokasi dimulai", Toast.LENGTH_SHORT).show();
            } else {
                stopService(serviceIntent);
                Toast.makeText(MapPetaniAllActivity.this, "Berbagi lokasi dihentikan", Toast.LENGTH_SHORT).show();
            }

            // Simpan status ke SharedPreferences
            editor.putBoolean("share_location_on", isChecked);
            editor.apply();
        });


        floatingActionButton = findViewById(R.id.focusMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapPetaniAllActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        floatingActionButton.show();
        loadPelangganEmails();
        mapboxMap.loadStyleUri(Style.SATELLITE_STREETS, style -> {
            AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (annotationPlugin != null) {
                polygonAnnotationManager = PolygonAnnotationManagerKt.createPolygonAnnotationManager(annotationPlugin, new AnnotationConfig());
                pointAnnotationManager = com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt
                        .createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
            }

            pointAnnotationManager.addClickListener(annotation -> {
                try {
                    if (annotation.getData() != null) {
                        com.google.gson.JsonObject data = annotation.getData().getAsJsonObject();
                        int receiverId = data.get("receiver_id").getAsInt();
                        String namaReceiver = data.get("nama").getAsString();

                        Intent intent = new Intent(MapPetaniAllActivity.this, ChatActivityPetaniAll.class);
                        intent.putExtra("receiver_id", receiverId);
                        intent.putExtra("nama_petani", namaReceiver);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            });

            checkLocationPermissionAndCenterMap();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);

// Update lokasi user secara realtime
            locationComponentPlugin.addOnIndicatorPositionChangedListener(point -> {
                // Update myLocation setiap kali posisi user berubah
                myLocation = new Location("mapbox");
                myLocation.setLatitude(point.latitude());
                myLocation.setLongitude(point.longitude());


            });

// Jika ingin juga update dari FusedLocationProviderClient saat pertama kali load:
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            myLocation = location; // simpan lokasi awal
                            centerMapOnLocation(location);
                        }
                    });
            locationComponentPlugin.addOnIndicatorPositionChangedListener(point -> {
                checkUserLocationAgainstPolygons(point);
            });

            LocationPuck2D locationPuck2D = new LocationPuck2D();
            locationPuck2D.setBearingImage(ImageHolder.from(R.drawable.ic_man));

            locationComponentPlugin.setLocationPuck(locationPuck2D);
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                    locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                    getGestures(mapView).addOnMoveListener(onMoveListener);
                    floatingActionButton.show();
                }
            });
            Button btnRefreshLocation = findViewById(R.id.btnRefreshLocation);
            ImageButton imgbtnChat = findViewById(R.id.imgbtnChat);
            btnRefreshLocation.setOnClickListener(v -> {
                // Disable tombol sebentar agar tidak spam
                btnRefreshLocation.setEnabled(false);
                btnRefreshLocation.setText("Memuat...");

                // Restart MapActivity
                Intent intent = getIntent(); // ambil intent saat ini
                finish(); // hentikan activity saat ini
                startActivity(intent); // mulai ulang MapActivity

                // Jika ingin animasi lebih smooth, bisa hapus delay
            });
            imgbtnChat.setOnClickListener(v -> {
                Intent intent = new Intent(this, DaftarChatPetaniAllActivity.class);
                startActivity(intent);

                // Jika ingin animasi lebih smooth, bisa hapus delay
            });


            // Tambahkan ikon (jika perlu)

            loadPolygonsFromServer(); // Load data dari API
            EditText etSearch = findViewById(R.id.etSearch);
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterPolygons(s.toString().trim());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });

            new Handler().postDelayed(() -> {
                // simpan data ke singleton
                ServiceDataHolderPetani.getInstance().setData(geofenceAreas, getAllTargetEmails(), companyId, companyName);

                // start service
                Intent serviceIntent = new Intent(this, MapForegroundService.class);
                startForegroundService(serviceIntent);
            }, 3000);
            startOtherUsersLocationUpdates();
        });

    }



    private void filterPolygons(String keyword) {
        if (polygonAnnotationManager == null || pointAnnotationManager == null) return;

        polygonAnnotationManager.deleteAll();
        pointAnnotationManager.deleteAll();
        filteredAreas.clear();

        String lowerKeyword = keyword.toLowerCase();

        for (MapPetaniAllActivity.GeofenceArea area : geofenceAreas) {
            if (keyword.isEmpty() ||
                    area.name.toLowerCase().contains(lowerKeyword) ||
                    area.komoditas.toLowerCase().contains(lowerKeyword) ||
                    area.statusLahan.toLowerCase().contains(lowerKeyword)) {

                filteredAreas.add(area);

                // Buat ulang polygon
                List<List<Point>> polygonPoints = new ArrayList<>();
                polygonPoints.add(area.polygon);

                PolygonAnnotationOptions polygonOptions = new PolygonAnnotationOptions()
                        .withPoints(polygonPoints)
                        .withFillColor(area.color)
                        .withFillOpacity(0.2f);

                polygonAnnotationManager.create(polygonOptions);

                // Buat label ulang
                Point centroid = calculateCentroid(area.polygon);
                String labelText = getLabelText(area.name, area.komoditas, area.statusLahan);

                PointAnnotationOptions labelOptions = new PointAnnotationOptions()
                        .withPoint(centroid)
                        .withTextField(labelText)
                        .withTextSize(10.0)
                        .withTextColor("black")
                        .withTextHaloColor("white")
                        .withTextHaloWidth(1.5)
                        .withTextJustify(TextJustify.CENTER)
                        .withTextAnchor(TextAnchor.CENTER);

                pointAnnotationManager.create(labelOptions);
            }
        }

        // Jika ada hasil, pindahkan kamera ke area pertama hasil filter
        if (!filteredAreas.isEmpty()) {
            mapboxMap.setCamera(new CameraOptions.Builder()
                    .center(filteredAreas.get(0).polygon.get(0))
                    .zoom(16.0)
                    .build());
        }
    }

    private void checkLocationPermissionAndCenterMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // Minta izin lokasi
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Izin sudah diberikan, ambil lokasi
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        centerMapOnLocation(location);
                    }
                });
    }
    private void centerMapOnLocation(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(lon, lat))
                .zoom(15.0)
                .build();

        mapboxMap.setCamera(cameraOptions);
    }
    // Handle hasil permintaan izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissionAndCenterMap();
            } else {
                Toast.makeText(this, "Izin lokasi dibutuhkan untuk menampilkan posisi Anda", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void loadPelangganEmails() {
        String url = ApiConfig.BASE_URL + "get_pelanggan_emails.php?company_id=" + companyId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    JSONArray arr = response.optJSONArray("emails");
                    if (arr != null) {
                        pelangganEmails.clear();
                        for (int i = 0; i < arr.length(); i++) {
                            pelangganEmails.add(arr.optString(i));
                        }
                    }
                },
                error -> Log.e("EMAILS","Gagal ambil email pelanggan: " + error)
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void loadPolygonsFromServer() {
        String url = ApiConfig.BASE_URL + "get_peta.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            String polygonWKT = obj.getString("polygon");
                            String color = obj.optString("color", "#88FF0000");

                            // Validasi warna (jika tidak valid, pakai default semi-transparan merah)
                            if (color == null || color.isEmpty() ||
                                    !color.matches("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")) {
                                color = "#88FF0000";
                            }

                            List<Point> points = parseWKTPolygon(polygonWKT);
                            if (points.size() < 3) continue;

                            List<List<Point>> polygonPoints = new ArrayList<>();
                            polygonPoints.add(points);

                            PolygonAnnotationOptions options = new PolygonAnnotationOptions()
                                    .withPoints(polygonPoints)
                                    .withFillColor(color)
                                    .withFillOpacity(0.2f);

                            polygonAnnotationManager.create(options);

                            // Label area
                            String areaName = obj.optString("name", "Area");
                            String komoditas = obj.optString("komoditas", "Tidak diketahui");
                            String statusLahan = obj.optString("statusLahan", "Tidak diketahui");

                            Point centroid = calculateCentroid(points);
                            String labelText = getLabelText(areaName, komoditas, statusLahan);

                            PointAnnotationOptions labelOptions = new PointAnnotationOptions()
                                    .withPoint(centroid)
                                    .withTextField(labelText)
                                    .withTextSize(10.0)
                                    .withTextColor("black")
                                    .withTextHaloColor("white")
                                    .withTextHaloWidth(1.5)
                                    .withTextJustify(TextJustify.CENTER)
                                    .withTextAnchor(TextAnchor.CENTER);

                            pointAnnotationManager.create(labelOptions);

                            // Simpan ke daftar geofence
                            geofenceAreas.add(new GeofenceArea(areaName, komoditas, statusLahan, points, color));


                            // Fokus kamera ke polygon pertama
                            if (i == 0) {
                                mapboxMap.setCamera(new CameraOptions.Builder()
                                        .center(points.get(0))
                                        .zoom(16.0)
                                        .build());
                            }
                        }
                    } catch (Exception e) {
                        Log.e("GeoParse", "Error parsing polygons: " + e.getMessage());
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley", "Error loading polygons: " + error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }


    private List<Point> parseWKTPolygon(String wkt) {
        List<Point> pointList = new ArrayList<>();

        try {
            // POLYGON((lon lat, lon lat, ...))
            String inner = wkt.substring(wkt.indexOf("((") + 2, wkt.lastIndexOf("))"));
            String[] coords = inner.split(",");

            for (String coord : coords) {
                String[] parts = coord.trim().split(" ");
                double lon = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                pointList.add(Point.fromLngLat(lon, lat));
            }
        } catch (Exception e) {
            Log.e("WKT_PARSE", "Invalid WKT: " + wkt);
        }

        return pointList;
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
        return (intersectCount % 2 == 1); // ganjil = dalam polygon
    }

    private void checkUserLocationAgainstPolygons(Point userLocation) {
        String email = getUserEmail();

        boolean insideAnyPolygon = false;
        String enteredAreaName = null;

        for (GeofenceArea area : geofenceAreas) {
            if (pointInPolygon(userLocation, area.polygon)) {
                insideAnyPolygon = true;
                enteredAreaName = area.name;
                break;
            }
        }

        if (insideAnyPolygon) {
            // Batalkan exit jika sedang menunggu
            if (isWaitingExit && exitRunnable != null) {
                geofenceHandler.removeCallbacks(exitRunnable);
                isWaitingExit = false;
            }

            if (!enteredAreaName.equals(currentGeofenceName)) {
                // Jika masuk ke area baru
                if (!isWaitingEnter) {
                    isWaitingEnter = true;
                    final String finalEnteredAreaName = enteredAreaName;
                    enterRunnable = () -> {
                        currentGeofenceName = finalEnteredAreaName;
                        String msg = email + " dari perusahaan " + companyName + " memasuki lahan: " + currentGeofenceName;
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        sendNotificationToServer(getAllTargetEmails(), "Masuk Area", msg);
                        isWaitingEnter = false;
                    };
                    geofenceHandler.postDelayed(enterRunnable, 120000); // 15 detik delay
                }

            }
        } else {
            // Batalkan enter jika belum sempat masuk
            if (isWaitingEnter && enterRunnable != null) {
                geofenceHandler.removeCallbacks(enterRunnable);
                isWaitingEnter = false;
            }

            if (currentGeofenceName != null && !isWaitingExit) {
                isWaitingExit = true;
                String exitedAreaName = currentGeofenceName;

                exitRunnable = () -> {
                    String msg = email + " dari perusahaan " + companyName + " keluar dari lahan: " + exitedAreaName;
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    sendNotificationToServer(getAllTargetEmails(), "Keluar Area", msg);
                    currentGeofenceName = null;
                    isWaitingExit = false;
                };

                geofenceHandler.postDelayed(exitRunnable, 120000); // 5 detik delay
            }
        }
    }


    private void fetchAndDisplayOtherUsers() {
        if (!showUserMarkers || pointAnnotationManager == null) return; // skip jika switch off

        String url = ApiConfig.BASE_URL + "get_location.php";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray locations = response.getJSONArray("locations");

                            // Hapus marker lama yang sudah tidak ada di server
                            List<String> emailsFromServer = new ArrayList<>();
                            for (int i = 0; i < locations.length(); i++) {
                                JSONObject obj = locations.getJSONObject(i);
                                String email = obj.getString("email");

                                // skip marker user sendiri
                                if (email.equals(getUserEmail())) continue;

                                emailsFromServer.add(email);
                            }

                            userMarkers.entrySet().removeIf(entry -> {
                                if (!emailsFromServer.contains(entry.getKey())) {
                                    pointAnnotationManager.delete(entry.getValue());
                                    return true;
                                }
                                return false;
                            });

                            // Tambah/update marker orang lain
                            for (int i = 0; i < locations.length(); i++) {
                                JSONObject obj = locations.getJSONObject(i);
                                int user_id = obj.getInt("user_id");
                                String email = obj.getString("email");

                                if (email.equals(getUserEmail())) continue;

                                double lat = obj.getDouble("latitude");
                                double lon = obj.getDouble("longitude");
                                String nama = obj.getString("nama");
                                String fotoProfil = obj.getString("foto_profil");
                                String namaPerusahaan = obj.optString("nama_perusahaan", ""); // ambil nama perusahaan

                                updateUserMarker(user_id, email, lat, lon, nama, fotoProfil, namaPerusahaan);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("LOC_FETCH", "Error: " + error)
        );

        Volley.newRequestQueue(this).add(request);
    }


    // Contoh metode ambil email user yang login
    private String getCurrentUserEmail() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return prefs.getString("email", "");
    }



    private void updateUserMarker(int userId, String email, double lat, double lon, String nama, String fotoProfilUrl, String namaPerusahaan) {
        if (MapPetaniAllActivity.this.isFinishing() || MapPetaniAllActivity.this.isDestroyed()) return;

        Point point = Point.fromLngLat(lon, lat);

        float[] results = new float[1];
        if (myLocation != null) {
            Location.distanceBetween(
                    myLocation.getLatitude(), myLocation.getLongitude(),
                    lat, lon,
                    results
            );
        }
        float distanceInMeters = results[0];

        if (userMarkers.containsKey(email)) {
            userMarkers.get(email).setPoint(point);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load("https://sistemcerdasindonesia.com/contractfarming/uploads/profile/" + fotoProfilUrl)
                    .circleCrop()
                    .placeholder(R.drawable.baseline_account_circle_24)
                    .error(R.drawable.baseline_account_circle_24)
                    .into(new com.bumptech.glide.request.target.CustomTarget<Bitmap>(100, 100) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            Bitmap markerBitmap = createMarkerBitmap(resource, nama, distanceInMeters, namaPerusahaan);

                            PointAnnotationOptions options = new PointAnnotationOptions()
                                    .withPoint(point)
                                    .withIconImage(markerBitmap)
                                    .withData(new com.google.gson.JsonObject()); // nanti diset receiver_id

                            PointAnnotation marker = pointAnnotationManager.create(options);

                            // simpan receiver_id di data marker
                            try {
                                com.google.gson.JsonObject data = new com.google.gson.JsonObject();
                                data.addProperty("receiver_id", userId);
                                data.addProperty("nama", nama);
                                marker.setData(data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            userMarkers.put(email, marker);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) { }
                    });
        }
    }

    private Bitmap createMarkerBitmap(Bitmap profilePic, String name, float distance, String namaPerusahaan) {
        int width = 220;
        int height = 260;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // background putih
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(0, 0, width, height, 25, 25, paint);

        // foto profil
        Rect rect = new Rect(60, 10, 160, 110);
        canvas.drawBitmap(profilePic, null, rect, paint);

        // nama pengguna
        paint.setColor(Color.BLACK);
        paint.setTextSize(24f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(name, width / 2f, 145, paint);

        // nama perusahaan (tosca)
        paint.setTextSize(20f);
        paint.setColor(Color.parseColor("#009688")); // tosca
        canvas.drawText(namaPerusahaan, width / 2f, 175, paint);

        // jarak
        paint.setTextSize(18f);
        paint.setColor(Color.DKGRAY);
        String jarakText = distance >= 1000 ?
                String.format("%.1f km", distance / 1000) :
                String.format("%.0f m", distance);
        canvas.drawText(jarakText, width / 2f, 205, paint);

        return bitmap;
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

    private void refreshLabels() {
        if (pointAnnotationManager != null) {
            pointAnnotationManager.deleteAll(); // hapus semua label dulu

            for (GeofenceArea area : geofenceAreas) {
                Point centroid = calculateCentroid(area.polygon);
                String labelText = getLabelText(area.name, area.komoditas, area.statusLahan);

                PointAnnotationOptions labelOptions = new PointAnnotationOptions()
                        .withPoint(centroid)
                        .withTextField(labelText)
                        .withTextSize(10.0)
                        .withTextColor("black")
                        .withTextHaloColor("white")
                        .withTextHaloWidth(1.5)
                        .withTextJustify(TextJustify.CENTER)
                        .withTextAnchor(TextAnchor.CENTER);

                pointAnnotationManager.create(labelOptions);
            }
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (polygonAnnotationManager != null) {
            polygonAnnotationManager.onDestroy();
        }
    }
}

