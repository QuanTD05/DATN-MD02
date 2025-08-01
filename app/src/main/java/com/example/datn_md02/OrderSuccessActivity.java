// === File: OrderSuccessActivity.java ===
package com.example.datn_md02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.CartItemAdapter;
import com.example.datn_md02.Fragment.HomeFragment;
import com.example.datn_md02.Model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderSuccessActivity extends AppCompatActivity {

    private TextView tvOrderId, tvTotalAmount, tvSuccessMessage;
    private RecyclerView rvItems;
    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Gắn view
        tvOrderId = findViewById(R.id.tvOrderId);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        rvItems = findViewById(R.id.rvItems);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        // Lấy dữ liệu từ Intent
        String orderId = getIntent().getStringExtra("orderId");
        double totalAmount = getIntent().getDoubleExtra("totalAmount", 0);
        ArrayList<CartItem> items = (ArrayList<CartItem>) getIntent().getSerializableExtra("items");

        // Kiểm tra dữ liệu
        if (orderId == null || items == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hiển thị thông tin đơn
        tvOrderId.setText("Mã đơn hàng: " + orderId);
        tvTotalAmount.setText("Tổng thanh toán: " + NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalAmount));
        tvSuccessMessage.setText("\uD83C\uDF89 Cảm ơn bạn đã mua hàng!\nĐơn hàng của bạn đang được xử lý.");

        // Hiển thị sản phẩm
        CartItemAdapter adapter = new CartItemAdapter(items);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(adapter);

        // Nút quay về trang chủ
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomeFragment.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
