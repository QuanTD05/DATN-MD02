package com.example.datn_md02.Model;

public class Promotion {
    private String code;
    private String description;
    private int discount;
    private boolean is_active;
    private boolean apply_to_all;
    private String start_date;
    private String end_date;

    public Promotion() {}

    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getDiscount() { return discount; }
    public boolean isIs_active() { return is_active; }
    public boolean isApply_to_all() { return apply_to_all; }
    public String getStart_date() { return start_date; }
    public String getEnd_date() { return end_date; }

    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setDiscount(int discount) { this.discount = discount; }
    public void setIs_active(boolean is_active) { this.is_active = is_active; }
    public void setApply_to_all(boolean apply_to_all) { this.apply_to_all = apply_to_all; }
    public void setStart_date(String start_date) { this.start_date = start_date; }
    public void setEnd_date(String end_date) { this.end_date = end_date; }
}
