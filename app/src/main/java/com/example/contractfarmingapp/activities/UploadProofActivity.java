package com.example.contractfarmingapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.VolleyError;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.utils.VolleySingleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadProofActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView proofImageView;
    private TextView userEmailTextView, invoiceIdTextView, topUpAmountTextView, paymentMethodTextView;
    private Button selectImageButton, uploadProofButton;

    private Bitmap selectedBitmap;
    private String userEmail, invoiceId, paymentMethod, topUpAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_proof_activity);

        initViews();
        loadUserInfo();
        setupListeners();
    }

    private void initViews() {
        proofImageView = findViewById(R.id.proofImageView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        topUpAmountTextView = findViewById(R.id.topUpAmountTextView);
        paymentMethodTextView = findViewById(R.id.paymentMethodTextView);
        invoiceIdTextView = findViewById(R.id.invoiceIdTextView);
        selectImageButton = findViewById(R.id.selectImageButton);
        uploadProofButton = findViewById(R.id.uploadProofButton);
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Pengguna belum login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userEmail = user.getEmail();
        int rawAmount = getIntent().getIntExtra("amount", 0);
        paymentMethod = getIntent().getStringExtra("payment_method");
        int invoiceNumericId = getIntent().getIntExtra("invoice_id", 0);
        invoiceId = String.valueOf(invoiceNumericId);
        topUpAmount = String.valueOf(rawAmount);

        // Validasi semua data wajib ada
        if (userEmail == null || rawAmount <= 0 || paymentMethod == null || invoiceNumericId <= 0) {
            Toast.makeText(this, "Data top-up tidak lengkap atau tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userEmailTextView.setText("Email: " + userEmail);
        topUpAmountTextView.setText("Jumlah Top-Up: " + topUpAmount);
        paymentMethodTextView.setText("Metode Pembayaran: " + paymentMethod);
        invoiceIdTextView.setText("ID Invoice: #" + invoiceId);
    }

    private void setupListeners() {
        selectImageButton.setOnClickListener(v -> openImageChooser());
        uploadProofButton.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Silakan pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadProofButton.setEnabled(false);

            // Pindah ke StatusPayment lebih dulu
            Intent intent = new Intent(UploadProofActivity.this, StatusPayment.class);
            intent.putExtra("email", userEmail);
            intent.putExtra("amount", topUpAmount);
            intent.putExtra("payment_method", paymentMethod);
            intent.putExtra("invoice_id", invoiceId);
            startActivity(intent);

            // Jalankan upload di background
            uploadProofInBackground();
        });


        findViewById(R.id.backIcon).setOnClickListener(v -> finish());
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                selectedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                proofImageView.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.e("IMAGE_LOAD", "Gagal memuat gambar", e);
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProofInBackground() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengunggah bukti pembayaran...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String uploadUrl = "http://192.168.1.27:9090/contractfarming/topup";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, uploadUrl,
                response -> {
                    progressDialog.dismiss();
                    Log.i("UPLOAD_SUCCESS", "Upload berhasil: " + response);
                    // Tidak perlu pindah ke StatusPayment di sini, sudah dilakukan sebelumnya
                },
                error -> {
                    progressDialog.dismiss();
                    String errorMsg;
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    } else if (error.getMessage() != null) {
                        errorMsg = error.getMessage();
                    } else {
                        errorMsg = "Terjadi kesalahan tidak diketahui.";
                    }
                    Log.e("UPLOAD_ERROR", "Upload gagal: " + errorMsg, error);
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", userEmail);
                params.put("amount", topUpAmount);
                params.put("payment_method", paymentMethod);
                params.put("invoice_id", invoiceId);
                params.put("image", encodeImageToBase64(selectedBitmap));
                return params;
            }
        };

        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                5000,
                0,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
