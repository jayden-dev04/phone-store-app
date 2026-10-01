package com.example.phonestore.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.phonestore.data.AuthRepository;
import com.example.phonestore.data.AuthState;
import com.example.phonestore.model.User;
import com.example.phonestore.utils.ValidationUtils;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.idle());

    public LoginViewModel() {
        this.repository = new AuthRepository();
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public void login(String account, String password) {
        String trimmedAccount = account != null ? account.trim() : "";
        String trimmedPassword = password != null ? password.trim() : "";

        if (TextUtils.isEmpty(trimmedAccount) || TextUtils.isEmpty(trimmedPassword)) {
            authState.setValue(AuthState.error("Vui lòng nhập đầy đủ tài khoản và mật khẩu."));
            return;
        }

        if (!ValidationUtils.isValidEmailOrPhone(trimmedAccount)) {
            authState.setValue(AuthState.error("Tài khoản phải là Email hoặc Số điện thoại hợp lệ."));
            return;
        }

        authState.setValue(AuthState.loading());

        repository.login(trimmedAccount, trimmedPassword, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user, String message) {
                authState.setValue(AuthState.success(user, message));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(AuthState.error(errorMessage));
            }
        });
    }

    public void resetPassword(String email) {
        String trimmedEmail = email != null ? email.trim() : "";

        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            authState.setValue(AuthState.error("Địa chỉ Email không hợp lệ."));
            return;
        }

        authState.setValue(AuthState.loading());
        repository.resetPassword(trimmedEmail, new AuthRepository.ResetPasswordCallback() {
            @Override
            public void onSuccess(String message) {
                authState.setValue(AuthState.error(message));
            }

            @Override
            public void onError(String errorMessage) {
                authState.setValue(AuthState.error(errorMessage));
            }
        });
    }
}
