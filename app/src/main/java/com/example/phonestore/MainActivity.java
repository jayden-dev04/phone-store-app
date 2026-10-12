package com.example.phonestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.phonestore.adapter.ProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivityMainBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.ui.ProductDetailActivity;
import com.example.phonestore.utils.PreferenceManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ActivityMainBinding binding;
    private ProductAdapter productAdapter;
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PreferenceManager(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupRecyclerView();
        setupSearchView();
        setupFloatingBottomNavigation();
        setupChipsAndHeader();
        loadProductsFromFirebase();
    }

    private void setupRecyclerView() {
        binding.rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProducts.setNestedScrollingEnabled(false);
        productAdapter = new ProductAdapter(new ArrayList<>(), this);
        binding.rvProducts.setAdapter(productAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (productAdapter != null) {
                    productAdapter.filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (productAdapter != null) {
                    productAdapter.filter(newText);
                }
                return true;
            }
        });
    }

    private void setupFloatingBottomNavigation() {
        binding.btnNavHome.setOnClickListener(v -> {
            binding.nestedScrollView.smoothScrollTo(0, 0);
            Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
        });

        binding.btnNavSearch.setOnClickListener(v -> {
            binding.searchView.requestFocus();
            Toast.makeText(this, "Tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
        });

        binding.btnNavGrid.setOnClickListener(v ->
            Toast.makeText(this, "Danh mục sản phẩm", Toast.LENGTH_SHORT).show()
        );

        binding.btnNavBag.setOnClickListener(v ->
            Toast.makeText(this, "Giỏ hàng của bạn", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupChipsAndHeader() {
        View.OnClickListener accountClickListener = v -> handleAccountClick();
        binding.cardAvatar.setOnClickListener(accountClickListener);
        binding.btnAvatar.setOnClickListener(accountClickListener);
        binding.btnNotification.setOnClickListener(v ->
            Toast.makeText(this, "Thông báo của bạn", Toast.LENGTH_SHORT).show()
        );

        binding.chipDeals.setOnClickListener(v ->
            Toast.makeText(this, "Ưu đãi hấp dẫn Deals", Toast.LENGTH_SHORT).show()
        );

        binding.chipFollowing.setOnClickListener(v ->
            Toast.makeText(this, "Đang theo dõi Following", Toast.LENGTH_SHORT).show()
        );

        binding.chipSaved.setOnClickListener(v ->
            Toast.makeText(this, "Sản phẩm đã lưu Saved", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleAccountClick() {
        if (prefManager.isLoggedIn()) {
            Toast.makeText(this, "Xin chào " + prefManager.getUserName() + " (" + prefManager.getUserEmail() + ")", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void loadProductsFromFirebase() {
        binding.progressBarProducts.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                binding.progressBarProducts.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    productAdapter.setProducts(productList);
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarProducts.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null) {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        }
    }
}
