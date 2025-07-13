package com.example.datn_md02.Product;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.datn_md02.R;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        ImageView fullImage = findViewById(R.id.fullImage);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.haha)
                    .into(fullImage);
        }

        // Đóng khi click ảnh
        fullImage.setOnClickListener(v -> finish());
    }
}
