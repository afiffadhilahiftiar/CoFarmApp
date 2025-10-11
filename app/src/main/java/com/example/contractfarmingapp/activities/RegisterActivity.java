package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;


import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    private EditText editNama, editEmail, editPassword, editConfirmPassword;
    private ImageView togglePassword, toggleConfirmPassword;
    private Button btnDaftar, btnToSudah;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editNama = findViewById(R.id.editNama);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        String googleEmail = getIntent().getStringExtra("google_email");
        if (googleEmail != null) {
            editEmail.setText(googleEmail);
            editEmail.setEnabled(false); // Tidak bisa diedit
        }

        togglePassword = findViewById(R.id.togglePassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        btnDaftar = findViewById(R.id.btnDaftar);
        btnToSudah = findViewById(R.id.btnToSudah);

        mAuth = FirebaseAuth.getInstance();

        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_on);
                isPasswordVisible = true;
            }
            editPassword.setSelection(editPassword.getText().length());
        });

        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
                isConfirmPasswordVisible = false;
            } else {
                editConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
                isConfirmPasswordVisible = true;
            }
            editConfirmPassword.setSelection(editConfirmPassword.getText().length());
        });

        btnDaftar.setOnClickListener(v -> {
            String nama = editNama.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String confirmPassword = editConfirmPassword.getText().toString().trim();

            if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Isi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(nama)
                                        .build();

                                user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();

                                        // Simpan ke MySQL
                                        simpanKeMySQL(nama, email);

                                        Intent intent = new Intent(this, ProfileActivity.class);
                                        intent.putExtra("nama", editNama.getText().toString()); // kirim nama dari field input
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(this, "Gagal menyimpan nama pengguna", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Gagal registrasi";
                            Toast.makeText(this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        btnToSudah.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void simpanKeMySQL(String nama, String email) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "register.php"); // Ganti IP sesuai server kamu
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String data = "nama=" + URLEncoder.encode(nama, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8");

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Response sukses (bisa ditambahkan log jika diperlukan)
                }

                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
