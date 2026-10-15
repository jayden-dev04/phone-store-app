package com.example.phonestore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemCartBinding;
import com.example.phonestore.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangeListener {
        void onSelectionChanged(CartItem item, boolean isSelected);
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemDeleted(CartItem item);
    }

    private List<CartItem> cartItems;
    private final OnCartItemChangeListener listener;

    public CartAdapter(List<CartItem> cartItems, OnCartItemChangeListener listener) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem item) {
            binding.tvCartName.setText(item.getProductName() != null ? item.getProductName() : "");
            binding.tvCartCapacity.setText(item.getCapacity() != null ? item.getCapacity() : "Tiêu chuẩn");

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvCartPrice.setText(currencyFormat.format(item.getPrice()));

            binding.tvCartQuantity.setText(String.valueOf(item.getQuantity()));
            binding.cbCartSelect.setChecked(item.isSelected());

            if (item.getProductImage() != null && !item.getProductImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProductImage())
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgCartProduct);
            } else {
                binding.imgCartProduct.setImageResource(R.drawable.ic_phone_store);
            }

            binding.cbCartSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (listener != null) {
                    listener.onSelectionChanged(item, isChecked);
                }
            });

            binding.btnCartMinus.setOnClickListener(v -> {
                int newQty = item.getQuantity() - 1;
                if (listener != null) {
                    listener.onQuantityChanged(item, newQty);
                }
            });

            binding.btnCartPlus.setOnClickListener(v -> {
                int newQty = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(item, newQty);
                }
            });

            binding.btnDeleteCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDeleted(item);
                }
            });
        }
    }
}
