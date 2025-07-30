package com.example.datn_md02.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.datn_md02.R;

public class PayFragment extends AppCompatActivity {
    private ImageView btnBack;
    private Button btnCheckout;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCOD, rbCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pay); // tên file XML bạn gửi là activity_checkout.xml

        // Ánh xạ view
        btnBack = findViewById(R.id.btnBack);
        btnCheckout = findViewById(R.id.btnCheckout);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCOD = findViewById(R.id.rbCOD);
        rbCard = findViewById(R.id.rbCard);

        setupListeners();
    }

    private void setupListeners() {
        // Quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // Xử lý khi nhấn nút "THANH TOÁN"
        btnCheckout.setOnClickListener(v -> {
            int checkedId = rgPaymentMethod.getCheckedRadioButtonId();
            String paymentMethod = (checkedId == R.id.rbCOD) ? "Thanh toán khi nhận hàng" : "Thẻ ngân hàng";

            Toast.makeText(this, "Đang thanh toán bằng: " + paymentMethod, Toast.LENGTH_SHORT).show();

            // TODO: Gửi đơn hàng, gọi API, hoặc hiển thị màn hình hoàn tất thanh toán
        });
    }
}
