package com.example.contractfarmingapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.contractfarmingapp.R;

public class FormTambahLahanActivity extends AppCompatActivity {

    private EditText editNamaLahan, editLuasLahan, editLokasi;
    private Button btnSimpanLahan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tambah_lahan);

        editNamaLahan = findViewById(R.id.editNamaLahan);
        editLuasLahan = findViewById(R.id.editLuasLahan);
        editLokasi = findViewById(R.id.editLokasi);
        btnSimpanLahan = findViewById(R.id.btnSimpanLahan);

        btnSimpanLahan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String namaLahan = editNamaLahan.getText().toString();
                String luasLahan = editLuasLahan.getText().toString();
                String lokasi = editLokasi.getText().toString();

                if (!namaLahan.isEmpty() && !luasLahan.isEmpty() && !lokasi.isEmpty()) {
                    Toast.makeText(FormTambahLahanActivity.this, "Data Lahan Disimpan", Toast.LENGTH_SHORT).show();
                    finish(); // Balik ke Dashboard
                } else {
                    Toast.makeText(FormTambahLahanActivity.this, "Lengkapi semua data!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}