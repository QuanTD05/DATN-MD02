package com.example.datn_md02.Model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productId;
    private String productName;
    private String productImage;
    private String variant; // VD: "Màu: Đỏ - Size: M"
    private int quantity;
    private double price;

    public CartItem() {}

    // ✅ Constructor đầy đủ
    public CartItem(String productId, String productName, String productImage, String variant, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.variant = variant;
        this.quantity = quantity;
        this.price = price;
    }

    // ✅ Constructor không có productId (vẫn dùng được, nhớ gọi setProductId sau)
    public CartItem(String productName, String productImage, String variant, int quantity, double price) {
        this.productName = productName;
        this.productImage = productImage;
        this.variant = variant;
        this.quantity = quantity;
        this.price = price;
    }

    // Getter & Setter
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // ✅ Lấy màu từ chuỗi variant
    public String getVariantColor() {
        if (variant != null && variant.contains("Màu:")) {
            String[] parts = variant.split(" - ");
            for (String part : parts) {
                if (part.trim().startsWith("Màu:")) {
                    return part.replace("Màu:", "").trim();
                }
            }
        }
        return null;
    }

    // ✅ Lấy size từ chuỗi variant
    public String getVariantSize() {
        if (variant != null && variant.contains("Size:")) {
            String[] parts = variant.split(" - ");
            for (String part : parts) {
                if (part.trim().startsWith("Size:")) {
                    return part.replace("Size:", "").trim();
                }
            }
        }
        return null;
    }
}
