package com.example.phonestore.model;

import java.io.Serializable;

public class User implements Serializable {
    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_USER = 2;

    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private long createdAt;
    private int role = ROLE_USER;

    public User() {}

    public User(String uid, String fullName, String email, String phone, long createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.role = ROLE_USER;
    }

    public User(String uid, String fullName, String email, String phone, long createdAt, int role) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.role = role;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }
}