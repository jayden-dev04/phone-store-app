package com.example.phonestore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.phonestore.MainActivity;
import com.example.phonestore.R;
import com.example.phonestore.data.AuthState;
import com.example.phonestore.databinding.ActivityLoginBinding;
import com.example.phonestore.model.User;
import com.example.phonestore.ui.admin.AdminMainActivity;
import com.example.phonestore.utils.PreferenceManager;
import com.example.phonestore.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PreferenceManager(this);

        if (prefManager.isLoggedIn()) {
            startNextActivityBasedOnRole(prefManager.getUserRole());
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        setupSavedCredentials();
        setupListeners();
        observeViewModel();
    }

    private void setupSavedCredentials() {
        if (prefManager.isRememberMeEnabled()) {
            binding.etAccount.setText(prefManager.getSavedAccount());
            binding.etPassword.setText(prefManager.getSavedPassword());
        }
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String account = binding.etAccount.getText() != null ? binding.etAccount.getText().toString().trim() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

            prefManager.saveRememberMe(true, account, password);
            viewModel.login(account, password);
        });

        binding.btnCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
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
                    int role = (user != null) ? user.getRole() : User.ROLE_USER;
                    if (user != null) {
                        prefManager.saveUserSession(true, user.getEmail(), user.getFullName(), role);
                    }
                    String successMsg = authState.getMessage() != null ? authState.getMessage() : getString(R.string.msg_login_success);
                    Toast.makeText(this, successMsg, Toast.LENGTH_SHORT).show();
                    startNextActivityBasedOnRole(role);
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
        binding.progressBarLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.etAccount.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_reset_meta);
        builder.setMessage(R.string.desc_reset_meta);

        final EditText input = new EditText(this);
        input.setHint(R.string.hint_account_meta);
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);

        builder.setPositiveButton(R.string.btn_continue_meta, (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                viewModel.resetPassword(email);
            } else {
                Toast.makeText(this, R.string.err_empty_fields, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.btn_cancel_meta, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void startNextActivityBasedOnRole(int role) {
        Intent intent;
        if (role == User.ROLE_ADMIN) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        startActivity(intent);
    }

    private void startMainActivity() {
        startNextActivityBasedOnRole(User.ROLE_USER);
    }
}