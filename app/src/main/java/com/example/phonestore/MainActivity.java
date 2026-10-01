package com.example.phonestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.databinding.ActivityMainBinding;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PreferenceManager(this);

        // Kiểm tra xem đã đăng nhập chưa
        if (!prefManager.isLoggedIn()) {
            startLoginActivity();
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupUserInfo();
        setupListeners();
    }

    private void setupUserInfo() {
        String name = prefManager.getUserName();
        String email = prefManager.getUserEmail();

        if (!TextUtils.isEmpty(name)) {
            binding.tvUserName.setText(name);
        }
        if (!TextUtils.isEmpty(email)) {
            binding.tvUserEmail.setText(email);
        }
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(v -> {
            // Đăng xuất Firebase & Xóa Session SharedPreferences
            FirebaseAuth.getInstance().signOut();
            prefManager.clearSession();

            startLoginActivity();
            finish();
        });
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}