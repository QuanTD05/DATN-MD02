package com.example.datn_md02.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.CartItem;
import com.example.datn_md02.Model.Order;
import com.example.datn_md02.OrderDetailActivity;
import com.example.datn_md02.ReviewActivity;
import com.example.datn_md02.R;
import com.example.datn_md02.Util.OrderStatus;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orderList;

    private static final String[] RETURN_REASONS = new String[]{
            "Sản phẩm lỗi",
            "Giao sai",
            "Không đúng mô tả",
            "Quá lâu",
            "Khác"
    };

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvCustomer.setText(order.getReceiverName() != null ? order.getReceiverName() : "Khách hàng");
        holder.tvDate.setText(formatTimestamp(order.getTimestamp()));
        holder.tvAddress.setText(order.getReceiverAddress() != null ? order.getReceiverAddress() : "Không có địa chỉ");
        holder.tvTotal.setText("Tổng: " + formatCurrency(order.getTotalAmount()));
        holder.tvStatus.setText("Trạng thái: " + humanReadableStatus(order.getStatus()));
        holder.tvProducts.setText(buildProductSummary(order.getItems()));

        holder.imgAvatar.setImageResource(R.drawable.ic_user);

        holder.btnDetail.setOnClickListener(v -> openDetail(order));
        holder.itemView.setOnClickListener(v -> openDetail(order));

        String status = order.getStatus() != null ? order.getStatus().toLowerCase(Locale.ROOT) : "";

        holder.btnAction.setVisibility(View.GONE);
        holder.btnAction.setText("Thao tác");
        holder.btnAction.setOnClickListener(null);

        if (OrderStatus.PENDING.equalsIgnoreCase(status)) {
            // Chờ xử lý: "Nhận hàng" -> ondelivery, "Huỷ"
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Huỷ");
            holder.btnAction.setOnClickListener(v -> confirmCancel(order, position));
        } else if (OrderStatus.ON_DELIVERY.equalsIgnoreCase(status)) {
            // Đang giao: "Xác nhận nhận hàng" -> completed, "Huỷ"
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Thao tác");
            holder.btnAction.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnAction);
                popup.getMenu().add("Xác nhận nhận hàng");
                popup.getMenu().add("Huỷ");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Xác nhận nhận hàng".equals(item.getTitle())) {
                        updateStatus(order, OrderStatus.COMPLETED, position);
                    } else if ("Huỷ".equals(item.getTitle())) {
                        confirmCancel(order, position);
                    }
                    return true;
                });
                popup.show();
            });
         } else if (OrderStatus.COMPLETED.equalsIgnoreCase(status)) {
        // Hoàn thành: đánh giá / yêu cầu hoàn trả
        holder.btnAction.setVisibility(View.VISIBLE);
        holder.btnAction.setText("Thao tác");
        holder.btnAction.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnAction);
            popup.getMenu().add("Đánh giá");
            popup.getMenu().add("Yêu cầu hoàn trả");
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if ("Đánh giá".equals(title)) {
                    ArrayList<CartItem> reviewItems = new ArrayList<>();
                    for (CartItem ci : order.getItems()) {
                        if (ci.getProductId() == null || ci.getProductId().isEmpty()) {
                            Toast.makeText(context, "Thiếu productId cho sản phẩm: " + ci.getProductName(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        reviewItems.add(ci);
                    }
                    Intent intent = new Intent(context, ReviewActivity.class);
                    intent.putExtra("items", reviewItems);
                    if (!(context instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    context.startActivity(intent);
                } else if ("Yêu cầu hoàn trả".equals(title)) {
                    requestReturn(order, position);
                }
                return true;
            });
            popup.show();
        });
    }
 else if (OrderStatus.RETURN_REQUESTED.equalsIgnoreCase(status)) {
            // Yêu cầu hoàn trả: huỷ yêu cầu / đã hoàn trả
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("Thao tác");
            holder.btnAction.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, holder.btnAction);
                popup.getMenu().add("Huỷ yêu cầu hoàn trả");
                popup.getMenu().add("Đã hoàn trả");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Huỷ yêu cầu hoàn trả".equals(item.getTitle())) {
                        updateStatus(order, OrderStatus.COMPLETED, position);
                    } else if ("Đã hoàn trả".equals(item.getTitle())) {
                        updateStatus(order, OrderStatus.RETURNED, position);
                    }
                    return true;
                });
                popup.show();
            });
        } else {
            holder.btnAction.setVisibility(View.GONE);
        }

        // tô màu trạng thái
        if (OrderStatus.CANCELLED.equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else if (OrderStatus.RETURN_REQUESTED.equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else if (OrderStatus.RETURNED.equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else if (OrderStatus.COMPLETED.equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.teal_700));
        } else if (OrderStatus.ON_DELIVERY.equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.purple_700));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void openDetail(Order order) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("order", order);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void openReview(Order order) {
        Intent intent = new Intent(context, ReviewActivity.class);
        intent.putExtra("order", order);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private void confirmCancel(Order order, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận huỷ")
                .setMessage("Bạn có chắc chắn muốn huỷ đơn này?")
                .setPositiveButton("Huỷ", (dialog, which) -> updateStatus(order, OrderStatus.CANCELLED, position))
                .setNegativeButton("Không", null)
                .show();
    }

    private void requestReturn(Order order, int position) {
        String status = order.getStatus() != null ? order.getStatus().toLowerCase(Locale.ROOT) : "";
        if (!OrderStatus.COMPLETED.equalsIgnoreCase(status)) {
            Toast.makeText(context, "Chỉ yêu cầu hoàn trả khi đơn đã hoàn thành.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] selectedIndex = { -1 };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chọn lý do hoàn trả");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_single_choice, RETURN_REASONS) {
            @NonNull
            @Override
            public View getView(int positionView, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(positionView, convertView, parent);
                if (positionView == selectedIndex[0]) {
                    tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
                    tv.setBackgroundColor(0xFFEFEFEF); // nền sáng để nổi bật
                } else {
                    tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
                    tv.setBackgroundColor(Color.TRANSPARENT);
                }
                return tv;
            }
        };

        builder.setSingleChoiceItems(adapter, -1, (dialog, which) -> {
            selectedIndex[0] = which;
            adapter.notifyDataSetChanged();
        });

        builder.setPositiveButton("Tiếp", (dialog, which) -> {
            if (selectedIndex[0] == -1) {
                Toast.makeText(context, "Vui lòng chọn lý do", Toast.LENGTH_SHORT).show();
                return;
            }
            String chosen = RETURN_REASONS[selectedIndex[0]];
            if ("Khác".equals(chosen)) {
                EditText input = new EditText(context);
                input.setHint("Nhập lý do hoàn trả");
                new AlertDialog.Builder(context)
                        .setTitle("Lý do hoàn trả")
                        .setView(input)
                        .setPositiveButton("Gửi yêu cầu", (d2, w2) -> {
                            String customReason = input.getText().toString().trim();
                            if (TextUtils.isEmpty(customReason)) {
                                Toast.makeText(context, "Bạn phải nhập lý do", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            sendReturnRequest(order, position, customReason);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            } else {
                sendReturnRequest(order, position, chosen);
            }
        });
        builder.setNegativeButton("Huỷ", null);
        builder.show();
    }

    private void sendReturnRequest(Order order, int position, String reason) {
        String userId = order.getUserId();
        String orderId = order.getOrderId();
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(orderId)) {
            Toast.makeText(context, "Dữ liệu không đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(userId)
                .child(orderId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", OrderStatus.RETURN_REQUESTED);

        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> returnInfo = new HashMap<>();
                    returnInfo.put("reason", reason);
                    returnInfo.put("requestedAt", System.currentTimeMillis());
                    orderRef.child("return").updateChildren(returnInfo)
                            .addOnSuccessListener(aVoid2 -> {
                                order.setStatus(OrderStatus.RETURN_REQUESTED);
                                notifyItemChanged(position);
                                Toast.makeText(context, "Đã gửi yêu cầu hoàn trả", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e2 -> {
                                Toast.makeText(context, "Lưu lý do thất bại: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Gửi yêu cầu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatus(Order order, String newStatus, int position) {
        if (order == null) return;
        String userId = order.getUserId();
        String orderId = order.getOrderId();
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(orderId)) {
            Toast.makeText(context, "Thiếu userId hoặc orderId", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("orders")
                .child(userId)
                .child(orderId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    order.setStatus(newStatus);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Cập nhật: " + newStatus, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderAdapter", "Update thất bại", e);
                    Toast.makeText(context, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String buildProductSummary(List<CartItem> items) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (CartItem it : items) {
            sb.append(it.getProductName() != null ? it.getProductName() : "Sản phẩm")
                    .append(" x").append(it.getQuantity())
                    .append(" ").append(formatCurrency(it.getPrice()));
            if (!TextUtils.isEmpty(it.getVariant())) {
                sb.append(" (").append(it.getVariant()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }

    private String formatTimestamp(long tsMillis) {
        if (tsMillis <= 0) return "";
        Date d = new Date(tsMillis);
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(d);
    }

    private String humanReadableStatus(String raw) {
        if (raw == null) return "";
        raw = raw.toLowerCase(Locale.ROOT);
        if (raw.equals(OrderStatus.PENDING)) return "Chờ xử lý";
        if (raw.equals(OrderStatus.ON_DELIVERY)) return "Đang giao";
        if (raw.equals(OrderStatus.COMPLETED)) return "Đã hoàn thành";
        if (raw.equals(OrderStatus.CANCELLED)) return "Đã huỷ";
        if (raw.equals(OrderStatus.RETURN_REQUESTED)) return "Yêu cầu hoàn trả";
        if (raw.equals(OrderStatus.RETURNED)) return "Đã hoàn trả";
        return raw;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvCustomer, tvDate, tvAddress, tvProducts, tvStatus, tvTotal;
        Button btnDetail, btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_avatar);
            tvCustomer = itemView.findViewById(R.id.tv_customer);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvProducts = itemView.findViewById(R.id.tv_products);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvTotal = itemView.findViewById(R.id.tv_total);
            btnDetail = itemView.findViewById(R.id.btn_detail);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}
