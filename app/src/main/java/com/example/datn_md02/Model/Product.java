package com.example.datn_md02.Model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String description;
    private double price; // giá gốc (nếu không có variants thì dùng cái này)
    private String imageUrl;
    private String categoryId;
    private Date created;
    private Map<String, Map<String, Variant>> variants; // size -> color -> variant
    private List<Review> reviews;

    public Product() {
        // Constructor rỗng để Firebase deserialize
    }

    // ===== Getter & Setter =====

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

    // ===== Tiện ích cho lọc & sắp xếp =====

    /**
     * Lấy giá nhỏ nhất trong các variants.
     * Nếu không có variants thì trả về price gốc.
     */
    public double getMinPrice() {
        double min = Double.MAX_VALUE;

        if (variants != null && !variants.isEmpty()) {
            for (Map<String, Variant> colorMap : variants.values()) {
                if (colorMap != null) {
                    for (Variant v : colorMap.values()) {
                        if (v != null && v.getPrice() > 0 && v.getPrice() < min) {
                            min = v.getPrice();
                        }
                    }
                }
            }
        }

        if (min == Double.MAX_VALUE) {
            return price;
        }
        return min;
    }

    /**
     * Lấy giá lớn nhất trong các variants.
     * Nếu không có variants thì trả về price gốc.
     */
    public double getMaxPrice() {
        double max = 0;

        if (variants != null && !variants.isEmpty()) {
            for (Map<String, Variant> colorMap : variants.values()) {
                if (colorMap != null) {
                    for (Variant v : colorMap.values()) {
                        if (v != null && v.getPrice() > max) {
                            max = v.getPrice();
                        }
                    }
                }
            }
        }

        if (max == 0) {
            return price;
        }
        return max;
    }
}
