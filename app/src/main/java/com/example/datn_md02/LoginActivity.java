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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.datn_md02.NotificationApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pusher.pushnotifications.PushNotifications;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button   loginButton;
    private TextView txtSignup, txtForgot;

    private FirebaseAuth    mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1) Khởi tạo Firebase Auth & Database
        mAuth    = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // 2) Bind các view
        emailEditText    = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton      = findViewById(R.id.login);
        txtSignup        = findViewById(R.id.txt_signup);
        txtForgot        = findViewById(R.id.txtForgot);

        // 3) Thiết lập click listener
        loginButton.setOnClickListener(v -> loginUser());
        txtForgot .setOnClickListener(v ->
                startActivity(new Intent(this, ResetPasswordActivity.class))
        );

        // 4) Spannable cho "Đăng ký"
        SpannableString span = new SpannableString("Bạn chưa có tài khoản? Đăng ký");
        ClickableSpan clickable = new ClickableSpan() {
            @Override public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        };
        int start = span.toString().indexOf("Đăng ký");
        span.setSpan(clickable, start, start + "Đăng ký".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.parseColor("#47A94B")),
                start, start + "Đăng ký".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtSignup.setText(span);
        txtSignup.setMovementMethod(LinkMovementMethod.getInstance());
        txtSignup.setHighlightColor(Color.TRANSPARENT);
    }

    private void loginUser() {
        String email    = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5) Đăng nhập với FirebaseAuth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    String uid = user.getUid();

                    // 6) Subscribe Pusher Beams interest
                    PushNotifications.addDeviceInterest("user_" + uid);

                    // 7) Lấy FCM token và gửi lên server
                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {
                                NotificationApiClient.registerToken(token);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi lấy token: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                    // 8) Kiểm tra role và điều hướng
                    checkUserRoleAndNavigate(user);
                });
    }

    private void checkUserRoleAndNavigate(FirebaseUser user) {
        usersRef.child(user.getUid()).child("role")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String role = snap.getValue(String.class);
                        if ("user".equalsIgnoreCase(role)) {
                            // Ví dụ điều hướng sang StartActivity
                            startActivity(new Intent(LoginActivity.this, StartActivity.class));
                            finish();
                        } else {
                            mAuth.signOut();
                            Toast.makeText(LoginActivity.this,
                                    "Không có quyền truy cập", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {}
                });
    }
}
