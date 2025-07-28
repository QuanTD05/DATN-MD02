package com.example.datn_md02;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView txtSignup, txtForgot;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            checkUserRoleAndNavigate(mAuth.getCurrentUser());
            return;
        }

        setContentView(R.layout.activity_login);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        txtSignup = findViewById(R.id.txt_signup);
        txtForgot = findViewById(R.id.txtForgot);

        loginButton.setOnClickListener(v -> loginUser());

        // ✅ Mở màn hình quên mật khẩu mới
        txtForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // Đăng ký (spannable link)
        SpannableString span = new SpannableString("Bạn chưa có tài khoản? Đăng ký");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        };

        int start = span.toString().indexOf("Đăng ký");
        int end = start + "Đăng ký".length();

        span.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#47A94B")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtSignup.setText(span);
        txtSignup.setMovementMethod(LinkMovementMethod.getInstance());
        txtSignup.setHighlightColor(Color.TRANSPARENT);
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRoleAndNavigate(user);
                        }
                    } else {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkUserRoleAndNavigate(FirebaseUser user) {
        usersRef.child(user.getUid()).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String role = snapshot.getValue(String.class);
                            if ("user".equalsIgnoreCase(role)) {
                                navigateToUserScreen();
                            } else {
                                mAuth.signOut();
                                Toast.makeText(LoginActivity.this, "Không có quyền truy cập", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this, "Tài khoản không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToUserScreen() {
        startActivity(new Intent(this, UserActivity.class));
        finish();
    }
}
