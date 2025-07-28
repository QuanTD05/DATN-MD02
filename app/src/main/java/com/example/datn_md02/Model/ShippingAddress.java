package com.example.datn_md02.Model;

import java.io.Serializable;

public class ShippingAddress implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String street;
    private String ward;
    private String district;
    private String city;
    private boolean isDefault;

    public ShippingAddress() {} // Constructor rỗng cho Firebase

    public ShippingAddress(String id, String name, String phone,
                           String street, String ward, String district,
                           String city, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.ward = ward;
        this.district = district;
        this.city = city;
        this.isDefault = isDefault;
    }

    // Getter/Setter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getStreet() { return street; }
    public String getWard() { return ward; }
    public String getDistrict() { return district; }
    public String getCity() { return city; }
    public boolean isDefault() { return isDefault; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStreet(String street) { this.street = street; }
    public void setWard(String ward) { this.ward = ward; }
    public void setDistrict(String district) { this.district = district; }
    public void setCity(String city) { this.city = city; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }



    // Optional: Lấy địa chỉ đầy đủ gộp lại
    public String getFullAddress() {
        return street + ", " + ward + ", " + district + ", " + city;
    }
}
