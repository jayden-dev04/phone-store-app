package com.example.phonestore.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private String productId;
    private String productName;
    private String productImage;
    private String brand;
    private String capacity;
    private long price;
    private int quantity;
    private boolean selected = true;

    public CartItem() {}

    public CartItem(String id, String productId, String productName, String productImage, String brand, String capacity, long price, int quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.brand = brand;
        this.capacity = capacity;
        this.price = price;
        this.quantity = quantity;
        this.selected = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
