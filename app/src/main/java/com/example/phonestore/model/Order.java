package com.example.phonestore.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    public static final String STATUS_PAID = "Đã thanh toán";
    public static final String STATUS_CANCELLED = "Đã hủy";

    private String orderId;
    private String userEmail;
    private String userName;
    private String userPhone;
    private String shippingAddress;
    private long orderDate;
    private long totalAmount;
    private long discountAmount;
    private String paymentMethod;
    private String status = STATUS_PAID;
    private String cancelReason;
    private List<CartItem> items = new ArrayList<>();

    public Order() {}

    public Order(String orderId, String userEmail, String userName, String userPhone, String shippingAddress, long orderDate, long totalAmount, long discountAmount, String paymentMethod, String status, List<CartItem> items) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPhone = userPhone;
        this.shippingAddress = shippingAddress;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public long getOrderDate() { return orderDate; }
    public void setOrderDate(long orderDate) { this.orderDate = orderDate; }

    public long getTotalAmount() { return totalAmount; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }

    public long getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(long discountAmount) { this.discountAmount = discountAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
