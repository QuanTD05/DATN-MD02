package com.example.datn_md02.Model;

import java.util.List;

public class Promotion {
    private String code;
    private String description;
    private int discount;              // % giảm
    private boolean is_active;
    private boolean apply_to_all;
    private String start_date;         // yyyy-MM-dd
    private String end_date;           // yyyy-MM-dd

    // ⭐️ Thêm: danh sách productId được áp dụng (khi apply_to_all = false)
    private List<String> apply_to_product_ids;

    public Promotion() {}

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getDiscount() { return discount; }
    public boolean isIs_active() { return is_active; }
    public boolean isApply_to_all() { return apply_to_all; }
    public String getStart_date() { return start_date; }
    public String getEnd_date() { return end_date; }

    // Null-safe: có thể trả về null nếu DB chưa có field này
    public List<String> getApply_to_product_ids() { return apply_to_product_ids; }

    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscount(int discount) { this.discount = discount; }
    public void setIs_active(boolean is_active) { this.is_active = is_active; }
    public void setApply_to_all(boolean apply_to_all) { this.apply_to_all = apply_to_all; }
    public void setStart_date(String start_date) { this.start_date = start_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }

    public void setApply_to_product_ids(List<String> apply_to_product_ids) {
        this.apply_to_product_ids = apply_to_product_ids;
    }
}
