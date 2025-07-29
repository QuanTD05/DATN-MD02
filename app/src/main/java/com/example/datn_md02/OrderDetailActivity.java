package com.example.datn_md02;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Adapter.CartOrderAdapter;
import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.Order;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvReceiver, tvAddress, tvPaymentMethod, tvTotal, tvStatus, tvCoupon;
    private RecyclerView rvOrderItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderId = findViewById(R.id.tvOrderId);
        tvReceiver = findViewById(R.id.tvReceiver);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        tvStatus = findViewById(R.id.tvStatus);
        tvCoupon = findViewById(R.id.tvCoupon);
        rvOrderItems = findViewById(R.id.rvOrderItems);

        Order order = (Order) getIntent().getSerializableExtra("order");

        if (order != null) {
            tvOrderId.setText("Mã đơn: " + order.getOrderId());
            tvReceiver.setText("Người nhận: " + order.getReceiverName());
            tvAddress.setText("Địa chỉ: " + order.getReceiverAddress());
            tvPaymentMethod.setText("Phương thức: " + order.getPaymentMethod());
            tvCoupon.setText("Mã giảm giá: " + (order.getCouponCode().isEmpty() ? "Không có" : order.getCouponCode()));
            tvTotal.setText(String.format(Locale.getDefault(), "Tổng tiền: ₫%,.0f", order.getTotalAmount()));
            tvStatus.setText("Trạng thái: " + translateStatus(order.getStatus()));

            CartOrderAdapter adapter = new CartOrderAdapter(convertCartItemToCart(order.getItems()));
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(adapter);
        }
    }

    private List<com.example.datn_md02.Model.Cart> convertCartItemToCart(List<CartItem> items) {
        java.util.ArrayList<com.example.datn_md02.Model.Cart> list = new java.util.ArrayList<>();
        for (CartItem item : items) {
            String color = "";
            String size = "";
            if (item.getVariant() != null && item.getVariant().contains(" - ")) {
                String[] parts = item.getVariant().replace("Màu: ", "").replace("Size: ", "").split(" - ");
                if (parts.length == 2) {
                    color = parts[0];
                    size = parts[1];
                }
            }
            list.add(new com.example.datn_md02.Model.Cart(
                    "", "", item.getProductName(), item.getProductImage(),
                    item.getQuantity(), item.getPrice(), false, size, color
            ));
        }
        return list;
    }

    private String translateStatus(String status) {
        switch (status) {
            case "pending": return "Chờ xử lý";
            case "completed": return "Đã hoàn thành";
            case "cancelled": return "Đã hủy";
            case "ondelivery": return "Đang giao hàng";
            default: return "Không xác định";
        }
    }
}
