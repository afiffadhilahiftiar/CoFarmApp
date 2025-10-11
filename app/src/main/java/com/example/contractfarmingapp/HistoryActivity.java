package com.example.contractfarmingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.activities.BankInstructionsActivity;
import com.example.contractfarmingapp.activities.DanaInstructionsActivity;
import com.example.contractfarmingapp.adapters.HistoryAdapter;
import com.example.contractfarmingapp.models.InvoiceHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private final List<InvoiceHistory> historyList = new ArrayList<>();
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        getUserEmail();
        setupAdapter();
        loadHistory();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = user.getEmail();
        } else {
            Toast.makeText(this, "Pengguna belum login", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupAdapter() {
        adapter = new HistoryAdapter(HistoryActivity.this, historyList);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(item -> {
            if ("approved".equalsIgnoreCase(item.getStatus())) {
                // Jika sudah approved, buka halaman detail bukti pembayaran
                Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
                intent.putExtra("id", item.getId());
                intent.putExtra("timestamp", item.getTimestamp());
                intent.putExtra("payment_method", item.getPaymentMethod());
                intent.putExtra("amount", item.getAmount());
                intent.putExtra("status", item.getStatus());
                startActivity(intent);
                return;
            }

            String method = item.getPaymentMethod();
            Intent intent;

            if ("BANK_TRANSFER".equalsIgnoreCase(method)) {
                intent = new Intent(HistoryActivity.this, BankInstructionsActivity.class);
            } else if ("EWALLET".equalsIgnoreCase(method)) {
                intent = new Intent(HistoryActivity.this, DanaInstructionsActivity.class);
            } else {
                Toast.makeText(this, "Metode: " + method, Toast.LENGTH_SHORT).show();
                return;
            }

            intent.putExtra("from_history", true);
            intent.putExtra("id", item.getId());
            intent.putExtra("timestamp", item.getTimestamp());
            intent.putExtra("payment_method", item.getPaymentMethod());
            intent.putExtra("amount", item.getAmount());
            startActivity(intent);
        });
    }

    private void loadHistory() {
        String url = ApiConfig.BASE_URL + "get_invoice_history.php?email=" + userEmail;

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                reader.close();
                conn.disconnect();

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                JSONArray historyArray = jsonResponse.getJSONArray("history");

                historyList.clear();
                for (int i = 0; i < historyArray.length(); i++) {
                    JSONObject obj = historyArray.getJSONObject(i);
                    InvoiceHistory history = new InvoiceHistory(
                            obj.getInt("id"),
                            obj.getString("payment_method"),
                            obj.getInt("amount"),
                            obj.getString("timestamp"),
                            obj.getString("status")
                    );
                    historyList.add(history);
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Gagal mengambil riwayat: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
}
