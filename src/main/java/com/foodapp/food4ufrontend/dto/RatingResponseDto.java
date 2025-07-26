// ApProject_foddApp_frontend/src/main/java/com/foodapp/food4ufrontend/dto/RatingResponseDto.java

package com.foodapp.food4ufrontend.dto; // NEW: پکیج فرانت‌اند

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.food4ufrontend.model.Rating; // NEW: از مدل Rating فرانت‌اند استفاده کنید
import java.time.format.DateTimeFormatter; // NEW: وارد کنید
import java.util.List;
import java.util.Collections; // NEW: وارد کنید

public class RatingResponseDto {
    private Integer id;
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("item_id")
    private Integer itemId;
    private Integer rating;
    private String comment;
    @JsonProperty("imageBase64") // مطابق با YAML
    private List<String> imageBase64; // اگر imageBase64 را از بک‌اند برمی‌گردانید
    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("created_at")
    private String createdAt;

    public RatingResponseDto() {}

    public RatingResponseDto(Rating rating) {
        this.id = rating.getId();
        this.orderId = rating.getOrderId();
        this.itemId = rating.getMenuItemId();
        this.rating = rating.getRating();
        this.comment = rating.getComment();
        // اگر imageUrl در مدل Rating فرانت‌اند دارید
        this.imageBase64 = (rating.getImageUrl() != null && !rating.getImageUrl().isEmpty()) ?
                List.of(rating.getImageUrl()) : Collections.emptyList();
        this.userId = rating.getUserId();
        // DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // اگر createdAt از نوع LocalDateTime باشد
        this.createdAt = rating.getCreatedAt(); // اگر createdAt از نوع String باشد
    }

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<String> getImageBase64() { return imageBase64; }
    public void setImageBase64(List<String> imageBase64) { this.imageBase64 = imageBase64; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}