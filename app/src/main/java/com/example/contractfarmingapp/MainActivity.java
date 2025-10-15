package com.example.contractfarmingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.*;

import java.util.concurrent.TimeUnit;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.view.MenuItem;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.contractfarmingapp.activities.CartActivity;
import com.example.contractfarmingapp.activities.LoginActivity;
import com.example.contractfarmingapp.activities.NotificationActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Tambahkan import ini


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etCari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Di MainActivity, dalam onCreate()
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            // Session Google/Firebase habis, arahkan ke Login
            Toast.makeText(this, "Session habis. Silakan login kembali.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        String email = mAuth.getCurrentUser().getEmail(); // Gunakan email dari Firebase
        // Mulai polling notifikasi setelah izin diberikan (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                startNotificationPolling(email);
            }
        } else {
            startNotificationPolling(email);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.POST_NOTIFICATIONS)) {
                    Toast.makeText(this, "Aplikasi membutuhkan izin notifikasi.", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        WorkManager wm = WorkManager.getInstance(this);
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                NotificationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(new Data.Builder().putString("email", email).build())
                .build();
        wm.enqueueUniquePeriodicWork("notif_worker", ExistingPeriodicWorkPolicy.KEEP, request);

        if (email == null) {
            // Jika email tidak ditemukan, redirect ke Login
            Toast.makeText(this, "Session habis. Silakan login kembali dengan email dan password.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        ImageView iconNotification = findViewById(R.id.iconNotification);

        iconNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
        ImageView iconCart = findViewById(R.id.iconCart);

        iconCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, KontrakSimpanActivity.class);
                startActivity(intent);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar); //Ignore red line errors
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        etCari = findViewById(R.id.editTextCari);
        etCari.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etCari.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, KontrakActivity.class);
                intent.putExtra("keyword", keyword); // kirim keyword ke KontrakActivity
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "Masukkan kata kunci pencarian", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        ImageView iconDehaze = findViewById(R.id.iconDehaze);
        iconDehaze.setOnClickListener(v -> {
            if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

// Ambil header dari NavigationView
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.navHeaderName);
        ImageView navProfileImage = headerView.findViewById(R.id.navProfileImage);
        TextView badgeCart = findViewById(R.id.badgeCart);
        TextView badgeNotification = findViewById(R.id.badgeNotification);



// Tampilkan badge jika count > 0
        int cartCount = 0;


        if (cartCount > 0) {
            badgeCart.setVisibility(View.VISIBLE);
            updateSavedContractsBadge(badgeCart);

        } else {
            badgeCart.setVisibility(View.GONE);
        }

        getUnreadNotificationCount(email, badgeNotification);



        getUserData(email, navHeaderName, navProfileImage);





        if (savedInstanceState == null) {
            String fragmentToOpen = getIntent().getStringExtra("fragment_to_open");

            if (fragmentToOpen != null && fragmentToOpen.equals("perusahaan")) {
                // Buka PerusahaanFragment jika diminta
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PerusahaanFragment())
                        .commit();
                navigationView.setCheckedItem(R.id.nav_share); // Pastikan ID ini sama dengan item Perusahaan
            } else {
                // Default buka HomeFragment
                PerusahaanFragment perusahaanFragment = new PerusahaanFragment();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                perusahaanFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, perusahaanFragment).commit();

                navigationView.setCheckedItem(R.id.nav_about);
            }
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

// Inisialisasi refresh behavior
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh user data dan fragment




            // Reload current fragment (misalnya HomeFragment)
            KontrakFragment kontrakFragment = new KontrakFragment();
            Bundle bundle = new Bundle();
            bundle.putString("email", email);
            kontrakFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, kontrakFragment).commit();
            getUnreadNotificationCount(email, badgeNotification);
            startNotificationPolling(email);
            updateSavedContractsBadge(badgeCart);
            getUserData(email, navHeaderName, navProfileImage);
            // Hentikan indikator refresh setelah selesai
            swipeRefreshLayout.setRefreshing(false);
        });

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show();
                String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if (email != null) startNotificationPolling(email);
            } else {
                Toast.makeText(this, "Izin notifikasi ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateSavedContractsBadge(TextView badgeCart) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (userId == -1) return;

        new AsyncTask<Integer, Void, Integer>() {
            @Override
            protected Integer doInBackground(Integer... params) {
                int count = 0;
                try {
                    URL url = new URL(ApiConfig.BASE_URL + "get_saved_contracts_count.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String data = "user_id=" + params[0];
                    conn.getOutputStream().write(data.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(result.toString());
                    count = json.getInt("count");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                if (count > 0) {
                    badgeCart.setVisibility(View.VISIBLE);
                    badgeCart.setText(String.valueOf(count));
                } else {
                    badgeCart.setVisibility(View.GONE);
                }
            }
        }.execute(userId);
    }

    private void startNotificationPolling(String email) {
        Data inputData = new Data.Builder()
                .putString("email", email)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) // opsional
                .build();

        PeriodicWorkRequest notificationWork =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.MINUTES) // WorkManager minimum 15 menit
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "notification_work",
                ExistingPeriodicWorkPolicy.REPLACE,
                notificationWork
        );
    }
    private void getUserData(String email, TextView navHeaderName, ImageView navProfileImage) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String result = "";
                try {
                    URL url = new URL(ApiConfig.BASE_URL + "get_user_data.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String data = "email=" + URLEncoder.encode(params[0], "UTF-8");
                    conn.getOutputStream().write(data.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    reader.close();
                    result = stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }


            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    if (jsonResponse.has("error")) {
                        Toast.makeText(MainActivity.this, "Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show();
                    } else {
                        int userId = jsonResponse.getInt("id");
                        String nama = jsonResponse.getString("nama");
                        String alamat = jsonResponse.getString("alamat");
                        String foto_profil = jsonResponse.getString("foto_profil");
                        int companyId = jsonResponse.getInt("company_id");
                        int fasilitatorId = jsonResponse.getInt("fasilitator_id");
                        String companyEmail = jsonResponse.getString("company_email"); // Tambahkan ini
                        String namaPerusahaan = jsonResponse.getString("nama_perusahaan"); // Tambahkan ini
                        String idPoktan = jsonResponse.getString("id_poktan"); // Tambahkan ini
                        String lokasiPerusahaan = jsonResponse.getString("lokasi_perusahaan"); // Tambahkan ini
                        String logoPerusahaan = jsonResponse.getString("logo_perusahaan"); // Tambahkan ini


                        navHeaderName.setText(nama);
// Simpan nama ke SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("id", userId);
                        editor.putString("nama_pengguna", nama);
                        editor.putString("alamat", alamat);
                        editor.putInt("company_id", companyId);
                        editor.putInt("fasilitator_id", fasilitatorId);
                        editor.putString("company_email", companyEmail);
                        editor.putString("nama_poktan", namaPerusahaan);
                        editor.putString("id_poktan", idPoktan);
                        editor.putString("lokasi_poktan", lokasiPerusahaan);
                        editor.putString("logo_poktan", logoPerusahaan);
                        editor.apply(); // Gunakan commit() jika ingin sinkron

                        if (foto_profil != null && !foto_profil.isEmpty() && foto_profil.startsWith("http")) {
                            Glide.with(MainActivity.this)
                                    .load(foto_profil)
                                    .placeholder(R.drawable.baseline_account_circle_24)
                                    .error(R.drawable.baseline_account_circle_24)
                                    .into(navProfileImage);
                        } else {
                            navProfileImage.setImageResource(R.drawable.baseline_account_circle_24);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Gagal memuat data profil", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(email);
    }

    private void getUnreadNotificationCount(String email, TextView badgeNotification) {
        new AsyncTask<String, Void, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                int count = 0;
                try {
                    URL url = new URL(ApiConfig.BASE_URL + "get_unread_notifications.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String data = "email=" + URLEncoder.encode(params[0], "UTF-8");
                    conn.getOutputStream().write(data.getBytes());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(result.toString());
                    count = jsonResponse.getInt("unread_count");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                if (count > 0) {
                    badgeNotification.setVisibility(View.VISIBLE);
                    badgeNotification.setText(String.valueOf(count));
                } else {
                    badgeNotification.setVisibility(View.GONE);
                }
            }
        }.execute(email);
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
        } else if (id == R.id.nav_kontrak) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new KontrakFragment()).commit();
        } else if (id == R.id.nav_share) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PerusahaanFragment()).commit();
        } else if (id == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (id == R.id.nav_logout) {
            // Tambahkan konfirmasi logout
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Logout")
                    .setMessage("Apakah Anda yakin ingin keluar?")
                    .setPositiveButton("Ya", (dialog, which) -> {

                        // Hapus session lokal saja
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear(); // Hapus semua data session
                        editor.apply();
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor userEditor = prefs.edit();
                        userEditor.clear();
                        userEditor.apply();

                        // Pindah ke LoginActivity
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                        // Jangan panggil FirebaseAuth.getInstance().signOut();

                    })
                    .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                    .show();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}