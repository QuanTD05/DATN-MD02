package com.example.datn_md02.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    private String sender;
    private String receiver;
    private String content;   // text
    private String imageUrl;  // ảnh
    private long timestamp;
    private boolean isImage;  // phân biệt text / ảnh
    private boolean seen;     // trạng thái đã xem

    public Message() {
        // Firebase cần constructor rỗng
    }

    // Gửi text
    public Message(String sender, String receiver, String text, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = text;
        this.imageUrl = null;
        this.timestamp = timestamp;
        this.isImage = false;
        this.seen = false;
    }

    // Gửi ảnh
    public Message(String sender, String receiver, String imageUrl, long timestamp, boolean isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.isImage = isImage;
        this.seen = false;

        if (isImage) {
            this.imageUrl = imageUrl;
            this.content = null;
        } else {
            this.content = imageUrl;
            this.imageUrl = null;
        }
    }

    // ===== Getter / Setter =====
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isImage() { return isImage; }
    public void setImage(boolean image) { isImage = image; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    // ===== Helpers không lưu lên Firebase =====
    @Exclude
    public String getDisplayContent() {
        return isImage ? "[Hình ảnh]" : (content != null ? content : "");
    }
}
