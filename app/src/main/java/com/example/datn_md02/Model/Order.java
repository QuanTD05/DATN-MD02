package com.example.datn_md02.Model;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private String receiverName;
    private String receiverAddress;
    private String paymentMethod;
    private String cardLastFour; // chỉ khi dùng thẻ
    private List<CartItem> items;
    private double subtotal;
    private double shippingFee;
    private double discount;
    private double totalAmount;
    private String couponCode;
    private long timestamp;
    private String status; // ví dụ: "pending", "shipped", "completed"

    public Order() {}

    public Order(String orderId, String userId, String receiverName, String receiverAddress,
                 String paymentMethod, String cardLastFour, List<CartItem> items,
                 double subtotal, double shippingFee, double discount, double totalAmount,
                 String couponCode, long timestamp, String status) {
        this.orderId = orderId;
        this.userId = userId;
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.paymentMethod = paymentMethod;
        this.cardLastFour = cardLastFour;
        this.items = items;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.totalAmount = totalAmount;
        this.couponCode = couponCode;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters và Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
