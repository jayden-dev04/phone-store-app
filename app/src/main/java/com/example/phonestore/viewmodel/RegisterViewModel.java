package com.example.phonestore.viewmodel;

import android.text.TextUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.phonestore.data.AuthRepository;
import com.example.phonestore.data.AuthState;
import com.example.phonestore.model.User;
import com.example.phonestore.utils.ValidationUtils;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.idle());

    public RegisterViewModel() { this.repository = new AuthRepository(); }

    public LiveData<AuthState> getAuthState() { return authState; }

    public void register(String fullName, String email, String phone, String password, String confirmPassword) {
        String trimmedName = fullName != null ? fullName.trim() : "";
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedPhone = phone != null ? phone.trim() : "";
        String trimmedPassword = password != null ? password.trim() : "";
        String trimmedConfirmPassword = confirmPassword != null ? confirmPassword.trim() : "";

        if (TextUtils.isEmpty(trimmedName) || TextUtils.isEmpty(trimmedEmail) || TextUtils.isEmpty(trimmedPhone) || TextUtils.isEmpty(trimmedPassword)) {
            authState.setValue(AuthState.error("Vui lòng điền đầy đủ tất cả các trường."));
            return;
        }

        if (!ValidationUtils.isValidEmail(trimmedEmail)) {
            authState.setValue(AuthState.error("Địa chỉ Email không hợp lệ."));
            return;
        }

        if (!ValidationUtils.isValidPhone(trimmedPhone)) {
            authState.setValue(AuthState.error("Số điện thoại phải gồm 10 chữ số bắt đầu bằng 0."));
            return;
        }

        if (!ValidationUtils.isValidPassword(trimmedPassword)) {
            authState.setValue(AuthState.error("Mật khẩu phải chứa ít nhất 6 ký tự."));
            return;
        }

        if (!ValidationUtils.isPasswordMatching(trimmedPassword, trimmedConfirmPassword)) {
            authState.setValue(AuthState.error("Mật khẩu nhập lại không trùng khớp."));
            return;
        }

        authState.setValue(AuthState.loading());
        repository.register(trimmedName, trimmedEmail, trimmedPhone, trimmedPassword, new AuthRepository.AuthCallback() {
            @Override public void onSuccess(User user, String message) { authState.setValue(AuthState.success(user, message)); }
            @Override public void onError(String errorMessage) { authState.setValue(AuthState.error(errorMessage)); }
        });
    }
}