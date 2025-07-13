package com.example.datn_md02.Model;

public class VariantDisplay {
    public String size;
    public String color;
    public double price;
    public int quantity;
    public String imageUrl;

    public VariantDisplay() {
        // Constructor mặc định bắt buộc nếu cần Firebase hoặc deserialization
    }

    public VariantDisplay(String size, String color, double price, int quantity, String imageUrl) {
        this.size = size;
        this.color = color;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getter và Setter nếu bạn cần dùng kiểu getSize(), getColor(), ...
    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
