// ApProject_foddApp_frontend/src/main/java/com/foodapp/food4ufrontend/model/Rating.java

package com.foodapp.food4ufrontend.model; // NEW: پکیج فرانت‌اند

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime; // اگر زمان را به عنوان LocalDateTime مدیریت می‌کنید

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rating {
    private Integer id;
    @JsonProperty("user_id")
    private Integer userId; // از بک‌اند Integer می‌گیریم
    @JsonProperty("order_id")
    private Integer orderId;
    @JsonProperty("menu_item_id")
    private Integer menuItemId;
    private Integer rating; // 1-5
    private String comment;
    @JsonProperty("image_url")
    private String imageUrl; // URL تصویر در بک‌اند ذخیره می‌شود، در فرانت‌اند Base64 می‌گیریم
    @JsonProperty("created_at")
    private String createdAt; // به عنوان String در فرانت‌اند برای سادگی (می‌تواند LocalDateTime باشد)
    @JsonProperty("updated_at")
    private String updatedAt; // به عنوان String در فرانت‌اند

    // --- Getters and Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    public Integer getMenuItemId() { return menuItemId; }
    public void setMenuItemId(Integer menuItemId) { this.menuItemId = menuItemId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // مهم: متدهای equals و hashCode را بر اساس ID اضافه کنید
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating1 = (Rating) o;
        return id != null && id.equals(rating1.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}