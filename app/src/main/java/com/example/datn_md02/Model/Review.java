package com.example.datn_md02.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Review implements Serializable {

    private String userId;
    private String userName;
    private String userAvatar;
    private String comment;
    private double rating;
    private long timestamp;

    private String productName;
    private String productImage;
    private List<String> imageUrls;

    private String variantColor; // ✅ Thêm
    private String variantSize;  // ✅ Thêm

    public Review() {
    }
    public Review(String userId, String userName, String userAvatar,
                  String comment, double rating, long timestamp,
                  String productName, String productImage,
                  List<String> imageUrls, String reviewId) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
        this.productName = productName;
        this.productImage = productImage;
        this.imageUrls = imageUrls;
        // Optional: dùng nếu bạn lưu reviewId riêng
        // this.reviewId = reviewId; // nếu bạn có biến này
    }


    public Review(String userId, String userName, String userAvatar, String comment, double rating, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.comment = comment;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getters & Setters

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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Date getCreatedAt() {
        return new Date(timestamp);
    }

    public void setTimestampToNow() {
        this.timestamp = System.currentTimeMillis();
    }

    public String getVariantColor() {
        return variantColor;
    }

    public void setVariantColor(String variantColor) {
        this.variantColor = variantColor;
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }
}
