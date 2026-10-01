package com.example.phonestore.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.matches("^0[0-9]{9}$");
    }

    public static boolean isValidEmailOrPhone(String input) {
        if (TextUtils.isEmpty(input)) return false;
        return isValidEmail(input) || isValidPhone(input);
    }

    // Validate mật khẩu từ 6 ký tự trở lên
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) return false;
        return password.length() >= 6;
    }

    // Kiểm tra mật khẩu nhập lại trùng khớp
    public static boolean isPasswordMatching(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }
}