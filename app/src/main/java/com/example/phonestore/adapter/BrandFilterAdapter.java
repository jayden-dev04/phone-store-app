package com.example.phonestore.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemBrandFilterBinding;
import com.example.phonestore.model.Brand;

import java.util.List;

public class BrandFilterAdapter extends RecyclerView.Adapter<BrandFilterAdapter.BrandViewHolder> {

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
    }

    private final List<Brand> brandList;
    private final OnBrandClickListener listener;

    public BrandFilterAdapter(List<Brand> brandList, OnBrandClickListener listener) {
        this.brandList = brandList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBrandFilterBinding binding = ItemBrandFilterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BrandViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        holder.bind(brandList.get(position));
    }

    @Override
    public int getItemCount() {
        return brandList != null ? brandList.size() : 0;
    }

    public void selectBrand(String brandName) {
        if (brandList == null) return;
        for (Brand b : brandList) {
            b.setSelected(b.getName().equalsIgnoreCase(brandName));
        }
        notifyDataSetChanged();
    }

    class BrandViewHolder extends RecyclerView.ViewHolder {
        private final ItemBrandFilterBinding binding;

        public BrandViewHolder(@NonNull ItemBrandFilterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Brand brand) {
            binding.tvBrandName.setText(brand.getName());

            if (brand.getLogoAssetPath() != null && !brand.getLogoAssetPath().isEmpty()) {
                binding.imgBrandLogo.setVisibility(View.VISIBLE);
                String assetUrl = "file:///android_asset/" + brand.getLogoAssetPath();
                Glide.with(itemView.getContext())
                        .load(assetUrl)
                        .placeholder(R.drawable.ic_phone_store)
                        .error(R.drawable.ic_phone_store)
                        .into(binding.imgBrandLogo);
            } else {
                binding.imgBrandLogo.setVisibility(View.GONE);
            }

            if (brand.isSelected()) {
                binding.cardBrandFilter.setCardBackgroundColor(Color.parseColor("#111111"));
                binding.cardBrandFilter.setStrokeColor(Color.parseColor("#111111"));
                binding.tvBrandName.setTextColor(Color.WHITE);
            } else {
                binding.cardBrandFilter.setCardBackgroundColor(Color.WHITE);
                binding.cardBrandFilter.setStrokeColor(Color.parseColor("#DDDDDD"));
                binding.tvBrandName.setTextColor(Color.parseColor("#111111"));
            }

            itemView.setOnClickListener(v -> {
                for (Brand b : brandList) {
                    b.setSelected(b == brand);
                }
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onBrandClick(brand);
                }
            });
        }
    }
}
