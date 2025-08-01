package com.example.datn_md02.Model;

public class Message {
    private String sender;
    private String receiver;
    private String content;
    private String message;
    private String imageUrl;
    private long timestamp;

    // ✅ Thêm thuộc tính seen
    private boolean seen = false;

    public Message() {}

    // Constructor cho tin nhắn văn bản
    public Message(String sender, String receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.message = content;
        this.timestamp = timestamp;
    }

    // Constructor cho tin nhắn hình ảnh
    public Message(String sender, String receiver, String imageUrl, long timestamp, boolean isImage) {
        this.sender = sender;
        this.receiver = receiver;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.content = "";
        this.message = "";
    }

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

    public String getDisplayContent() {
        return (content != null && !content.trim().isEmpty())
                ? content
                : (message != null ? message : "");
    }

    // ✅ Getter & Setter cho seen
    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
