package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemSavedProductBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.model.Variant;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavedProductAdapter extends RecyclerView.Adapter<SavedProductAdapter.SavedViewHolder> {

    public interface OnSavedProductClickListener {
        void onProductClick(Product product);
        void onHeartClick(Product product, boolean isSaved);
    }

    private List<Product> productList;
    private final OnSavedProductClickListener listener;

    public SavedProductAdapter(List<Product> productList, OnSavedProductClickListener listener) {
        this.productList = productList != null ? productList : new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> newProducts) {
        this.productList = newProducts != null ? newProducts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSavedProductBinding binding = ItemSavedProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SavedViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener, position);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class SavedViewHolder extends RecyclerView.ViewHolder {
        private final ItemSavedProductBinding binding;

        public SavedViewHolder(ItemSavedProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product, OnSavedProductClickListener listener, int position) {
            if (product == null) return;

            binding.tvName.setText(product.getName() != null ? product.getName() : "");
            binding.tvBrand.setText(product.getBrand() != null ? product.getBrand() : "");

            long minPrice = product.getMinPrice();
            if (minPrice > 0) {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                binding.tvPrice.setText(formatter.format(minPrice));
            } else {
                binding.tvPrice.setText("Liên hệ");
            }

            // Highlighting capacity or variant info
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                Variant firstVar = product.getVariants().values().iterator().next();
                binding.tvCapacity.setText(firstVar.getCapacity() + " • Hàng chính hãng");
            } else {
                binding.tvCapacity.setText("Hàng chính hãng");
            }

            // Discount Badge
            String badgeText = (position % 2 == 0) ? "Save $30" : "Giảm 10%";
            binding.tvDiscountBadge.setText(badgeText);

            if (product.getImages() != null && !product.getImages().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(product.getImages().get(0))
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.ic_phone_store);
            }

            binding.btnHeartCircle.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeartClick(product, true);
                }
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
