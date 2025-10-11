package com.example.contractfarmingapp.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class InvoiceUploader {

    public interface UploadCallback {
        void onSuccess(int invoiceId);
        void onFailure(String errorMessage);
    }

    public static void uploadInvoice(Context context, String email, int amount, String method, String timestamp, UploadCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.27:8080/contractfarming/insert_invoice.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("amount", String.valueOf(amount));
                params.put("payment_method", method);
                params.put("timestamp", timestamp);

                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();

                    Log.d("InvoiceUploader", "Response: " + result.toString());

                    JSONObject responseJson = new JSONObject(result.toString());
                    boolean success = responseJson.optBoolean("success", false);

                    if (success) {
                        int invoiceId = responseJson.optInt("invoice_id", -1);
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(invoiceId));
                    } else {
                        String message = responseJson.optString("message", "Gagal menyimpan invoice.");
                        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(message));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Server error: " + responseCode));
                }

            } catch (Exception e) {
                Log.e("InvoiceUploader", "Exception: ", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure("Exception: " + e.getMessage()));
            }
        }).start();
    }
}
