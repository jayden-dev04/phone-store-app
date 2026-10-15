package com.example.phonestore.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.MainActivity;
import com.example.phonestore.R;
import com.example.phonestore.adapter.BrandFilterAdapter;
import com.example.phonestore.adapter.CollectionAdapter;
import com.example.phonestore.adapter.SavedProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivitySavedBinding;
import com.example.phonestore.model.Brand;
import com.example.phonestore.model.Product;

import java.util.ArrayList;
import java.util.List;

public class SavedActivity extends AppCompatActivity implements SavedProductAdapter.OnSavedProductClickListener {

    private ActivitySavedBinding binding;
    private SavedProductAdapter savedProductAdapter;
    private BrandFilterAdapter brandFilterAdapter;
    private ArrayList<Product> fullProductList = new ArrayList<>();
    private String selectedBrandName = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String initialBrand = getIntent().getStringExtra("extra_brand");
        if (initialBrand != null && !initialBrand.trim().isEmpty()) {
            selectedBrandName = initialBrand.trim();
        }

        setupBrandFilters();
        setupCollections();
        setupSavedProductsGrid();
        setupFloatingBottomNavigation();
        loadSavedProductsFromFirebase();

        binding.btnAddCollection.setOnClickListener(v ->
                Toast.makeText(this, "Tạo bộ sưu tập mới (+)", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBrandFilters() {
        binding.rvBrandFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Brand> brandList = new ArrayList<>();
        brandList.add(new Brand("Tất cả", "", "Tất cả".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("Apple", "image/apple.png", "Apple".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("Samsung", "image/samsung.png", "Samsung".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("Xiaomi", "image/xiaomi.png", "Xiaomi".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("OPPO", "image/oppo.png", "OPPO".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("Honor", "image/honor.png", "Honor".equalsIgnoreCase(selectedBrandName)));
        brandList.add(new Brand("ZTE", "image/zte.png", "ZTE".equalsIgnoreCase(selectedBrandName)));

        brandFilterAdapter = new BrandFilterAdapter(brandList, brand -> {
            selectedBrandName = brand.getName();
            filterProductsByBrand(selectedBrandName);
        });

        binding.rvBrandFilters.setAdapter(brandFilterAdapter);
    }

    private void setupCollections() {
        binding.rvCollections.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<CollectionAdapter.CollectionItem> collections = new ArrayList<>();
        collections.add(new CollectionAdapter.CollectionItem("All Saved", android.R.drawable.ic_menu_agenda));
        collections.add(new CollectionAdapter.CollectionItem("Tech & Phones", android.R.drawable.ic_menu_compass));
        collections.add(new CollectionAdapter.CollectionItem("Favorites", android.R.drawable.btn_star));
        collections.add(new CollectionAdapter.CollectionItem("Summer Deals", android.R.drawable.ic_menu_today));

        CollectionAdapter collectionAdapter = new CollectionAdapter(collections);
        binding.rvCollections.setAdapter(collectionAdapter);
    }

    private void setupSavedProductsGrid() {
        binding.rvSavedProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvSavedProducts.setNestedScrollingEnabled(false);
        savedProductAdapter = new SavedProductAdapter(new ArrayList<>(), this);
        binding.rvSavedProducts.setAdapter(savedProductAdapter);
    }

    private void setupFloatingBottomNavigation() {
        // Biểu tượng Grid (Lưới/Saved) đang Active (Màu đen)
        updateBottomNavTint(binding.btnNavGrid);

        binding.btnNavHome.setOnClickListener(v -> {
            Intent intent = new Intent(SavedActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.btnNavSearch.setOnClickListener(v -> {
            Intent intent = new Intent(SavedActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.btnNavGrid.setOnClickListener(v -> {
            binding.nestedScrollViewSaved.smoothScrollTo(0, 0);
            Toast.makeText(this, "Bạn đang ở danh sách sản phẩm đã lưu", Toast.LENGTH_SHORT).show();
        });

        binding.btnNavBag.setOnClickListener(v ->
                Toast.makeText(this, "Giỏ hàng của bạn", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateBottomNavTint(ImageButton activeBtn) {
        int activeColor = Color.parseColor("#111111");
        int inactiveColor = Color.parseColor("#888888");

        binding.btnNavHome.setColorFilter(binding.btnNavHome == activeBtn ? activeColor : inactiveColor);
        binding.btnNavSearch.setColorFilter(binding.btnNavSearch == activeBtn ? activeColor : inactiveColor);
        binding.btnNavGrid.setColorFilter(binding.btnNavGrid == activeBtn ? activeColor : inactiveColor);
        binding.btnNavBag.setColorFilter(binding.btnNavBag == activeBtn ? activeColor : inactiveColor);
    }

    private void loadSavedProductsFromFirebase() {
        binding.progressBarSaved.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(this, new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                binding.progressBarSaved.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    fullProductList = productList;
                    filterProductsByBrand(selectedBrandName);
                } else {
                    Toast.makeText(SavedActivity.this, "Chưa có sản phẩm nào trên hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarSaved.setVisibility(View.GONE);
                Toast.makeText(SavedActivity.this, "Không thể nạp dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProductsByBrand(String brandName) {
        if (fullProductList == null || fullProductList.isEmpty()) return;

        if ("Tất cả".equalsIgnoreCase(brandName) || brandName == null || brandName.trim().isEmpty()) {
            savedProductAdapter.setProducts(fullProductList);
        } else {
            ArrayList<Product> filtered = new ArrayList<>();
            for (Product p : fullProductList) {
                if (p.getBrand() != null && p.getBrand().equalsIgnoreCase(brandName)) {
                    filtered.add(p);
                }
            }
            savedProductAdapter.setProducts(filtered);
            if (filtered.isEmpty()) {
                Toast.makeText(this, "Không có sản phẩm nào thuộc thương hiệu " + brandName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null) {
            Intent intent = new Intent(SavedActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        }
    }

    @Override
    public void onHeartClick(Product product, boolean isSaved) {
        if (product != null) {
            Toast.makeText(this, "Đã bỏ lưu: " + product.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
