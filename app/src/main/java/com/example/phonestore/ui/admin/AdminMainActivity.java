package com.example.phonestore.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.MainActivity;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivityAdminMainBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.utils.PreferenceManager;

import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PreferenceManager(this);

        setupAdminInfo();
        loadProductCount();
        setupClickListeners();
    }

    private void setupAdminInfo() {
        if (prefManager.isLoggedIn()) {
            binding.tvAdminName.setText(prefManager.getUserName());
            binding.tvAdminEmail.setText(prefManager.getUserEmail());
        }
    }

    private void loadProductCount() {
        FirebaseHelper.getAllProducts(this, new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                if (productList != null) {
                    binding.tvStatProducts.setText(String.valueOf(productList.size()));
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.tvStatProducts.setText("N/A");
            }
        });
    }

    private void setupClickListeners() {
        binding.toolbarAdmin.setNavigationOnClickListener(v -> finish());

        binding.cardManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminProductListActivity.class);
            startActivity(intent);
        });

        binding.cardManageOrders.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminOrderListActivity.class);
            startActivity(intent);
        });

        binding.cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AdminUserListActivity.class);
            startActivity(intent);
        });

        binding.btnGoToUserStore.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, MainActivity.class);
            startActivity(intent);
        });

        binding.btnAdminLogout.setOnClickListener(v -> {
            prefManager.clearSession();
            Toast.makeText(this, "Đã đăng xuất tài khoản Admin!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
