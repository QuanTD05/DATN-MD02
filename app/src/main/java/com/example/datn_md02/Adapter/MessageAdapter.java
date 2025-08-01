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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_sent_img, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_received_img, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        Context context = holder.itemView.getContext();

        String content = msg.getDisplayContent();
        boolean hasImage = msg.getImageUrl() != null && !msg.getImageUrl().trim().isEmpty();
        boolean hasText = !TextUtils.isEmpty(content);

        String timeText = formatTimestamp(msg.getTimestamp());

        if (holder instanceof SentViewHolder) {
            SentViewHolder h = (SentViewHolder) holder;
            h.tvMsg.setVisibility(hasText ? View.VISIBLE : View.GONE);
            h.tvMsg.setText(content);
            h.tvTime.setText(timeText);

            if (hasImage) {
                h.imgMsg.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(msg.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(h.imgMsg);
            } else {
                h.imgMsg.setVisibility(View.GONE);
            }

        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder h = (ReceivedViewHolder) holder;
            h.tvMsg.setVisibility(hasText ? View.VISIBLE : View.GONE);
            h.tvMsg.setText(content);
            h.tvTime.setText(timeText);

            if (hasImage) {
                h.imgMsg.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(msg.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(h.imgMsg);
            } else {
                h.imgMsg.setVisibility(View.GONE);
            }
        }
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "";
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

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
}
