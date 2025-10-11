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

public class DanaInstructionsActivity extends AppCompatActivity {

    private static final String accountNumber = "089653961978";

    private TextView instructionsTextView, accountdanaNumberTextView, amountLabel,
            emailTextView,invoiceIdTextView, timestampTextView;
    private Button uploadButton;
    private ImageView backIcon;
    private int invoiceId;
    private LinearLayout rekeningdanaLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // UI komponen
        instructionsTextView = findViewById(R.id.instructionsTextView);
        accountdanaNumberTextView = findViewById(R.id.accountdanaNumberTextView);
        amountLabel = findViewById(R.id.amountLabel);
        emailTextView = findViewById(R.id.emailTextView);
        timestampTextView = findViewById(R.id.timestampTextView);
        uploadButton = findViewById(R.id.uploadButton);
        backIcon = findViewById(R.id.backIcon);
        rekeningdanaLayout = findViewById(R.id.rekeningdanaLayout);
        invoiceIdTextView = findViewById(R.id.invoiceIdTextView);
        // Ambil data intent
        String topup = getIntent().getStringExtra("topup");
        String paymentMethod = getIntent().getStringExtra("payment_method");
        int amount = getIntent().getIntExtra("amount", 0);
        int id = getIntent().getIntExtra("id", -1);
        String timestamp = getIntent().getStringExtra("timestamp");
        boolean fromHistory = getIntent().getBooleanExtra("from_history", false);

        // Format waktu saat ini
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Ambil email dari Firebase login
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = (user != null) ? user.getEmail() : "Tidak diketahui";
        timestampTextView.setText((timestamp != null && !timestamp.isEmpty()) ? timestamp : currentTime);

        // Tampilkan info profesional
        amountLabel.setText("Rp"+ String.format("%,d", amount).replace(',', '.'));
        emailTextView.setText(userEmail);
        invoiceId = id;

        if (invoiceId != -1) {
            invoiceIdTextView.setText("ID Invoice: #" + invoiceId);
        } else {
            invoiceIdTextView.setText("ID Invoice: -");
        }


        if ("Dana".equalsIgnoreCase(paymentMethod)) {
            rekeningdanaLayout.setVisibility(View.VISIBLE);
            accountdanaNumberTextView.setText(accountNumber);

            accountdanaNumberTextView.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Account Number", accountNumber);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Nomor rekening disalin", Toast.LENGTH_SHORT).show();
            });

            instructionsTextView.setText("Langkah Pembayaran via DANA:\n\n" +
                    "1. Buka aplikasi DANA.\n" +
                    "2. Pilih menu 'Kirim'.\n" +
                    "3. Masukkan nomor tujuan.\n" +
                    "4. Masukkan jumlah top-up: Rp" + String.format("%,d", amount).replace(',', '.') + "\n" +
                    "5. Konfirmasi dan lakukan pembayaran.\n\n" +
                    "⚠️ Setelah transfer, upload bukti pembayaran ke sistem untuk verifikasi." +
                    "Proses verifikasi kurang dari 15 menit setelah upload bukti pembayaran.");
        } else {
            rekeningdanaLayout.setVisibility(View.GONE);
            instructionsTextView.setText("Instruksi pembayaran akan ditampilkan di sini.");
        }
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
                            runOnUiThread(() -> Toast.makeText(DanaInstructionsActivity.this, errorMessage, Toast.LENGTH_LONG).show());
                        }
                    }
            );
        }
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(DanaInstructionsActivity.this, UploadProofActivity.class);
            intent.putExtra("amount", amount);
            intent.putExtra("payment_method", paymentMethod);
            intent.putExtra("invoice_id", invoiceId);
            startActivity(intent);
        });

        backIcon.setOnClickListener(v -> onBackPressed());
    }
}