package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemProductHorizontalBinding;
import com.example.phonestore.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductHorizontalAdapter extends RecyclerView.Adapter<ProductHorizontalAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> productList;
    private final OnProductClickListener listener;

    public ProductHorizontalAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductHorizontalBinding binding = ItemProductHorizontalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductHorizontalBinding binding;

        public ProductViewHolder(@NonNull ItemProductHorizontalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvProductName.setText(product.getName() != null ? product.getName() : "");
            binding.tvBrand.setText(product.getBrand() != null ? product.getBrand() : "");

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            long minPrice = product.getMinPrice();
            binding.tvProductPrice.setText(minPrice > 0 ? currencyFormat.format(minPrice) : "Liên hệ");

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_phone_store);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
