package com.example.contractfarmingapp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegionCodeActivity extends AppCompatActivity {

    private static final String TAG = "REGION_CODE";
    private static final String API_KEY = "Y0uedcHee7bc43d08d22dfbe8a8X2p4S";
    private static final String BASE_URL = "https://api-sandbox.collaborator.komerce.id/tariff/api/v1/destination/search?keyword=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String kodePos = getIntent().getStringExtra("kodepos");
        if (kodePos == null || kodePos.length() < 3) {
            Toast.makeText(this, "Kode pos tidak valid", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Log.d(TAG, "Cek validasi kode pos: " + kodePos);
        checkValidKodePos(kodePos);
    }

    private void checkValidKodePos(String kodePos) {
        String url = BASE_URL + kodePos;
        Log.d(TAG, "URL API: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d(TAG, "Response API: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray dataArray = jsonResponse.optJSONArray("data");

                        if (dataArray != null && dataArray.length() > 0) {
                            // Ada region ID, valid
                            JSONObject firstData = dataArray.getJSONObject(0);
                            String regionId = firstData.optString("id", "");

                            if (regionId != null && !regionId.isEmpty()) {
                                Log.d(TAG, "Valid kode pos. Region ID: " + regionId);
                                setResult(Activity.RESULT_OK);
                            } else {
                                Toast.makeText(this, "Kode pos tidak valid", Toast.LENGTH_SHORT).show();
                                setResult(Activity.RESULT_CANCELED);
                            }
                        } else {
                            Toast.makeText(this, "Kode pos tidak ditemukan", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_CANCELED);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing error", e);
                        Toast.makeText(this, "Gagal memproses data lokasi", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_CANCELED);
                    }
                    finish();
                },
                error -> {
                    Log.e(TAG, "API request error", error);
                    Toast.makeText(this, "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-api-key", API_KEY);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
