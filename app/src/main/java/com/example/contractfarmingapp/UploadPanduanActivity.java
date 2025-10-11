package com.example.contractfarmingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class UploadPanduanActivity extends AppCompatActivity {
    EditText edtKondisi, edtTindakan;
    Button btnKirim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_panduan);

        edtKondisi = findViewById(R.id.edtKondisi);
        edtTindakan = findViewById(R.id.edtTindakan);
        btnKirim = findViewById(R.id.btnKirim);

        btnKirim.setOnClickListener(v -> {
            String kondisi = edtKondisi.getText().toString();
            String tindakan = edtTindakan.getText().toString();

            if (!kondisi.isEmpty() && !tindakan.isEmpty()) {
                Toast.makeText(this, "Panduan berhasil dikirim.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Isi semua kolom.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

