package com.example.phonestore.data;

import com.example.phonestore.model.User;

public class AuthState {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final User user;
    private final String message;

    private AuthState(Status status, User user, String message) {
        this.status = status;
        this.user = user;
        this.message = message;
    }

    public static AuthState idle() {
        return new AuthState(Status.IDLE, null, null);
    }

    public static AuthState loading() {
        return new AuthState(Status.LOADING, null, null);
    }

    public static AuthState success(User user, String message) {
        return new AuthState(Status.SUCCESS, user, message);
    }

    public static AuthState error(String message) {
        return new AuthState(Status.ERROR, null, message);
    }

    public Status getStatus() {
        return status;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }
}
