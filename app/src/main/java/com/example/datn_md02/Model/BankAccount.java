
package com.example.datn_md02.Model;

public class BankAccount {
    private String id;
    private String bankName;
    private String cardHolderName;
    private String cardNumber;
    private String expiryDate;
    private boolean isDefault;

    public BankAccount() {}

    public BankAccount(String id, String bankName, String cardHolderName, String cardNumber, String expiryDate, boolean isDefault) {
        this.id = id;
        this.bankName = bankName;
        this.cardHolderName = cardHolderName;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
