package com.foodapp.food4ufrontend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AvailableDelivery {
    private int id;
    private String deliveryAddress;
    private int customerId;
    private int restaurantId;
    private List<Integer> itemIds;
    private BigDecimal payPrice;
    private String status;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public int getCustomerId() { return customerId; }
    public int getRestaurantId() { return restaurantId; }
    public List<Integer> getItemIds() { return itemIds; }
    public BigDecimal getPayPrice() { return payPrice; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) {
        this.id = id;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setItemIds(List<Integer> itemIds) {
        this.itemIds = itemIds;
    }

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
