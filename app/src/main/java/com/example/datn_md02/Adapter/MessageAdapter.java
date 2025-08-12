package com.example.datn_md02.Adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.Message;
import com.example.datn_md02.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<Message> messages;
    private final String currentUser;

    public MessageAdapter(List<Message> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser != null ? currentUser.trim().toLowerCase() : "";
    }

    @Override
    public int getItemViewType(int position) {
        String sender = messages.get(position).getSender();
        sender = sender != null ? sender.trim().toLowerCase() : "";
        return sender.equals(currentUser) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_sent_img, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_received_img, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        Context context = holder.itemView.getContext();

        // Nội dung text để hiển thị
        String content = safe(msg.getDisplayContent());
        String rawContent = safe(msg.getContent());
        String legacyImageUrl = safe(msg.getImageUrl());
        boolean imageFlag = msg.getImage() != null && msg.getImage(); // cờ schema mới

        // Quy tắc nhận dạng ảnh:
        // 1) image == true
        // 2) hoặc có imageUrl cũ
        // 3) hoặc content là URL http(s)
        boolean looksLikeUrl = rawContent.startsWith("http://") || rawContent.startsWith("https://");
        boolean isImage = imageFlag || !TextUtils.isEmpty(legacyImageUrl) || looksLikeUrl;

        // URL ảnh thực tế để load
        String imageToLoad = !TextUtils.isEmpty(legacyImageUrl) ? legacyImageUrl
                : (isImage ? rawContent : "");

        String timeText = formatTimestamp(msg.getTimestamp());

        if (holder instanceof SentViewHolder) {
            SentViewHolder h = (SentViewHolder) holder;

            if (isImage) {
                h.imgMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(imageToLoad)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.ic_chat)
                        .into(h.imgMsg);
            } else {
                h.imgMsg.setVisibility(View.GONE);
                h.tvMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setText(content);
            }
            h.tvTime.setText(timeText);

        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder h = (ReceivedViewHolder) holder;

            if (isImage) {
                h.imgMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(imageToLoad)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.ic_chat)
                        .into(h.imgMsg);
            } else {
                h.imgMsg.setVisibility(View.GONE);
                h.tvMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setText(content);
            }
            h.tvTime.setText(timeText);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    // ===== ViewHolders =====
    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ImageView imgMsg;
        SentViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsgSent);
            tvTime = v.findViewById(R.id.tvTimeSent);
            imgMsg = v.findViewById(R.id.imgMsgSent);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg, tvTime;
        ImageView imgMsg;
        ReceivedViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsgReceived);
            tvTime = v.findViewById(R.id.tvTimeReceived);
            imgMsg = v.findViewById(R.id.imgMsgReceived);
        }
    }

    // ===== Helpers =====
    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "";
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }
}
