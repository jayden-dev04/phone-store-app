package com.example.phonestore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.phonestore.MainActivity;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ActivityRegisterBinding;
import com.example.phonestore.model.User;
import com.example.phonestore.utils.PreferenceManager;
import com.example.phonestore.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        prefManager = new PreferenceManager(this);

        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String fullName = binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
            String phone = binding.etPhone.getText() != null ? binding.etPhone.getText().toString().trim() : "";
            String password = binding.etRegPassword.getText() != null ? binding.etRegPassword.getText().toString().trim() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

            viewModel.register(fullName, email, phone, password, confirmPassword);
        });

        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getAuthState().observe(this, authState -> {
            if (authState == null) return;

            switch (authState.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    User user = authState.getUser();
                    if (user != null) {
                        prefManager.saveUserSession(true, user.getEmail(), user.getFullName());
                    }
                    Toast.makeText(this, authState.getMessage() != null ? authState.getMessage() : getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    break;

                case ERROR:
                    showLoading(false);
                    if (!TextUtils.isEmpty(authState.getMessage())) {
                        Toast.makeText(this, authState.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    break;

                case IDLE:
                default:
                    showLoading(false);
                    break;
            }
        });
    }

    private void showLoading(boolean isLoading) {
        binding.progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.etFullName.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPhone.setEnabled(!isLoading);
        binding.etRegPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
    }
}