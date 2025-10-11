package com.example.contractfarmingapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.io.File;

public class ImagePreviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ImageView imageView = findViewById(R.id.fullscreenImageView);
        String imagePath = getIntent().getStringExtra("image_url");

        if (imagePath != null) {
            imageView.setImageURI(Uri.fromFile(new File(imagePath)));
        }

        imageView.setOnClickListener(v -> finish());
    }

}
