package com.example.datn_md02.Model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Message {
    private String sender;
    private String receiver;

    // Schema mới:
    //  - Text:  content = text,      image = false
    //  - Image: content = image URL, image = true
    private String content;

    // Tương thích ngược (dữ liệu cũ)
    private String message;   // text cũ
    private String imageUrl;  // URL ảnh cũ

    private long timestamp;

    // Cờ phân biệt ảnh/text (schema mới)
    private Boolean image;    // dùng Boolean để có thể null

    // Trạng thái đã xem
    private boolean seen = false;

    public Message() {}

    // Text
    public Message(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.message = content;  // compat cũ
        this.timestamp = timestamp;
        this.image = false;
    }

    // Image (schema mới: URL nằm trong content)
    public Message(String sender, String receiver, String imageUrl, long timestamp, boolean isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.content = imageUrl;   // NEW schema
        this.imageUrl = imageUrl;  // compat cũ
        this.message = "";
        this.image = true;
    }

    // ==== Getters / Setters ====
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // CHỈ GIỮ get/setImage — KHÔNG có isImage() để tránh xung đột
    public Boolean getImage() { return image; }
    public void setImage(Boolean image) { this.image = image; }

    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    // ===== Helpers không map lên Firebase =====
    @Exclude
    public boolean isImageFlag() { return image != null && image; }

    @Exclude
    public String getDisplayContent() {
        if (content != null && !content.trim().isEmpty()) return content;
        return message != null ? message : "";
    }
}
