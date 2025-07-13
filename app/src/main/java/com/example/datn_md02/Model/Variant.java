package com.example.datn_md02.Model;


import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class Variant implements Serializable {

    private double price;
    private int quantity;

    @PropertyName("image")
    private String imageUrl;

    public Variant() {}

    public Variant(double price, int quantity, String imageUrl) {
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @PropertyName("image")
    public String getImageUrl() { return imageUrl; }

    @PropertyName("image")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
