package com.example.contractfarmingapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;

public class HistoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        TextView tvId = findViewById(R.id.tvId);
        TextView tvAmount = findViewById(R.id.tvAmount);
        TextView tvMethod = findViewById(R.id.tvMethod);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvTimestamp = findViewById(R.id.tvTimestamp);

        // Ambil data dari intent
        int id = getIntent().getIntExtra("id", 0);
        int amount = getIntent().getIntExtra("amount", 0);
        String method = getIntent().getStringExtra("payment_method");
        String status = getIntent().getStringExtra("status");
        String timestamp = getIntent().getStringExtra("timestamp");

        // Format angka dengan titik ribuan (Indonesia)
        NumberFormat formatRupiah = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        String formattedAmount = formatRupiah.format(amount);

        // Tampilkan data ke UI
        tvId.setText("ID: " + id);
        tvAmount.setText("Rp" + formattedAmount);
        tvMethod.setText("Metode: " + method);
        tvStatus.setText("Status: " + status);
        tvTimestamp.setText("Waktu: " + timestamp);
    }
}
