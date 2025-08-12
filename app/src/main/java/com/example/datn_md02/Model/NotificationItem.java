package com.example.datn_md02.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    public String id;        // id của thông báo (để update read status)
    public String title;
    public String message;
    public Object timestamp;
    public String type = "order"; // mặc định là order
    public boolean read = false;  // trạng thái đã đọc/chưa đọc

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public NotificationItem() {}

    public NotificationItem(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public NotificationItem(long millis, String message) {
        this.title = "🛒 Đặt hàng thành công";
        this.message = message;
        this.timestamp = millis;
    }

    public String getFormattedTime() {
        try {
            if (timestamp instanceof Long) {
                return new SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault())
                        .format(new Date((Long) timestamp));
            } else if (timestamp instanceof String) {
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        .parse((String) timestamp);
                return new SimpleDateFormat("HH:mm dd-MM-yy", Locale.getDefault()).format(date);
            }
        } catch (Exception ignored) {}
        return String.valueOf(timestamp);
    }
}
