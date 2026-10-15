package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemAdminProductBinding;
import com.example.phonestore.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    public interface OnAdminProductClickListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    private List<Product> productList;
    private final OnAdminProductClickListener listener;

    public AdminProductAdapter(List<Product> productList, OnAdminProductClickListener listener) {
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProductBinding binding = ItemAdminProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    class AdminProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProductBinding binding;

        public AdminProductViewHolder(@NonNull ItemAdminProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvAdminProductName.setText(product.getName() != null ? product.getName() : "");
            binding.tvAdminProductBrand.setText(product.getBrand() != null ? product.getBrand() : "");

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            long minPrice = product.getMinPrice();
            binding.tvAdminProductPrice.setText(minPrice > 0 ? currencyFormat.format(minPrice) : "Liên hệ");

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgAdminProduct);
            } else {
                binding.imgAdminProduct.setImageResource(R.drawable.ic_phone_store);
            }

            binding.btnEditProduct.setOnClickListener(v -> {
                if (listener != null) listener.onEditProduct(product);
            });

            binding.btnDeleteProduct.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteProduct(product);
            });
        }
    }
}
