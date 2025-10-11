package com.example.contractfarmingapp;

import static com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManagerKt.createPolygonAnnotationManager;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt.createPointAnnotationManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.api.GeoFenceApi;
import com.example.contractfarmingapp.models.GeoFenceModel;
import com.example.contractfarmingapp.utils.BitmapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import java.util.ArrayList;
import java.util.List;


public class MapGeofencingActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap mapboxMap;

    FloatingActionButton floatingActionButton;
    private FusedLocationProviderClient fusedLocationClient;
    private PolygonAnnotationManager polygonAnnotationManager;
    private PointAnnotationManager pointAnnotationManager;

    private List<Point> pointList = new ArrayList<>();
    private PolygonAnnotation currentPolygon;
    private Spinner spinnerPetani;
    private List<String> petaniNames = new ArrayList<>();
    private List<String> petaniIds = new ArrayList<>();
    private String selectedUserId = null;

    private Button btnReset, btnDeletePoint, btnSimpan;
    private EditText etNamaArea;
    private String companyId;
    private Spinner spinnerColor, spinnerStatus;
    private String selectedColorHex = "#880000FF";


    private EditText etAreaSize, etKomoditas;
    private GeoFenceApi geoFenceApi; // deklarasi global
    private String convertToWKTFromPoints(List<Point> points) {
        if (points == null || points.size() < 3) return null;

        // Pastikan loop tertutup
        if (!points.get(0).equals(points.get(points.size() - 1))) {
            points.add(points.get(0));
        }

        StringBuilder wkt = new StringBuilder("POLYGON((");
        for (Point point : points) {
            wkt.append(point.longitude())
                    .append(" ")
                    .append(point.latitude())
                    .append(", ");
        }
        // Hapus koma terakhir
        int lastComma = wkt.lastIndexOf(", ");
        wkt.replace(lastComma, wkt.length(), "))");

        return wkt.toString();
    }

    private final ActivityResultLauncher<String> activityResultLauncher =registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                Toast.makeText(MapGeofencingActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapGeofencingActivity.this, "Permission not Granted", Toast.LENGTH_SHORT).show();
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

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_geofencing);
        companyId = getIntent().getStringExtra("company_id");

        mapView = findViewById(R.id.mapView);
        btnReset = findViewById(R.id.btnReset);
        btnDeletePoint = findViewById(R.id.btnDeletePoint);
        btnSimpan = findViewById(R.id.btnSimpan);
        etAreaSize = findViewById(R.id.etAreaSize);
        etNamaArea = findViewById(R.id.etNamaArea);
        etKomoditas = findViewById(R.id.etKomoditas);
        spinnerColor = findViewById(R.id.spinnerColor);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerPetani = findViewById(R.id.spinnerPetani);

// Load data petani
        loadPetaniFromServer(companyId);

        spinnerColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Merah
                        selectedColorHex = "#FF0000"; break;
                    case 1: // Kuning
                        selectedColorHex = "#FFFF00"; break;
                    case 2: // Biru
                        selectedColorHex = "#0000FF"; break;
                    case 3: // Oren
                        selectedColorHex = "#FFA500"; break;
                }
                if (pointList.size() > 0) {
                    updatePolygon(); // Update warna jika sudah ada polygon
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        floatingActionButton = findViewById(R.id.focusMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapGeofencingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        floatingActionButton.show();
        mapboxMap = mapView.getMapboxMap();

        mapboxMap.loadStyleUri(Style.SATELLITE_STREETS, style -> {
            Bitmap iconBitmap = BitmapUtils.getBitmapFromDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_pinlokasi)
            );
            if (iconBitmap != null) {
                style.addImage("pin-icon", iconBitmap);
            }

            AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);

            if (annotationPlugin != null) {
                polygonAnnotationManager = createPolygonAnnotationManager(
                        annotationPlugin,
                        new AnnotationConfig()
                );
                pointAnnotationManager = createPointAnnotationManager(
                        annotationPlugin,
                        new AnnotationConfig()
                );
            }
            ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                    this, R.array.polygon_colors, R.layout.spinner_maps);
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerColor.setAdapter(colorAdapter);

            ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                    this, R.array.status_lahan, R.layout.spinner_maps);
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(statusAdapter);

            checkLocationPermissionAndCenterMap();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            LocationPuck2D locationPuck2D = new LocationPuck2D();
            locationPuck2D.setBearingImage(ImageHolder.from(R.drawable.ic_man));

            locationComponentPlugin.setLocationPuck(locationPuck2D);
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);

            GesturesPlugin gestures = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
            if (gestures != null) {
                gestures.addOnMapClickListener(point -> {
                    pointList.add(point);
                    updatePolygon();
                    return true;
                });
            }
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
                    locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
                    getGestures(mapView).addOnMoveListener(onMoveListener);
                    floatingActionButton.show();
                }
            });

            loadPolygonsFromServer(); // Load data dari API
        });

        btnReset.setOnClickListener(v -> {
            pointList.clear();
            etAreaSize.setText("");
            if (polygonAnnotationManager != null) {
                polygonAnnotationManager.deleteAll();
            }
            if (pointAnnotationManager != null) {
                pointAnnotationManager.deleteAll();
            }
        });
        btnDeletePoint.setOnClickListener(v -> {
            if (!pointList.isEmpty()) {
                pointList.remove(pointList.size() - 1); // hapus titik terakhir
                updatePolygon();
            } else {
                Toast.makeText(this, "Tidak ada titik untuk dihapus", Toast.LENGTH_SHORT).show();
            }
        });
        ProgressDialog progressDialog = new ProgressDialog(MapGeofencingActivity.this);
        progressDialog.setMessage("Menyimpan data...");
        progressDialog.setCancelable(false);

        btnSimpan.setOnClickListener(v -> {
            if (pointList.size() < 3) {
                Toast.makeText(this, "Tambahkan minimal 3 titik", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedUserId == null) {
                Toast.makeText(this, "Pilih petani terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }




            double luas = calculatePolygonArea(pointList);
            String wktPolygon = convertToWKTFromPoints(new ArrayList<>(pointList));
            String namaArea = etNamaArea.getText().toString().trim();
            String komoditas = etKomoditas.getText().toString().trim();
            String selectedStatus = spinnerStatus.getSelectedItem().toString();

            if (namaArea.isEmpty()) {
                Toast.makeText(this, "Nama area tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            if (komoditas.isEmpty()) {
                Toast.makeText(this, "Komoditas tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedStatus.equalsIgnoreCase("Pilih Status Lahan")) {
                Toast.makeText(this, "Status tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }
            // Hitung centroid dari titik-titik
            Point centroid = pointList.get(0); // atau hitung centroid yang akurat
            progressDialog.show();
            getWilayahFromCoordinates(centroid, (desaName, kabupatenName, provinsiName) -> {
                new Thread(() -> {
                    try {
                        URL url = new URL(ApiConfig.BASE_URL + "save_geofence.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setDoOutput(true);

                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("name", namaArea);
                        jsonBody.put("komoditas", komoditas);
                        jsonBody.put("area_size", luas);
                        String lahanGabungan = namaArea + " - " + String.format("%.3f ha", luas / 10000); // dari m² ke ha
                        jsonBody.put("lahan", lahanGabungan);
                        jsonBody.put("user_id", selectedUserId);
                        jsonBody.put("status", selectedStatus);
                        jsonBody.put("polygon_wkt", wktPolygon);
                        jsonBody.put("company_id", companyId);
                        jsonBody.put("color", selectedColorHex);
                        jsonBody.put("desa", desaName);
                        jsonBody.put("kabupaten", kabupatenName);
                        jsonBody.put("provinsi", provinsiName);
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            jsonBody.put("email", user.getEmail());
                        }

                        OutputStream os = conn.getOutputStream();
                        os.write(jsonBody.toString().getBytes("UTF-8"));
                        os.close();

                        int responseCode = conn.getResponseCode();
                        InputStream is = (responseCode == 200 || responseCode == 201) ?
                                conn.getInputStream() : conn.getErrorStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            if (responseCode == 200 || responseCode == 201) {
                                Toast.makeText(MapGeofencingActivity.this, "Berhasil disimpan", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("nama_area", namaArea);
                                resultIntent.putExtra("luas_area", luas);
                                resultIntent.putExtra("wkt_polygon", wktPolygon);
                                resultIntent.putExtra("komoditas", komoditas);
                                resultIntent.putExtra("status", selectedStatus);
                                setResult(RESULT_OK, resultIntent);
                                finish(); // menutup activity dan kembali ke activity sebelumnya

                            } else {
                                Toast.makeText(MapGeofencingActivity.this, "Gagal simpan: " + response.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MapGeofencingActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            });

        });

        spinnerColor.setSelection(2); // 2 = biru default

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
    private void loadPetaniFromServer(String companyId) {
        String url = ApiConfig.BASE_URL + "get_userprofiles.php";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("company_id", companyId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");

                        petaniNames.clear();
                        petaniIds.clear();

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            String id = obj.getString("id");
                            String name = obj.getString("name");
                            String email = obj.getString("email");

                            // Simpan user_id
                            petaniIds.add(id);

                            // Tampilkan di spinner: "id - nama - email"
                            petaniNames.add(id + " - " + name + " - " + email);
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                R.layout.spinner_item_1,
                                petaniNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPetani.setAdapter(adapter);

                        spinnerPetani.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedUserId = petaniIds.get(position); // simpan user_id
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                selectedUserId = null;
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Volley", "Error load petani: " + error.toString())
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(request);
    }


    private void updatePolygon() {
        if (pointList.size() >= 1 && polygonAnnotationManager != null) {
            polygonAnnotationManager.deleteAll();

            List<List<Point>> polygonPoints = new ArrayList<>();
            List<Point> closed = new ArrayList<>(pointList);

            // Menutup polygon jika belum tertutup
            if (!closed.get(0).equals(closed.get(closed.size() - 1))) {
                closed.add(closed.get(0));
            }
            polygonPoints.add(closed);

            // Debug log
            System.out.println("Update Polygon dengan warna: " + selectedColorHex);

            // Fallback warna jika null
            String fillColor = (selectedColorHex != null && !selectedColorHex.isEmpty())
                    ? selectedColorHex : "#88FF0000";

            PolygonAnnotationOptions options = new PolygonAnnotationOptions()
                    .withPoints(polygonPoints)
                    .withFillColor(fillColor)
                    .withFillOpacity(0.2f);

            currentPolygon = polygonAnnotationManager.create(options);

            if (pointAnnotationManager != null) {
                pointAnnotationManager.deleteAll();

                for (Point point : pointList) {
                    PointAnnotationOptions markerOptions = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("pin-icon")
                            .withIconSize(0.8);
                    pointAnnotationManager.create(markerOptions);
                }
            }

            double luas = calculatePolygonArea(closed);
            etAreaSize.setText(String.format("%.2f m²", luas));
        }
    }

    // Fungsi utilitas untuk hitung luas polygon (meter persegi)
    private double calculatePolygonArea(List<Point> points) {
        final double R = 6378137; // jari-jari bumi dalam meter
        double area = 0.0;

        int size = points.size();
        if (size < 3) return 0;

        for (int i = 0; i < size; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % size);

            double lon1 = Math.toRadians(p1.longitude());
            double lat1 = Math.toRadians(p1.latitude());
            double lon2 = Math.toRadians(p2.longitude());
            double lat2 = Math.toRadians(p2.latitude());

            area += (lon2 - lon1) * (2 + Math.sin(lat1) + Math.sin(lat2));
        }

        area = area * R * R / 2.0;
        return Math.abs(area); // meter persegi
    }
    private String convertPolygonToGeoJson(List<Point> points) {
        List<List<Point>> polygon = new ArrayList<>();
        List<Point> closed = new ArrayList<>(points);
        closed.add(points.get(0)); // Close the loop
        polygon.add(closed);

        Polygon geoJsonPolygon = Polygon.fromLngLats(polygon);
        return geoJsonPolygon.toJson(); // → bentuk GeoJSON siap dikirim
    }
    private void getWilayahFromCoordinates(Point centroid, GeocodingCallback callback) {
        new Thread(() -> {
            try {
                String accessToken = "sk.eyJ1IjoiYXBpcHZheHMiLCJhIjoiY21jNHNjd3RvMGE5MTJpc2EzOWxtZXo4bSJ9.rqnsgFwXAnzPrII2XkBCSw";
                String urlStr = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                        + centroid.longitude() + "," + centroid.latitude()
                        + ".json?access_token=" + accessToken + "&types=place,locality,district,region";

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder json = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                JSONObject response = new JSONObject(json.toString());
                JSONArray features = response.getJSONArray("features");

                String desa = "Tidak Diketahui";
                String kabupaten = "Tidak Diketahui";
                String provinsi = "Tidak Diketahui";

                if (features.length() > 0) {
                    JSONObject feature = features.getJSONObject(0);

                    // Ambil text utama
                    desa = feature.optString("text", desa);

                    // Cari di context
                    if (feature.has("context")) {
                        JSONArray context = feature.getJSONArray("context");
                        for (int i = 0; i < context.length(); i++) {
                            JSONObject ctx = context.getJSONObject(i);
                            String id = ctx.getString("id");
                            String text = ctx.getString("text");

                            if (id.startsWith("locality")) {
                                kabupaten = text;
                            } else if (id.startsWith("region")) {
                                provinsi = text;
                            }
                        }
                    }
                }

                String finalDesa = desa;
                String finalKabupaten = kabupaten;
                String finalProvinsi = provinsi;

                runOnUiThread(() -> callback.onResult(finalDesa, finalKabupaten, finalProvinsi));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> callback.onResult("Tidak Diketahui", "Tidak Diketahui", "Tidak Diketahui"));
            }
        }).start();
    }


    interface GeocodingCallback {
        void onResult(String desaName, String kabupatenName, String provinsiName);
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
