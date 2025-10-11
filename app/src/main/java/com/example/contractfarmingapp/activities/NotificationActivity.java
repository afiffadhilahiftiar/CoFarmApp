package com.example.contractfarmingapp.activities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contractfarmingapp.ApiConfig;
import com.example.contractfarmingapp.R;
import com.example.contractfarmingapp.adapters.NotificationAdapter;
import com.example.contractfarmingapp.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> allNotifications = new ArrayList<>();
    private List<NotificationModel> currentList = new ArrayList<>();
    private Button buttonAll, buttonUnread, buttonRead;
    private String userEmail = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userEmail = currentUser.getEmail();

        initViews();
        setupRecyclerView();
        showSwipeHint();  // <-- menampilkan hint modern
        setupFilterButtons();
        loadNotificationsFromServer();

    }

    private void initViews() {

        recyclerView = findViewById(R.id.recyclerViewNotification);
        buttonAll = findViewById(R.id.buttonAll);
        buttonUnread = findViewById(R.id.buttonUnread);
        buttonRead = findViewById(R.id.buttonRead);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(currentList);
        recyclerView.setAdapter(adapter);

        adapter.setOnNotificationClickListener(notification -> {
            notification.setRead(true);
            adapter.notifyDataSetChanged();
            markNotificationAsRead(notification.getId());
        });

        setupSwipeToDelete();
    }
    private void showSwipeHint() {
        // Inflate layout overlay
        final View hintView = getLayoutInflater().inflate(R.layout.swipe_hint, recyclerView, false);

        // Tambahkan overlay di atas RecyclerView
        addContentView(hintView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView ivArrow = hintView.findViewById(R.id.ivArrow);

        // Animasi panah bergerak ke kanan dan kembali
        ivArrow.animate()
                .translationX(50f)
                .setDuration(500)
                .withEndAction(() -> ivArrow.animate()
                        .translationX(0f)
                        .setDuration(500)
                        .start())
                .start();

        // Hilangkan overlay setelah 3 detik
        hintView.postDelayed(() -> {
            if (hintView.getParent() != null) {
                ((ViewGroup) hintView.getParent()).removeView(hintView);
            }
        }, 3000);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                NotificationModel notification = currentList.get(position);

                currentList.remove(position);
                allNotifications.remove(notification);
                adapter.notifyItemRemoved(position);

                deleteNotificationFromServer(notification.getId());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                Paint paint = new Paint();
                paint.setColor(Color.RED);
                View itemView = viewHolder.itemView;

                if (dX > 0) {
                    c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);
                } else {
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void setupFilterButtons() {
        buttonAll.setOnClickListener(v -> {
            currentList = new ArrayList<>(allNotifications);
            adapter.updateList(currentList);
        });

        buttonUnread.setOnClickListener(v -> {
            List<NotificationModel> filtered = new ArrayList<>();
            for (NotificationModel n : allNotifications) {
                if (!n.isRead()) filtered.add(n);
            }
            adapter.updateList(filtered);
        });

        buttonRead.setOnClickListener(v -> {
            List<NotificationModel> filtered = new ArrayList<>();
            for (NotificationModel n : allNotifications) {
                if (n.isRead()) filtered.add(n);
            }
            adapter.updateList(filtered);
        });
    }

    private void loadNotificationsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "get_notifications.php?email=" + URLEncoder.encode(userEmail, "UTF-8"));
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                InputStream inputStream = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONObject responseObject = new JSONObject(result.toString());
                if (responseObject.getBoolean("success")) {
                    JSONArray jsonArray = responseObject.getJSONArray("notifications");
                    allNotifications.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        NotificationModel model = new NotificationModel(
                                obj.getInt("id"),
                                obj.getString("title"),
                                obj.getString("message"),
                                obj.getString("time"),
                                obj.getBoolean("isRead")

                        );
                        allNotifications.add(model);
                    }

                    runOnUiThread(() -> {
                        currentList = new ArrayList<>(allNotifications);
                        adapter.updateList(currentList);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void markNotificationAsRead(int id) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "mark_as_read.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", id);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                conn.getInputStream().close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteNotificationFromServer(int id) {
        new Thread(() -> {
            try {
                URL url = new URL(ApiConfig.BASE_URL + "delete_notification.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", id);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();
                os.close();

                conn.getInputStream().close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
