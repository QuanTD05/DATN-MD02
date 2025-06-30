package com.example.datn_md02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private Button btnContinue;
    private CheckBox checkboxAgreement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btnContinue = findViewById(R.id.btnContinue);
        checkboxAgreement = findViewById(R.id.checkboxAgreement);

        // Ban đầu: nút Bắt đầu bị disable
        btnContinue.setEnabled(false);
        btnContinue.setAlpha(0.5f); // màu xám nhạt

        // Khi tick checkbox, thay đổi trạng thái nút
        checkboxAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnContinue.setEnabled(isChecked);
            btnContinue.setAlpha(isChecked ? 1.0f : 0.5f);
        });

        // Khi bấm Bắt đầu
        btnContinue.setOnClickListener(v -> {
            if (checkboxAgreement.isChecked()) {
                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Vui lòng đồng ý với điều khoản", Toast.LENGTH_SHORT).show();
            }
        });
    }
}