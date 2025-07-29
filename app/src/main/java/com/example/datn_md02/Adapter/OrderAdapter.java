package com.example.datn_md02.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.Order;
import com.example.datn_md02.OrderDetailActivity;
import com.example.datn_md02.R;
import com.example.datn_md02.ReviewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvCustomer.setText(order.getReceiverName());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "Thành tiền: ₫%,.0f", order.getTotalAmount()));
        holder.tvStatus.setText("Trạng thái: " + translateStatus(order.getStatus()));
        holder.tvDate.setText("Ngày đặt: " + formatDate(order.getTimestamp()));
        holder.tvAddress.setText(order.getReceiverAddress());

        // Hiển thị danh sách sản phẩm và biến thể
        StringBuilder productText = new StringBuilder();
        for (CartItem item : order.getItems()) {
            productText.append("- ").append(item.getProductName());
            if (item.getVariant() != null && !item.getVariant().isEmpty()) {
                productText.append(" (").append(item.getVariant()).append(")");
            }
            productText.append("\n  x").append(item.getQuantity())
                    .append(" → ₫").append(String.format(Locale.getDefault(), "%,.0f", item.getPrice()))
                    .append("\n");
        }
        holder.tvProducts.setText(productText.toString().trim());

        // Nút chi tiết
        holder.btnDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });

        holder.btnAction.setVisibility(View.GONE);

        switch (order.getStatus()) {
            case "ondelivery":
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText("Đã nhận hàng");
                holder.btnAction.setOnClickListener(v -> {
                    updateOrderStatus(order, "completed");
                });
                break;
            case "completed":
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText("Đánh giá");

                holder.btnAction.setOnClickListener(v -> {
                    ArrayList<CartItem> reviewItems = new ArrayList<>();
                    for (CartItem item : order.getItems()) {
                        if (item.getProductId() == null) {
                            // ⚠ Nếu thiếu thì báo lỗi ra log hoặc hiển thị
                            Toast.makeText(context, "Thiếu productId cho sản phẩm: " + item.getProductName(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        reviewItems.add(item);
                    }

                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("items", reviewItems);
                    context.startActivity(intent);
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvTotal, tvStatus, tvDate, tvProducts, tvAddress;
        Button btnDetail, btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomer = itemView.findViewById(R.id.tv_customer);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvProducts = itemView.findViewById(R.id.tv_products);
            tvAddress = itemView.findViewById(R.id.tv_address);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }

    private String translateStatus(String status) {
        switch (status) {
            case "pending": return "Chờ xử lý";
            case "ondelivery": return "Đang giao hàng";
            case "completed": return "Đã hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return "Không xác định";
        }
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void updateOrderStatus(Order order, String newStatus) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders")
                .child(order.getOrderId());
        ref.child("status").setValue(newStatus)
                .addOnSuccessListener(unused -> Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void reorderItems(List<CartItem> items) {
        DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        for (CartItem item : items) {
            String key = cartRef.push().getKey();
            cartRef.child(key).setValue(item);
        }

        Toast.makeText(context, "Đã thêm lại vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }
}
