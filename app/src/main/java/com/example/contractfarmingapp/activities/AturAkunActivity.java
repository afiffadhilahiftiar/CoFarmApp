package com.example.contractfarmingapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class AturAkunActivity extends AppCompatActivity {

    private TextView txtEmailLama;
    private EditText editNewEmail;
    private Button btnChangeEmail, btnResetPassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private int userId; // ambil dari SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_akun);

        txtEmailLama = findViewById(R.id.txtEmailLama);
        editNewEmail = findViewById(R.id.editNewEmail);
        btnChangeEmail = findViewById(R.id.btnChangeEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Ambil user_id dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("id", -1);

        if (currentUser == null || userId == 0) {
            Toast.makeText(this, "User belum login atau user_id tidak tersedia", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tampilkan email lama
        txtEmailLama.setText(currentUser.getEmail());

        // Reset Password
        btnResetPassword.setOnClickListener(v -> {
            String email = currentUser.getEmail();
            if (email != null) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Email reset password dikirim ke " + email + "(Cek Spam)", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Gagal mengirim email reset: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Update Email
        btnChangeEmail.setOnClickListener(v -> {
            String newEmail = editNewEmail.getText().toString().trim();
            if (TextUtils.isEmpty(newEmail)) {
                editNewEmail.setError("Harap isi email baru");
                return;
            }

            // Update Firebase
            currentUser.updateEmail(newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update database juga pakai user_id
                            updateEmailDatabase(userId, newEmail);
                        } else {
                            Toast.makeText(this, "Gagal mengubah email di Firebase: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void updateEmailDatabase(int userId, String newEmail) {
        String url = ApiConfig.BASE_URL + "update_email.php"; // PHP script update email di DB
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "Email berhasil diperbarui di database!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Gagal update email di DB", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("new_email", newEmail);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);

        // Update tampilan
        txtEmailLama.setText(newEmail);
    }
}
