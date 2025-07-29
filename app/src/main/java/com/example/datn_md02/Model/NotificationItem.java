package com.example.datn_md02.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationItem {
    public String title;
    public String message;
    public Object timestamp;

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

    public String type = "order"; // mặc định là "order" nếu không set

    public NotificationItem() {}

    public NotificationItem(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        // không set type để giữ mặc định là "order"
    }

    public NotificationItem(long millis, String message) {
        this.title = "🛒 Đặt hàng thành công";
        this.message = message;
        this.timestamp = millis; // giữ dạng Long
        // type vẫn mặc định là "order"
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
