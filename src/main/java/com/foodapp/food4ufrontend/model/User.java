package com.foodapp.food4ufrontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    @JsonProperty("full_name")
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private String address;
    @JsonProperty("profileImageBase64")
    private String profileImageBase64; // Used for sending image data on signup
    @JsonProperty("bank_info")
    private BankInfo bankInfo;
    @JsonProperty("wallet_balance")
    private BigDecimal walletBalance;
    private String status;
    @JsonProperty("profile_image_url") // ADDED: Field for displaying profile image URL from backend
    private String profileImageUrl;


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BankInfo {
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("account_number")
        private String accountNumber;

        // Getters and Setters for BankInfo
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    }

    // Getters and Setters for User
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getProfileImageBase64() { return profileImageBase64; }
    public void setProfileImageBase64(String profileImageBase64) { this.profileImageBase64 = profileImageBase64; }
    public BankInfo getBankInfo() { return bankInfo; }
    public void setBankInfo(BankInfo bankInfo) { this.bankInfo = bankInfo; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // NEW: Getter and Setter for profileImageUrl
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}