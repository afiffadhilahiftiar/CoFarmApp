package com.example.contractfarmingapp;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.List;

public class MapPetaniActivity extends AppCompatActivity {
    private static class GeofenceArea {
        String name;
        List<Point> polygon;

        GeofenceArea(String name, List<Point> polygon) {
            this.name = name;
            this.polygon = polygon;
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


    private MapView mapView;
    private MapboxMap mapboxMap;
    private String companyId;
    private String companyName;
    private android.os.Handler geofenceHandler = new android.os.Handler();
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
                Toast.makeText(MapPetaniActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapPetaniActivity.this, "Permission not Granted", Toast.LENGTH_SHORT).show();
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        companyId = getIntent().getStringExtra("company_id");
        companyName = getIntent().getStringExtra("company_name");
        mapView = findViewById(R.id.mapView);
        mapboxMap = mapView.getMapboxMap();
        floatingActionButton = findViewById(R.id.focusMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapPetaniActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


            checkLocationPermissionAndCenterMap();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
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

            // Tambahkan ikon (jika perlu)

            loadPolygonsFromServer(); // Load data dari API
        });

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
        String url = ApiConfig.BASE_URL + "get_geofences.php";

        // Siapkan JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("company_id", companyId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        // Response langsung berupa array, jadi ubah jadi JSONArray
                        JSONArray dataArray = response.getJSONArray("data");

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            String polygonWKT = obj.getString("polygon");
                            String color = obj.optString("color", "#88FF0000");

                            // Validasi warna
                            if (color == null || color.isEmpty() || !color.matches("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")) {
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
                            Point centroid = calculateCentroid(points);
                            String areaName = obj.optString("name", "Area");

                            PointAnnotationOptions labelOptions = new PointAnnotationOptions()
                                    .withPoint(centroid)
                                    .withTextField(areaName)
                                    .withTextSize(10.0)
                                    .withTextColor("black")
                                    .withTextHaloColor("white")
                                    .withTextHaloWidth(1.5)
                                    .withTextJustify(TextJustify.CENTER)
                                    .withTextAnchor(TextAnchor.CENTER);

                            pointAnnotationManager.create(labelOptions);
                            geofenceAreas.add(new GeofenceArea(areaName, points));


                            // Kamera ke polygon pertama
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
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

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
                    geofenceHandler.postDelayed(enterRunnable, 5000); // 5 detik delay
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

                geofenceHandler.postDelayed(exitRunnable, 5000); // 5 detik delay
            }
        }
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

