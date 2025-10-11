package com.example.contractfarmingapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        // Bisa coba ambil "invoice_url" dulu, kalau null coba "url"

        String invoiceUrl = getIntent().getStringExtra("invoice_url");
        if (invoiceUrl == null) {
            invoiceUrl = getIntent().getStringExtra("url");
        }

        if (invoiceUrl != null && (invoiceUrl.startsWith("http://") || invoiceUrl.startsWith("https://"))) {
            webView.loadUrl(invoiceUrl);
        } else {
            Toast.makeText(this, "URL tidak valid atau tidak ditemukan", Toast.LENGTH_LONG).show();
            finish(); // tutup activity kalau URL tidak valid
        }
    }
}
