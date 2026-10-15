package com.example.phonestore.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.BrandFilterAdapter;
import com.example.phonestore.adapter.CollectionAdapter;
import com.example.phonestore.adapter.SavedProductAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.FragmentSavedBinding;
import com.example.phonestore.model.Brand;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment implements SavedProductAdapter.OnSavedProductClickListener {

    private FragmentSavedBinding binding;
    private SavedProductAdapter savedProductAdapter;
    private BrandFilterAdapter brandFilterAdapter;
    private ArrayList<Product> fullProductList = new ArrayList<>();
    private String selectedBrandName = "Tất cả";

    public static SavedFragment newInstance(String initialBrand) {
        SavedFragment fragment = new SavedFragment();
        Bundle args = new Bundle();
        args.putString("extra_brand", initialBrand);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String initialBrand = getArguments().getString("extra_brand");
            if (initialBrand != null && !initialBrand.trim().isEmpty()) {
                selectedBrandName = initialBrand.trim();
            }
        }

        setupBrandFilters();
        setupCollections();
        setupSavedProductsGrid();
        loadSavedProductsFromFirebase();

        binding.btnAddCollection.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tạo bộ sưu tập mới (+)", Toast.LENGTH_SHORT).show()
        );
    }

    public void selectBrand(String brandName) {
        if (brandName != null && !brandName.trim().isEmpty()) {
            selectedBrandName = brandName.trim();
            if (brandFilterAdapter != null) {
                brandFilterAdapter.selectBrand(selectedBrandName);
            }
            filterProductsByBrand(selectedBrandName);
        }
    }

    private void setupBrandFilters() {
        binding.rvBrandFilters.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

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
        binding.rvCollections.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        List<CollectionAdapter.CollectionItem> collections = new ArrayList<>();
        collections.add(new CollectionAdapter.CollectionItem("All Saved", android.R.drawable.ic_menu_agenda));
        collections.add(new CollectionAdapter.CollectionItem("Tech & Phones", android.R.drawable.ic_menu_compass));
        collections.add(new CollectionAdapter.CollectionItem("Favorites", android.R.drawable.btn_star));
        collections.add(new CollectionAdapter.CollectionItem("Summer Deals", android.R.drawable.ic_menu_today));

        CollectionAdapter collectionAdapter = new CollectionAdapter(collections);
        binding.rvCollections.setAdapter(collectionAdapter);
    }

    private void setupSavedProductsGrid() {
        binding.rvSavedProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvSavedProducts.setNestedScrollingEnabled(false);
        savedProductAdapter = new SavedProductAdapter(new ArrayList<>(), this);
        binding.rvSavedProducts.setAdapter(savedProductAdapter);
    }

    private void loadSavedProductsFromFirebase() {
        binding.progressBarSaved.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(requireContext(), new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                if (!isAdded() || binding == null) return;
                binding.progressBarSaved.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    fullProductList = productList;
                    filterProductsByBrand(selectedBrandName);
                } else {
                    Toast.makeText(requireContext(), "Chưa có sản phẩm nào trên hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded() || binding == null) return;
                binding.progressBarSaved.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Không thể nạp dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProductsByBrand(String brandName) {
        if (fullProductList == null || fullProductList.isEmpty() || savedProductAdapter == null) return;

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
            if (filtered.isEmpty() && isAdded()) {
                Toast.makeText(requireContext(), "Không có sản phẩm nào thuộc thương hiệu " + brandName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProductClick(Product product) {
        if (product != null && isAdded()) {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        }
    }

    @Override
    public void onHeartClick(Product product, boolean isSaved) {
        if (product != null && isAdded()) {
            Toast.makeText(requireContext(), "Đã bỏ lưu: " + product.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
