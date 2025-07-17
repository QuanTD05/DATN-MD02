package com.example.datn_md02.Model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String productId;
    private String name;
    private String imageUrl;
    private double price;
    private int quantity;
    private boolean selected; // ✅ thêm thuộc tính này

    public CartItem() {
    }

    public CartItem(String productId, String name, String imageUrl, double price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.selected = true; // ✅ mặc định được chọn
    }

    // Getter và Setter
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isSelected() { // ✅ cần method này
        return selected;
    }

    public void setSelected(boolean selected) { // ✅ và method này
        this.selected = selected;
    }
}
