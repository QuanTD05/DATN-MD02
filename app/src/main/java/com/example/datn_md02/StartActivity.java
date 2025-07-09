package com.example.datn_md02;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("is_first_launch", true);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // ✅ Nếu không phải lần đầu mở app => tự chuyển luôn
        if (!isFirstLaunch) {
            if (currentUser != null) {
                startActivity(new Intent(this, UserActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
            return;
        }

        // ✅ Nếu là lần đầu mở => Hiện StartActivity
        setContentView(R.layout.activity_start);
        btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(v -> {
            // Đánh dấu đã mở lần đầu
            prefs.edit().putBoolean("is_first_launch", false).apply();

            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            finish();
        });
    }
}
