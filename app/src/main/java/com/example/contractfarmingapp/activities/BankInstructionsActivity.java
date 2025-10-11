package com.example.contractfarmingapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.utils.InvoiceUploader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BankInstructionsActivity extends AppCompatActivity {

    private static final String accountNumber = "123-00-000-456";

    private TextView instructionsTextView, accountNumberTextView, amountLabel,
            emailTextView, timestampTextView, invoiceIdTextView;
    private Button uploadButton;
    private ImageView backIcon;
    private LinearLayout rekeningLayout;

    private int invoiceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Inisialisasi UI
        instructionsTextView = findViewById(R.id.instructionsTextView);
        accountNumberTextView = findViewById(R.id.accountNumberTextView);
        amountLabel = findViewById(R.id.amountLabel);
        emailTextView = findViewById(R.id.emailTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        uploadButton = findViewById(R.id.uploadButton);
        backIcon = findViewById(R.id.backIcon);
        rekeningLayout = findViewById(R.id.rekeningLayout);
        invoiceIdTextView = findViewById(R.id.invoiceIdTextView);

        // Ambil data intent
        String paymentMethod = getIntent().getStringExtra("payment_method");
        int amount = getIntent().getIntExtra("amount", 0);
        int id = getIntent().getIntExtra("id", -1);
        String timestamp = getIntent().getStringExtra("timestamp");
        boolean fromHistory = getIntent().getBooleanExtra("from_history", false);

        // Ambil email user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "Tidak diketahui";

        // Format waktu saat ini
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Set timestamp (pakai yang dari intent kalau ada, jika tidak pakai waktu sekarang)
        timestampTextView.setText((timestamp != null && !timestamp.isEmpty()) ? timestamp : currentTime);

        // Set amount dan email
        amountLabel.setText("Rp" + String.format("%,d", amount).replace(',', '.'));
        emailTextView.setText(userEmail);

        // Set invoiceId dan tampilkan
        invoiceId = id;
        if (invoiceId != -1) {
            invoiceIdTextView.setText("ID Invoice: #" + invoiceId);
        } else {
            invoiceIdTextView.setText("ID Invoice: -");
        }

        // Tampilkan instruksi jika paymentMethod adalah Bank
        if ("Bank".equalsIgnoreCase(paymentMethod)) {
            rekeningLayout.setVisibility(View.VISIBLE);
            accountNumberTextView.setText(accountNumber);
            accountNumberTextView.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Account Number", accountNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Nomor rekening disalin", Toast.LENGTH_SHORT).show();
            });

            instructionsTextView.setText("Langkah Pembayaran via Bank Mandiri:\n\n" +
                    "1. Buka aplikasi Livin' by Mandiri atau ATM Mandiri.\n" +
                    "2. Pilih menu 'Transfer' lalu 'Transfer ke Rekening Mandiri'.\n" +
                    "3. Salin dan tempel nomor rekening tujuan di atas.\n" +
                    "4. Masukkan jumlah top-up: Rp" + String.format("%,d", amount).replace(',', '.') + "\n" +
                    "5. Konfirmasi dan selesaikan pembayaran.\n\n" +
                    "⚠️ Setelah transfer, upload bukti pembayaran ke sistem untuk verifikasi.\n" +
                    "Proses verifikasi kurang dari 15 menit setelah upload bukti pembayaran.");
        } else {
            rekeningLayout.setVisibility(View.GONE);
            instructionsTextView.setText("Instruksi pembayaran akan ditampilkan di sini.");
        }

        // Jika bukan dari history dan invoiceId belum ada, buat invoice baru
        if (!fromHistory && invoiceId == -1) {
            InvoiceUploader.uploadInvoice(
                    this,
                    userEmail,
                    amount,
                    paymentMethod,
                    currentTime,
                    new InvoiceUploader.UploadCallback() {
                        @Override
                        public void onSuccess(int id) {
                            invoiceId = id;
                            runOnUiThread(() -> invoiceIdTextView.setText("ID Invoice: #" + invoiceId));
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            runOnUiThread(() -> Toast.makeText(BankInstructionsActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                        }
                    }
            );
        }

        // Tombol upload bukti pembayaran
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(BankInstructionsActivity.this, UploadProofActivity.class);
            intent.putExtra("amount", amount);
            intent.putExtra("payment_method", paymentMethod);
            intent.putExtra("invoice_id", invoiceId);
            startActivity(intent);
        });

        // Tombol back
        backIcon.setOnClickListener(v -> onBackPressed());
    }
}
