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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.phonestore.MainActivity;
import com.example.phonestore.R;
import com.example.phonestore.adapter.ProductHorizontalAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.FragmentHomeBinding;
import com.example.phonestore.databinding.ItemCategorySectionBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.ProductDetailActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupChips();
        loadProductsFromFirebase();
    }

    private void setupChips() {
        binding.chipDeals.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Ưu đãi hấp dẫn Deals", Toast.LENGTH_SHORT).show()
        );

        binding.chipFollowing.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Đang theo dõi Following", Toast.LENGTH_SHORT).show()
        );

        binding.chipSaved.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectTab(MainActivity.TAB_SAVED);
            }
        });
    }

    private void loadProductsFromFirebase() {
        binding.progressBarProducts.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(requireContext(), new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                if (!isAdded() || binding == null) return;
                binding.progressBarProducts.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    bindSectionsData(productList);
                    setupCategorySections(productList);
                } else {
                    Toast.makeText(requireContext(), "Chưa có sản phẩm nào trên hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded() || binding == null) return;
                binding.progressBarProducts.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Tải dữ liệu thất bại: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindSectionsData(ArrayList<Product> productList) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if (!productList.isEmpty()) {
            Product p1 = productList.get(0);
            binding.tvPriceJump1.setText(p1.getMinPrice() > 0 ? currencyFormat.format(p1.getMinPrice()) : "Liên hệ");
            if (p1.getImages() != null && !p1.getImages().isEmpty()) {
                Glide.with(this).load(p1.getImages().get(0)).into(binding.imgJump1);
            }
            binding.cardJump1.setOnClickListener(v -> openProductDetail(p1));
        }

        if (productList.size() > 1) {
            Product p2 = productList.get(1);
            binding.tvPriceJump2.setText(p2.getMinPrice() > 0 ? currencyFormat.format(p2.getMinPrice()) : "Liên hệ");
            if (p2.getImages() != null && !p2.getImages().isEmpty()) {
                Glide.with(this).load(p2.getImages().get(0)).into(binding.imgJump2);
            }
            binding.cardJump2.setOnClickListener(v -> openProductDetail(p2));
        }

        if (productList.size() > 2) {
            Product p3 = productList.get(2);
            binding.tvPriceJump3.setText(p3.getMinPrice() > 0 ? currencyFormat.format(p3.getMinPrice()) : "Liên hệ");
            if (p3.getImages() != null && !p3.getImages().isEmpty()) {
                Glide.with(this).load(p3.getImages().get(0)).into(binding.imgJump3);
            }
            binding.cardJump3.setOnClickListener(v -> openProductDetail(p3));
        }

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

    private void setupCategorySections(ArrayList<Product> productList) {
        binding.layoutCategorySections.removeAllViews();

        String[] targetBrands = {"Apple", "Samsung", "Xiaomi", "OPPO", "Honor", "ZTE"};

        for (String brand : targetBrands) {
            ArrayList<Product> filteredByBrand = new ArrayList<>();
            for (Product p : productList) {
                if (p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand)) {
                    filteredByBrand.add(p);
                }
            }

            if (filteredByBrand.isEmpty()) continue;

            int maxCount = Math.min(4, filteredByBrand.size());
            ArrayList<Product> top4Products = new ArrayList<>(filteredByBrand.subList(0, maxCount));

            ItemCategorySectionBinding sectionBinding = ItemCategorySectionBinding.inflate(
                    getLayoutInflater(), binding.layoutCategorySections, false);

            sectionBinding.tvSectionTitle.setText(brand);

            String logoAssetPath = "file:///android_asset/image/" + brand.toLowerCase() + ".png";
            Glide.with(this)
                    .load(logoAssetPath)
                    .placeholder(R.drawable.ic_phone_store)
                    .error(R.drawable.ic_phone_store)
                    .into(sectionBinding.imgSectionLogo);

            sectionBinding.btnSeeAll.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).openCategoryWithBrand(brand);
                }
            });

            sectionBinding.rvSectionProducts.setLayoutManager(
                    new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

            ProductHorizontalAdapter horizontalAdapter = new ProductHorizontalAdapter(top4Products, this::openProductDetail);
            sectionBinding.rvSectionProducts.setAdapter(horizontalAdapter);

            binding.layoutCategorySections.addView(sectionBinding.getRoot());
        }
    }

    private void openProductDetail(Product product) {
        if (product != null && isAdded()) {
            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT, product);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
