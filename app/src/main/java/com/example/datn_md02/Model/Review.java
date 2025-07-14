package com.example.datn_md02.Model;

import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {

    private String userId;
    private String userName;
    private String userAvatar;
    private String comment;
    private double rating;
    private long timestamp;

    private String productName;     // 👉 THÊM MỚI
    private String productImage;    // 👉 THÊM MỚI

    public Review() {
    }

    public Review(String userId, String userName, String userAvatar, String comment, double rating, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // 👉 THÊM GET/SET CHO TÊN VÀ ẢNH SẢN PHẨM

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    // Trả về Date từ timestamp
    public Date getCreatedAt() {
        return new Date(timestamp);
    }

    // Tuỳ chọn: đặt timestamp = thời gian hiện tại
    public void setTimestampToNow() {
        this.timestamp = System.currentTimeMillis();
    }
}
