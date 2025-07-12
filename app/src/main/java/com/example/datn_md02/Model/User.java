package com.example.datn_md02.Model;

public class User {
    private String name;
    private String email;
    private String role;
    private String fullName;
    private long timestamp; // ✅ Đã sửa tên thành timestamp

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public User(String name, String email, String role, long timestamp) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.timestamp = timestamp;
    }

    // ✅ Getter & Setter
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName != null ? fullName : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
