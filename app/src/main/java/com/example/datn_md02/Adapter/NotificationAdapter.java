package com.example.datn_md02.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotiViewHolder> {

    private final List<NotificationItem> list;
    private final Context context;

    public NotificationAdapter(Context context, List<NotificationItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        NotificationItem item = list.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);
        holder.tvTime.setText(item.getFormattedTime());

        // Hiển thị khác cho chưa đọc
        if (!item.read) {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_light));
        } else {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Icon loại thông báo
        if (item.type == null) {
            holder.imgIcon.setImageResource(R.drawable.ic_notification);
        } else {
            switch (item.type) {
                case "promo":
                case "promotions":
                    holder.imgIcon.setImageResource(R.drawable.icon_khuyenmai);
                    break;
                case "order":
                case "orders":
                    holder.imgIcon.setImageResource(R.drawable.ic_donhang);
                    break;
                default:
                    holder.imgIcon.setImageResource(R.drawable.ic_notification);
                    break;
            }
        }

        // Click => đánh dấu đã đọc
        holder.itemView.setOnClickListener(v -> {
            if (!item.read) {
                markAsRead(item);
                item.read = true;
                notifyItemChanged(position);
            }
        });
    }

    private void markAsRead(NotificationItem item) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && item.id != null) {
            FirebaseDatabase.getInstance()
                    .getReference("user_notifications")
                    .child(uid)
                    .child(item.id)
                    .child("read")
                    .setValue(true);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class NotiViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvTitle, tvMessage, tvTime;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgTypeIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
