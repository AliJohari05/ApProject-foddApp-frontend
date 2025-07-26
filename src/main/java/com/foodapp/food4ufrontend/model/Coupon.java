package com.foodapp.food4ufrontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coupon {
    private Integer id;
    @JsonProperty("coupon_code")
    private String couponCode;
    private String type; // enum: fixed, percent
    private Double value; // Use Double as per OpenAPI number type for value
    @JsonProperty("min_price")
    private Integer minPrice;
    @JsonProperty("user_count")
    private Integer userCount;
    @JsonProperty("start_date") // Matches format: date in OpenAPI
    private LocalDate startDate; // Consider using java.time.LocalDate
    @JsonProperty("end_date") // Matches format: date in OpenAPI
    private LocalDate endDate; // Consider using java.time.LocalDate

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }
    public Integer getUserCount() { return userCount; }
    public void setUserCount(Integer userCount) { this.userCount = userCount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}