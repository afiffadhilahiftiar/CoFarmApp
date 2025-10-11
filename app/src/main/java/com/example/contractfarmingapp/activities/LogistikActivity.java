package com.example.contractfarmingapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.adapters.OngkirAdapter;
import com.example.contractfarmingapp.models.OngkirModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LogistikActivity extends AppCompatActivity implements OngkirAdapter.OnOngkirSelectedListener {

    private RecyclerView recyclerView;
    private final List<OngkirModel> ongkirList = new ArrayList<>();
    private OngkirAdapter adapter;

    private static final String API_KEY = "Y0uedcHee7bc43d08d22dfbe8a8X2p4S"; // Ganti dengan API Key Komship Anda
    private static final String BASE_URL = "https://api-sandbox.collaborator.komerce.id/tariff/api/v1/calculate";

    private String shipperId = "31555";
    private String receiverId = "68423";
    private String originPinPoint = "-7.250445,112.768845";
    private String destinationPinPoint = "-7.795580,110.369490";
    private float weight = 1.7f;
    private int itemValue = 100000;
    private String cod = "yes";
    private int indexToko = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logistik);

        recyclerView = findViewById(R.id.recyclerOngkir);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OngkirAdapter(ongkirList, this, this);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        shipperId = intent.getStringExtra("origin") != null ? intent.getStringExtra("origin") : shipperId;
        receiverId = intent.getStringExtra("destination") != null ? intent.getStringExtra("destination") : receiverId;
        weight = intent.getFloatExtra("weight", weight);
        itemValue = intent.getIntExtra("item_value", itemValue);
        originPinPoint = intent.getStringExtra("origin_pin_point") != null ? intent.getStringExtra("origin_pin_point") : originPinPoint;
        destinationPinPoint = intent.getStringExtra("destination_pin_point") != null ? intent.getStringExtra("destination_pin_point") : destinationPinPoint;
        cod = intent.getStringExtra("cod") != null ? intent.getStringExtra("cod") : cod;
        indexToko = intent.getIntExtra("index_toko", -1);

        getOngkirKomship();
    }

    private void getOngkirKomship() {
        try {
            String url = BASE_URL +
                    "?shipper_destination_id=" + URLEncoder.encode(shipperId, "UTF-8") +
                    "&receiver_destination_id=" + URLEncoder.encode(receiverId, "UTF-8") +
                    "&origin_pin_point=" + URLEncoder.encode(originPinPoint, "UTF-8") +
                    "&destination_pin_point=" + URLEncoder.encode(destinationPinPoint, "UTF-8") +
                    "&weight=" + weight +
                    "&item_value=" + itemValue +
                    "&cod=" + URLEncoder.encode(cod, "UTF-8");

            Log.d("KOMSHIP_API_URL", url);

            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONObject data = json.getJSONObject("data");
                            JSONArray reguler = data.getJSONArray("calculate_reguler");

                            ongkirList.clear();

                            for (int i = 0; i < reguler.length(); i++) {
                                JSONObject item = reguler.getJSONObject(i);

                                OngkirModel model = new OngkirModel(
                                        item.optString("shipping_name", "-"),       // Courier name
                                        item.optString("service_name", "-"),        // Service type
                                        "",                                          // Description (optional)
                                        item.optInt("shipping_cost", 0),            // Estimated cost
                                        item.optString("etd", "-")                  // Estimated delivery time
                                );

                                ongkirList.add(model);
                            }

                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(this, "Gagal parsing JSON", Toast.LENGTH_SHORT).show();
                            Log.e("KOMSHIP_API", "Parsing Error: " + e.getMessage());
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Gagal mengambil ongkir", Toast.LENGTH_SHORT).show();
                        Log.e("KOMSHIP_API", "Volley Error: " + error.toString());
                    }) {

                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("x-api-key", API_KEY);
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        } catch (Exception e) {
            Log.e("KOMSHIP_API", "URL Encode Error: " + e.getMessage());
        }
    }

    @Override
    public void onOngkirSelected(OngkirModel selected) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("ongkir", selected.estimatedCost);
        resultIntent.putExtra("kurir", selected.courierName);
        resultIntent.putExtra("layanan", selected.serviceType);
        resultIntent.putExtra("etd", selected.estimatedDeliveryTime);
        resultIntent.putExtra("index_toko", indexToko);

        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
