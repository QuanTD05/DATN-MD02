package com.example.datn_md02.Model;

public class User {
    private String name;
    private String email;
    private String role;
    private String fullName; // ✅ thêm dòng này nếu cần fullName riêng
    private long lastMessageTimestamp;

    public User() {} // Firebase cần constructor rỗng

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // Getter & Setter
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getFullName() { return fullName != null ? fullName : name; } // dùng name nếu fullName null

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}
