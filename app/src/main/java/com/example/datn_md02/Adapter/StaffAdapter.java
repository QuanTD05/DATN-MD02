package com.example.datn_md02.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.User;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.VH> {
    public interface OnClick { void onClick(User u); }
    private final List<User> list;
    private final Context ctx;
    private final OnClick listener;

    public StaffAdapter(List<User> list, Context ctx, OnClick listener) {
        this.list = list;
        this.ctx = ctx;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_staff, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = list.get(pos);
        Glide.with(ctx)
                .load(u.getAvatar())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(h.ivAvatar);

        h.tvName.setText(u.getFullName());
        h.tvLast.setText(u.getLastMessageText());
        h.tvTime.setText(
                u.getLastMessageTimestamp() > 0
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(u.getLastMessageTimestamp()))
                        : ""
        );

        // Màu nền nếu chưa đọc
        h.itemView.setBackgroundColor(u.isHasUnread()
                ? Color.parseColor("#E0F2FF")
                : Color.WHITE
        );

        // Chấm trạng thái
        int c = u.isOnline()
                ? Color.parseColor("#34D399") // xanh
                : Color.parseColor("#A0AEC0"); // xám
        h.vDot.setBackgroundTintList(ColorStateList.valueOf(c));

        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLast, tvTime;
        View vDot;
        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.imgAvatar);
            tvName   = v.findViewById(R.id.tvStaffName);
            tvLast   = v.findViewById(R.id.tvLastMessage);
            tvTime   = v.findViewById(R.id.tvTimestamp);
            vDot     = v.findViewById(R.id.statusDot);
        }
    }
}
