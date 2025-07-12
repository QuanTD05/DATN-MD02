package com.example.datn_md02.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02.Model.Message;
import com.example.datn_md02.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUser;

    public MessageAdapter(List<Message> messages, String currentUser) {
        this.messages = messages;
        this.currentUser = currentUser != null ? currentUser.trim().toLowerCase() : "";
    }

    @Override
    public int getItemViewType(int position) {
        String sender = messages.get(position).getSender() != null
                ? messages.get(position).getSender().trim().toLowerCase()
                : "";
        return sender.equals(currentUser) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_received, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        String content = getDisplayContent(msg);

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).tvMsg.setText(content);
        } else {
            ((ReceivedViewHolder) holder).tvMsg.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    /**
     * ✅ Ưu tiên lấy content, fallback sang message nếu cần
     */
    private String getDisplayContent(Message msg) {
        if (msg == null) return "";
        String content = msg.getContent();
        String message = msg.getMessage();
        return (content != null && !content.trim().isEmpty()) ? content : (message != null ? message : "");
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;

        SentViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsgSent);
        }
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvMsg;

        ReceivedViewHolder(View v) {
            super(v);
            tvMsg = v.findViewById(R.id.tvMsgReceived);
        }
    }
}
