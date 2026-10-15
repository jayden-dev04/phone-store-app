package com.example.phonestore.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.phonestore.adapter.ProductAdapter;
import com.example.phonestore.adapter.ProductHorizontalAdapter;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.FragmentSearchBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.ui.ProductDetailActivity;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private FragmentSearchBinding binding;
    private ProductAdapter productAdapter;
    private ProductHorizontalAdapter suggestionAdapter;
    private ArrayList<Product> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchView();
        loadProductsFromFirebase();
    }

    private void setupRecyclerView() {
        binding.rvSearchProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productAdapter = new ProductAdapter(new ArrayList<>(), this);
        binding.rvSearchProducts.setAdapter(productAdapter);

        binding.rvSearchSuggestions.setLayoutManager(
                new androidx.recyclerview.widget.LinearLayoutManager(requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        suggestionAdapter = new ProductHorizontalAdapter(new ArrayList<>(), this::onProductClick);
        binding.rvSearchSuggestions.setAdapter(suggestionAdapter);
    }

    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                handleFilter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                handleFilter(newText);
                return true;
            }
        });
    }

    private void handleFilter(String query) {
        if (productAdapter != null) {
            productAdapter.filter(query);
        }

        if (query != null && !query.trim().isEmpty() && allProducts != null && !allProducts.isEmpty()) {
            String lowerQuery = query.toLowerCase().trim();
            ArrayList<Product> matched = new ArrayList<>();
            for (Product p : allProducts) {
                boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(lowerQuery);
                boolean matchBrand = p.getBrand() != null && p.getBrand().toLowerCase().contains(lowerQuery);
                if (matchName || matchBrand) {
                    matched.add(p);
                }
            }

            if (!matched.isEmpty()) {
                int maxCount = Math.min(5, matched.size());
                ArrayList<Product> top5Suggestions = new ArrayList<>(matched.subList(0, maxCount));
                suggestionAdapter.setProducts(top5Suggestions);
                binding.layoutSuggestions.setVisibility(View.VISIBLE);
                binding.tvSearchResultCount.setText("Tìm thấy " + matched.size() + " kết quả cho \"" + query + "\"");
            } else {
                binding.layoutSuggestions.setVisibility(View.GONE);
                binding.tvSearchResultCount.setText("Không tìm thấy sản phẩm phù hợp.");
            }
        } else {
            binding.layoutSuggestions.setVisibility(View.GONE);
            binding.tvSearchResultCount.setText("Tất cả sản phẩm (" + (allProducts != null ? allProducts.size() : 0) + ")");
        }
    }

    private void loadProductsFromFirebase() {
        binding.progressBarSearch.setVisibility(View.VISIBLE);

        FirebaseHelper.getAllProducts(requireContext(), new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                if (!isAdded() || binding == null) return;
                binding.progressBarSearch.setVisibility(View.GONE);
                if (productList != null && !productList.isEmpty()) {
                    allProducts = productList;
                    productAdapter.setProducts(productList);
                    binding.tvSearchResultCount.setText("Tất cả sản phẩm (" + productList.size() + ")");
                } else {
                    binding.tvSearchResultCount.setText("Chưa có sản phẩm nào.");
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded() || binding == null) return;
                binding.progressBarSearch.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
