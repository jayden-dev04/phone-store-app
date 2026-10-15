package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemProductBinding;
import com.example.phonestore.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> productList;
    private List<Product> productListFull;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList != null ? productList : new ArrayList<>();
        this.productListFull = new ArrayList<>(this.productList);
        this.listener = listener;
    }

    public void setProducts(List<Product> newProducts) {
        this.productList = newProducts != null ? newProducts : new ArrayList<>();
        this.productListFull = new ArrayList<>(this.productList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        productList.clear();
        if (query == null || query.trim().isEmpty()) {
            productList.addAll(productListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Product item : productListFull) {
                if ((item.getName() != null && item.getName().toLowerCase().contains(filterPattern)) ||
                    (item.getBrand() != null && item.getBrand().toLowerCase().contains(filterPattern))) {
                    productList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductBinding binding;

        public ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, OnProductClickListener listener) {
            if (product == null) return;

            binding.tvProductName.setText(product.getName() != null ? product.getName() : "");
            binding.tvProductBrand.setText(product.getBrand() != null ? product.getBrand() : "");

            long minPrice = product.getMinPrice();
            if (minPrice > 0) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                binding.tvProductPrice.setText(formatter.format(minPrice));
            } else {
                binding.tvProductPrice.setText("Liên hệ");
            }

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_phone_store);
            }

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
