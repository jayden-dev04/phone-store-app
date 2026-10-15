package com.example.phonestore.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.AdminProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivityAdminProductListBinding;
import com.example.phonestore.model.Product;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminProductListActivity extends AppCompatActivity implements AdminProductAdapter.OnAdminProductClickListener {

    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";
    private ActivityAdminProductListBinding binding;
    private AdminProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarAdminProducts.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();

        binding.fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductListActivity.this, AdminEditProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductsFromFirebase();
    }

    private void setupRecyclerView() {
        binding.rvAdminProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProductAdapter(new ArrayList<>(), this);
        binding.rvAdminProducts.setAdapter(adapter);
    }

    private void loadProductsFromFirebase() {
        binding.progressBarAdminProducts.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(this, new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                binding.progressBarAdminProducts.setVisibility(View.GONE);
                if (productList != null) {
                    adapter.setProducts(productList);
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarAdminProducts.setVisibility(View.GONE);
                Toast.makeText(AdminProductListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditProduct(Product product) {
        if (product == null) return;
        Intent intent = new Intent(AdminProductListActivity.this, AdminEditProductActivity.class);
        intent.putExtra(AdminEditProductActivity.EXTRA_PRODUCT, product);
        startActivity(intent);
    }

    @Override
    public void onDeleteProduct(Product product) {
        if (product == null || product.getId() == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + product.getName() + "\" khỏi hệ thống Firebase không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    FirebaseDatabase.getInstance(DB_URL)
                            .getReference("Products")
                            .child(product.getId())
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminProductListActivity.this, "Đã xóa sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                                loadProductsFromFirebase();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(AdminProductListActivity.this, "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
