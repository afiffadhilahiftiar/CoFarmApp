package com.example.contractfarmingapp;
import static com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt.createPointAnnotationManager;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import com.example.contractfarmingapp.utils.BitmapUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

public class MapPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private EditText etCoordinates;
    private MapboxMap mapboxMap;
    private String selectedCoordinates = "";
    private Button btnSimpanLokasi;
    FloatingActionButton floatingActionButton;
    private FusedLocationProviderClient fusedLocationClient;
private final ActivityResultLauncher<String> activityResultLauncher =registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
    @Override
    public void onActivityResult(Boolean result) {
        if (result) {
            Toast.makeText(MapPickerActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MapPickerActivity.this, "Permission not Granted", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_map_picker);

        etCoordinates = findViewById(R.id.etCoordinates);
        mapView = findViewById(R.id.mapView);
        btnSimpanLokasi = findViewById(R.id.btnSimpanLokasi);
        mapboxMap = mapView.getMapboxMap();

        floatingActionButton = findViewById(R.id.focusLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MapPickerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        floatingActionButton.show();
        mapboxMap.loadStyleUri(Style.SATELLITE_STREETS, style -> {
            Bitmap iconBitmap = BitmapUtils.getBitmapFromDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_pinlokasi)
            );

            if (iconBitmap != null) {
                style.addImage("pin-icon", iconBitmap);
            }

            // Cek dan ambil lokasi saat style selesai di-load
            checkLocationPermissionAndCenterMap();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
            locationComponentPlugin.setEnabled(true);
            LocationPuck2D locationPuck2D = new LocationPuck2D();
            locationPuck2D.setBearingImage(ImageHolder.from(R.drawable.ic_man));

            locationComponentPlugin.setLocationPuck(locationPuck2D);
            locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);

            // Tambahkan listener klik peta
            AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);

// Buat PointAnnotationManager dengan konfigurasi default
            PointAnnotationManager pointAnnotationManager = createPointAnnotationManager(
                    annotationPlugin,
                    new AnnotationConfig()
            );
            GesturesPlugin gesturesPlugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
            if (gesturesPlugin != null) {
                gesturesPlugin.addOnMapClickListener(point -> {
                    selectedCoordinates = point.latitude() + ", " + point.longitude();
                    etCoordinates.setText(selectedCoordinates);
                    Toast.makeText(this, "Koordinat: " + selectedCoordinates, Toast.LENGTH_SHORT).show();
                    pointAnnotationManager.deleteAll();
                    ImageHolder imageHolder = ImageHolder.from(R.drawable.ic_pinlokasi);

                    PointAnnotationOptions options = new PointAnnotationOptions()
                            .withPoint(point)
                            .withIconImage("pin-icon") // Pakai ID string, bukan ImageHolder
                            .withIconSize(1.0);

                    pointAnnotationManager.create(options);
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
        });

        btnSimpanLokasi.setOnClickListener(v -> {
            if (!selectedCoordinates.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("latitude", selectedCoordinates.split(",")[0].trim());
                resultIntent.putExtra("longitude", selectedCoordinates.split(",")[1].trim());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Silakan klik lokasi di peta terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
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
    }
}