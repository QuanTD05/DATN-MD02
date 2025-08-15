package com.example.datn_md02.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02.Model.Message;
import com.example.datn_md02.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        String sender = safe(messages.get(position).getSender()).trim().toLowerCase();
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

        boolean isImage = msg.isImage();
        String content = safe(msg.getContent());
        String imageUrl = safe(msg.getImageUrl());
        String timeText = formatTimestamp(msg.getTimestamp());

        if (holder instanceof SentViewHolder) {
            SentViewHolder h = (SentViewHolder) holder;
            if (isImage && !TextUtils.isEmpty(imageUrl)) {
                h.imgMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.ic_chat)
                        .fitCenter()
                        .into(h.imgMsg);
            } else {
                h.imgMsg.setVisibility(View.GONE);
                h.tvMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setText(content);
            }
            h.tvTime.setText(timeText);

            // Long click -> Sửa/Xóa
            h.itemView.setOnLongClickListener(v -> {
                showMessageOptions(context, msg);
                return true;
            });

        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder h = (ReceivedViewHolder) holder;
            if (isImage && !TextUtils.isEmpty(imageUrl)) {
                h.imgMsg.setVisibility(View.VISIBLE);
                h.tvMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.ic_chat)
                        .fitCenter()
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

    // ===== Sửa / Xóa =====
    private void showMessageOptions(Context context, Message msg) {
        String[] options = {"Sửa tin nhắn", "Xóa tin nhắn"};
        new AlertDialog.Builder(context)
                .setTitle("Tùy chọn")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        editMessage(context, msg);
                    } else if (which == 1) {
                        confirmDeleteMessage(context, msg);
                    }
                })
                .show();
    }

    private void editMessage(Context context, Message message) {
        final EditText input = new EditText(context);
        input.setText(message.getContent());
        new AlertDialog.Builder(context)
                .setTitle("Sửa tin nhắn")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats");
                        chatRef.orderByChild("timestamp").equalTo(message.getTimestamp())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot child : snapshot.getChildren()) {
                                            Message snapMsg = child.getValue(Message.class);
                                            if (snapMsg != null &&
                                                    snapMsg.getSender().equals(message.getSender()) &&
                                                    snapMsg.getReceiver().equals(message.getReceiver()) &&
                                                    snapMsg.getTimestamp() == message.getTimestamp()) {
                                                child.getRef().child("content").setValue(newText);
                                                child.getRef().child("image").setValue(false);
                                                child.getRef().child("imageUrl").setValue(null);
                                                int index = messages.indexOf(message);
                                                if (index != -1) {
                                                    message.setContent(newText);
                                                    message.setImage(false);
                                                    message.setImageUrl(null);
                                                    notifyItemChanged(index);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmDeleteMessage(Context context, Message msg) {
        new AlertDialog.Builder(context)
                .setTitle("Xóa tin nhắn")
                .setMessage("Bạn có chắc chắn muốn xóa tin nhắn này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteMessage(msg))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteMessage(Message message) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats");
        chatRef.orderByChild("timestamp").equalTo(message.getTimestamp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Message snapMsg = child.getValue(Message.class);
                            if (snapMsg != null &&
                                    snapMsg.getSender().equals(message.getSender()) &&
                                    snapMsg.getReceiver().equals(message.getReceiver()) &&
                                    snapMsg.getTimestamp() == message.getTimestamp()) {
                                child.getRef().removeValue();
                                int index = messages.indexOf(message);
                                if (index != -1) {
                                    messages.remove(index);
                                    notifyItemRemoved(index);
                                }
                                break;
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
