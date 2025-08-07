package com.example.datn_md02.Model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String fullName;    // đổi từ name → fullName
    private String email;
    private String avatar;      // tương ứng với key “avatar” trong DB
    private String role;
    private long lastMessageTimestamp;
    private String lastMessageText;
    private boolean online;
    private int unreadCount;
    private boolean hasUnread;

    public User() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getLastMessageTimestamp() { return lastMessageTimestamp; }
    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) {
        this.lastMessageText = lastMessageText;
    }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isHasUnread() { return hasUnread; }
    public void setHasUnread(boolean hasUnread) { this.hasUnread = hasUnread; }
}
