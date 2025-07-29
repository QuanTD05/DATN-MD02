package com.example.datn_md02.Model;

import java.io.Serializable;

public class Cart implements Serializable {
    private String cartId;
    private String productId;
    private String productName;
    private String imageUrl;
    private int quantity;
    private double price;
    private boolean selected;

    private String variantSize;   // ✅ thêm
    private String variantColor;  // ✅ thêm

    public Cart() {}

    public Cart(String cartId, String productId, String productName, String imageUrl,
                int quantity, double price, boolean selected,
                String variantSize, String variantColor) {
        this.cartId = cartId;
        this.productId = productId;
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
        this.selected = selected;
        this.variantSize = variantSize;
        this.variantColor = variantColor;
    }

    // Getter & Setter
    public String getCartId() { return cartId; }
    public void setCartId(String cartId) { this.cartId = cartId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public String getVariantSize() { return variantSize; }
    public void setVariantSize(String variantSize) { this.variantSize = variantSize; }

    public String getVariantColor() { return variantColor; }
    public void setVariantColor(String variantColor) { this.variantColor = variantColor; }
}
