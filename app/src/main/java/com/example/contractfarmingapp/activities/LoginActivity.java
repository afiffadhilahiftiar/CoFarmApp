package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.MainActivity;
import com.example.contractfarmingapp.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button btnLogin, btnToRegister;
    ImageView togglePassword;
    ImageButton btnGoogle;
    private int userId;
    private int companyId;
    private String email;
    private String ktp;
    boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi UI
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);
        togglePassword = findViewById(R.id.togglePassword);
        btnGoogle = findViewById(R.id.btnGoogle);
        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In Config
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Toggle password visibility
        togglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            editPassword.setInputType(isPasswordVisible ?
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(isPasswordVisible ? R.drawable.ic_eye_on : R.drawable.ic_eye_off);
            editPassword.setSelection(editPassword.getText().length());
        });

        // Tombol Login Manual (Firebase email/password)
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Ambil user aktif dari Firebase
                    if (mAuth.getCurrentUser() != null) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            // Email sudah diverifikasi -> lanjut ke server
                            cekUserDiServer(email);
                        } else {
                            // Email belum diverifikasi
                            Toast.makeText(this, "Email belum diverifikasi. Silakan cek inbox atau spam Anda.", Toast.LENGTH_LONG).show();

                            // Kirim ulang email verifikasi
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnSuccessListener(unused -> Toast.makeText(this, "Email verifikasi telah dikirim ulang.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal mengirim email verifikasi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            mAuth.signOut(); // logout agar user tidak lanjut login
                        }
                    }
                } else {
                    Toast.makeText(this, "Login gagal: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        });

        // Google Sign-In
        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Daftar akun baru
        btnToRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        // Reset password
        findViewById(R.id.textView5).setOnClickListener(v -> showForgotPasswordDialog());
    }

    // ===== Hanya ambil email dari Google Sign-In, tanpa login ke Firebase =====
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account == null) {
                Toast.makeText(this, "Login Google gagal: akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                return;
            }

            email = account.getEmail();
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "Email Google tidak tersedia", Toast.LENGTH_SHORT).show();
                return;
            }

            // langsung cek email di server
            cekUserDiServer(email);

        } catch (ApiException e) {
            Toast.makeText(this, "Login Google gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void cekUserDiServer(String email) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "cek_user.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "email=" + URLEncoder.encode(email, "UTF-8");

                try (OutputStream os = conn.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String response = reader.readLine();

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            String status = json.getString("status");
                            if ("terdaftar".equalsIgnoreCase(status)) {
                                userId = json.getInt("id");
                                companyId = json.getInt("company_id");
                                ktp = json.optString("ktp", ""); // aman meski null

                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("id", userId);
                                editor.putInt("company_id", companyId);
                                editor.putString("ktp", ktp);
                                editor.apply();

                                // 🔹 Simpan token FCM user ke server
                                saveFcmTokenToServer(email);

                                // 🔹 Cek apakah KTP kosong
                                if (ktp == null || ktp.trim().isEmpty() || ktp.equalsIgnoreCase("null")) {
                                    Toast.makeText(this, "Lengkapi data profil Anda terlebih dahulu.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(this, ProfileActivity.class);
                                    intent.putExtra("email", email);
                                    startActivity(intent);
                                } else {
                                    // Jika sudah punya KTP, langsung ke MainActivity
                                    startActivity(new Intent(this, MainActivity.class));
                                }

                                finish();
                            }
                            else if ("tidak_terdaftar".equalsIgnoreCase(status)) {
                                Intent intent = new Intent(this, RegisterActivity.class);
                                intent.putExtra("google_email", email);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Respons tidak dikenali dari server", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Gagal parsing respons server", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Kesalahan koneksi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveFcmTokenToServer(String email) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    new Thread(() -> {
                        try {
                            URL url = new URL(ApiConfig.BASE_URL + "save_fcm_token.php");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setDoOutput(true);

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("email", email);
                            jsonParam.put("token", token);

                            OutputStream os = conn.getOutputStream();
                            os.write(jsonParam.toString().getBytes());
                            os.flush();
                            os.close();

                            conn.getInputStream().close();
                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText emailInput = new EditText(this);
        emailInput.setHint("Masukkan email Anda");
        emailInput.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(emailInput);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Email reset terkirim (Cek spam)", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Gagal mengirim email reset", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
