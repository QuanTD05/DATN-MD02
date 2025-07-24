package com.example.datn_md02.Model;

import com.google.firebase.database.PropertyName;
import java.io.Serializable;

public class Variant implements Serializable {

    private String size;      // ✅ thêm
    private String color;     // ✅ thêm
    private double price;
    private int quantity;

    @PropertyName("image")
    private String imageUrl;

    public Variant() {
        // Bắt buộc cho Firebase
    }

    // ✅ Constructor đầy đủ
    public Variant(String size, String color, double price, int quantity, String imageUrl) {
        this.size = size;
        this.color = color;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // ✅ Getter và Setter cho size
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    // ✅ Getter và Setter cho color
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // ✅ Giá
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // ✅ Số lượng
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // ✅ Ảnh
    @PropertyName("image")
    public String getImageUrl() {
        return imageUrl;
    }

    @PropertyName("image")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
