package com.example.phonestore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.phonestore.adapter.ProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivityMainBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.ui.ProductDetailActivity;
import com.example.phonestore.ui.SavedActivity;
import com.example.phonestore.utils.PreferenceManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

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
        updateBottomNavTint(binding.btnNavHome);

        binding.btnNavHome.setOnClickListener(v -> {
            updateBottomNavTint(binding.btnNavHome);
            binding.nestedScrollView.smoothScrollTo(0, 0);
            Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
        });

        binding.btnNavSearch.setOnClickListener(v -> {
            updateBottomNavTint(binding.btnNavSearch);
            binding.nestedScrollView.smoothScrollTo(0, binding.searchView.getTop());
            binding.searchView.requestFocus();
            Toast.makeText(this, "Tìm kiếm sản phẩm", Toast.LENGTH_SHORT).show();
        });

        binding.btnNavGrid.setOnClickListener(v -> {
            updateBottomNavTint(binding.btnNavGrid);
            Intent intent = new Intent(MainActivity.this, SavedActivity.class);
            startActivity(intent);
        });

        binding.btnNavBag.setOnClickListener(v -> {
            updateBottomNavTint(binding.btnNavBag);
            Toast.makeText(this, "Giỏ hàng của bạn", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateBottomNavTint(ImageButton activeBtn) {
        int activeColor = Color.parseColor("#111111");
        int inactiveColor = Color.parseColor("#888888");

        binding.btnNavHome.setColorFilter(binding.btnNavHome == activeBtn ? activeColor : inactiveColor);
        binding.btnNavSearch.setColorFilter(binding.btnNavSearch == activeBtn ? activeColor : inactiveColor);
        binding.btnNavGrid.setColorFilter(binding.btnNavGrid == activeBtn ? activeColor : inactiveColor);
        binding.btnNavBag.setColorFilter(binding.btnNavBag == activeBtn ? activeColor : inactiveColor);
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

        binding.chipSaved.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SavedActivity.class);
            startActivity(intent);
        });
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

        FirebaseHelper.getAllProducts(this, new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                binding.progressBarProducts.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    productAdapter.setProducts(productList);
                    bindSectionsData(productList);
                } else {
                    Toast.makeText(MainActivity.this, "Chưa có sản phẩm nào trên hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarProducts.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Tải dữ liệu thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindSectionsData(ArrayList<Product> productList) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Jump back in - Product 1
        if (!productList.isEmpty()) {
            Product p1 = productList.get(0);
            binding.tvPriceJump1.setText(p1.getMinPrice() > 0 ? currencyFormat.format(p1.getMinPrice()) : "Liên hệ");
            if (p1.getImages() != null && !p1.getImages().isEmpty()) {
                Glide.with(this).load(p1.getImages().get(0)).into(binding.imgJump1);
            }
            binding.cardJump1.setOnClickListener(v -> openProductDetail(p1));
        }

        // Jump back in - Product 2
        if (productList.size() > 1) {
            Product p2 = productList.get(1);
            binding.tvPriceJump2.setText(p2.getMinPrice() > 0 ? currencyFormat.format(p2.getMinPrice()) : "Liên hệ");
            if (p2.getImages() != null && !p2.getImages().isEmpty()) {
                Glide.with(this).load(p2.getImages().get(0)).into(binding.imgJump2);
            }
            binding.cardJump2.setOnClickListener(v -> openProductDetail(p2));
        }

        // Jump back in - Product 3
        if (productList.size() > 2) {
            Product p3 = productList.get(2);
            binding.tvPriceJump3.setText(p3.getMinPrice() > 0 ? currencyFormat.format(p3.getMinPrice()) : "Liên hệ");
            if (p3.getImages() != null && !p3.getImages().isEmpty()) {
                Glide.with(this).load(p3.getImages().get(0)).into(binding.imgJump3);
            }
            binding.cardJump3.setOnClickListener(v -> openProductDetail(p3));
        }

        // Recently Viewed Card - Featured Product
        if (!productList.isEmpty()) {
            Product featured = productList.get(0);
            String brand = featured.getBrand() != null ? featured.getBrand() : "Brand";
            binding.tvRecentlyBrandLogo.setText(brand.substring(0, 1).toUpperCase());
            binding.tvRecentlyBrandName.setText(brand);
            binding.tvRecentlyProductName.setText(featured.getName() != null ? featured.getName() : "");
            binding.tvRecentlyProductPrice.setText(featured.getMinPrice() > 0 ? currencyFormat.format(featured.getMinPrice()) : "Liên hệ");

            if (featured.getImages() != null && !featured.getImages().isEmpty()) {
                Glide.with(this).load(featured.getImages().get(0)).into(binding.imgRecentlyProduct);
            }
            binding.cardRecentlyViewed.setOnClickListener(v -> openProductDetail(featured));
        }
    }

    private void openProductDetail(Product product) {
        if (product != null) {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        }
    }

    @Override
    public void onProductClick(Product product) {
        openProductDetail(product);
    }
}
