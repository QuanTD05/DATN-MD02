package com.example.datn_md02.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.NotificationItem;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(item.getFormattedTime());
            SimpleDateFormat output = new SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault());
            holder.tvTime.setText(output.format(date));
        } catch (Exception e) {
            holder.tvTime.setText(item.getFormattedTime());
        }

        if (item.type == null) {
            holder.imgIcon.setImageResource(R.drawable.ic_notification);
        } else {
            switch (item.type) {
                case "promotions":
                    holder.imgIcon.setImageResource(R.drawable.ic_promo);
                    break;
                case "orders":
                    holder.imgIcon.setImageResource(R.drawable.ic_order);
                    break;
                default:
                    holder.imgIcon.setImageResource(R.drawable.ic_order);
                    break;
            }
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
