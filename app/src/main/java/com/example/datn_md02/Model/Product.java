package com.example.datn_md02.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String description;
    private double price; // optional, nếu không dùng có thể bỏ
    private String imageUrl;
    private String categoryId; // ✅ Sửa lại thành String
    private Date created;      // Nếu không dùng timestamp, có thể chuyển sang String
    private Map<String, Map<String, Variant>> variants;
    private List<Review> reviews;

    public Product() {
        // Constructor rỗng để Firebase deserialize
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Map<String, Map<String, Variant>> getVariants() {
        return variants;
    }

    public void setVariants(Map<String, Map<String, Variant>> variants) {
        this.variants = variants;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
