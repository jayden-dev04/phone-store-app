package com.example.phonestore.model;

import java.io.Serializable;

public class Variant implements Serializable {

    private String capacity;
    private long price;
    private int stock;

    public Variant() {
    }

    public Variant(String capacity, long price, int stock) {
        this.capacity = capacity;
        this.price = price;
        this.stock = stock;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}