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
import com.example.phonestore.adapter.CollectionAdapter;
import com.example.phonestore.adapter.SavedProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivitySavedBinding;
import com.example.phonestore.model.Product;

import java.util.ArrayList;
import java.util.List;

public class SavedActivity extends AppCompatActivity implements SavedProductAdapter.OnSavedProductClickListener {

    private ActivitySavedBinding binding;
    private SavedProductAdapter savedProductAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupCollections();
        setupSavedProductsGrid();
        setupFloatingBottomNavigation();
        loadSavedProductsFromFirebase();

        binding.btnAddCollection.setOnClickListener(v ->
                Toast.makeText(this, "Tạo bộ sưu tập mới (+)", Toast.LENGTH_SHORT).show()
        );
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
                    savedProductAdapter.setProducts(productList);
                } else {
                    Toast.makeText(SavedActivity.this, "Chưa có sản phẩm nào được lưu.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarSaved.setVisibility(View.GONE);
                Toast.makeText(SavedActivity.this, "Không thể nạp dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
