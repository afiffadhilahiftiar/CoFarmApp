package com.example.contractfarmingapp;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.contractfarmingapp.adapters.ChatPetaniAdapter;
import com.example.contractfarmingapp.models.ChatItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DaftarChatPetaniActivityPoktan extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatPetaniAdapter adapter;
    private List<ChatItem> chatList = new ArrayList<>();
    private String companyId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_chat_petani);

        rvChats = findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatPetaniAdapter(chatList, item -> {
            Intent intent = new Intent(DaftarChatPetaniActivityPoktan.this, ChatActivityPerusahaan.class);

            try {
                int receiverId = Integer.parseInt(item.getSenderId());
                intent.putExtra("receiver_id", receiverId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(DaftarChatPetaniActivityPoktan.this, "ID petani tidak valid", Toast.LENGTH_SHORT).show();
                return; // jangan lanjutkan jika ID tidak valid
            }

            intent.putExtra("nama_admin", item.getSenderName());
            startActivity(intent);
        });

        rvChats.setAdapter(adapter);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        companyId = sharedPreferences.getString("company_id", null);

        if (companyId != null) {
            loadChats();
        } else {
            Toast.makeText(this, "ID perusahaan tidak tersedia", Toast.LENGTH_SHORT).show();
        }

    }

    private void loadChats() {
        String url =  ApiConfig.BASE_URL + "get_messages_for_company_poktan.php?company_id=" + companyId;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    chatList.clear();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String senderId = obj.getString("sender_id");
                            String senderName = obj.getString("sender_name");
                            String lastMessage = obj.getString("last_message");

                            chatList.add(new ChatItem(senderId, senderName, lastMessage));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
}

