package com.example.phonestore.model;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Product implements Serializable {

    private String id;
    private String name;
    private String brand;
    private String short_description;
    private String detail_description;
    private List<String> images;
    private HashMap<String, Variant> variants;

    public Product() {
    }

    public Product(String id, String name, String brand, String short_description, String detail_description, List<String> images, HashMap<String, Variant> variants) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.short_description = short_description;
        this.detail_description = detail_description;
        this.images = images;
        this.variants = variants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    @PropertyName("short_description")
    public String getShort_description() {
        return short_description;
    }

    @PropertyName("short_description")
    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    @PropertyName("detail_description")
    public String getDetail_description() {
        return detail_description;
    }

    @PropertyName("detail_description")
    public void setDetail_description(String detail_description) {
        this.detail_description = detail_description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public HashMap<String, Variant> getVariants() {
        return variants;
    }

    public void setVariants(HashMap<String, Variant> variants) {
        this.variants = variants;
    }

    public long getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        long minPrice = Long.MAX_VALUE;
        for (Variant v : variants.values()) {
            if (v != null && v.getPrice() > 0 && v.getPrice() < minPrice) {
                minPrice = v.getPrice();
            }
        }
        return minPrice == Long.MAX_VALUE ? 0 : minPrice;
    }
}