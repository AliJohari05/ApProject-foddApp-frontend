package com.foodapp.food4ufrontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodItem {
private Integer id;
private String name;
private String imageBase64;
private String description;
//@JsonProperty("vendor_id")
//    private Integer vendorId;
private Integer price;
private Integer supply;
@JsonProperty("keywords")
private String[] keywords;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getImageBase64() {
        return imageBase64;
    }
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }



    public Integer getPrice() {
        return price;
    }
    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSupply() {
        return supply;
    }
    public void setSupply(Integer supply) {
        this.supply = supply;
    }

    public String[] getKeywords() {
        return keywords;
    }
    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }
}
