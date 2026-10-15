package com.example.phonestore.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.databinding.ActivityProductDetailBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.model.Variant;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    private ActivityProductDetailBinding binding;
    private Variant selectedVariant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarDetail.setNavigationOnClickListener(v -> finish());

        Product product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        if (product != null) {
            displayProductDetail(product);
        } else {
            Toast.makeText(this, "Không thể tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductDetail(Product product) {
        binding.tvDetailName.setText(product.getName() != null ? product.getName() : "");
        binding.tvDetailBrand.setText(product.getBrand() != null ? product.getBrand() : "");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        long minPrice = product.getMinPrice();
        binding.tvDetailPrice.setText(minPrice > 0 ? currencyFormat.format(minPrice) : "Liên hệ");

        // Mô tả sản phẩm
        StringBuilder descriptionBuilder = new StringBuilder();
        if (product.getShort_description() != null && !product.getShort_description().trim().isEmpty()) {
            descriptionBuilder.append(product.getShort_description().trim());
        }
        if (product.getDetail_description() != null && !product.getDetail_description().trim().isEmpty()) {
            if (descriptionBuilder.length() > 0) {
                descriptionBuilder.append("\n\n");
            }
            descriptionBuilder.append(product.getDetail_description().trim());
        }
        binding.tvDetailDescription.setText(descriptionBuilder.length() > 0 ? descriptionBuilder.toString() : "Chưa có thông tin mô tả.");

        // Danh sách hình ảnh
        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            Glide.with(this)
                    .load(images.get(0))
                    .placeholder(R.drawable.ic_phone_store)
                    .error(R.drawable.ic_phone_store)
                    .into(binding.imgDetailProduct);

            setupThumbnails(images);
        } else {
            binding.imgDetailProduct.setImageResource(R.drawable.ic_phone_store);
            binding.scrollViewThumbnails.setVisibility(View.GONE);
        }

        // Danh sách biến thể (Variants)
        Map<String, Variant> variantsMap = product.getVariants();
        if (variantsMap != null && !variantsMap.isEmpty()) {
            setupVariants(variantsMap, currencyFormat);
        } else {
            binding.tvVariantTitle.setVisibility(View.GONE);
            binding.scrollViewVariants.setVisibility(View.GONE);
        }

        binding.btnAddToCart.setOnClickListener(v -> {
            String variantInfo = selectedVariant != null ? " (" + selectedVariant.getCapacity() + ")" : "";
            Toast.makeText(this, "Đã thêm " + product.getName() + variantInfo + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupThumbnails(List<String> images) {
        binding.layoutThumbnails.removeAllViews();
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        for (String url : images) {
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, margin, 0);
            card.setLayoutParams(params);
            card.setRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()));
            card.setStrokeWidth(0);

            android.widget.ImageView img = new android.widget.ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            img.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
            img.setPadding(6, 6, 6, 6);

            Glide.with(this).load(url).placeholder(R.drawable.ic_phone_store).into(img);
            card.addView(img);

            card.setOnClickListener(v -> Glide.with(this).load(url).into(binding.imgDetailProduct));

            binding.layoutThumbnails.addView(card);
        }
    }

    private void setupVariants(Map<String, Variant> variantsMap, NumberFormat currencyFormat) {
        binding.layoutVariants.removeAllViews();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());

        final TextView[] selectedChip = {null};

        for (Map.Entry<String, Variant> entry : variantsMap.entrySet()) {
            String capacity = entry.getKey();
            Variant variant = entry.getValue();

            TextView chip = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics())
            );
            params.setMargins(0, 0, margin, 0);
            chip.setLayoutParams(params);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(32, 0, 32, 0);
            chip.setText(capacity);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            updateChipStyle(chip, false);

            chip.setOnClickListener(v -> {
                if (selectedChip[0] != null) {
                    updateChipStyle(selectedChip[0], false);
                }
                updateChipStyle(chip, true);
                selectedChip[0] = chip;
                selectedVariant = variant;

                if (variant != null && variant.getPrice() > 0) {
                    binding.tvDetailPrice.setText(currencyFormat.format(variant.getPrice()));
                }
            });

            binding.layoutVariants.addView(chip);

            if (selectedChip[0] == null) {
                updateChipStyle(chip, true);
                selectedChip[0] = chip;
                selectedVariant = variant;
                if (variant != null && variant.getPrice() > 0) {
                    binding.tvDetailPrice.setText(currencyFormat.format(variant.getPrice()));
                }
            }
        }
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_button_primary);
            chip.setTextColor(getResources().getColor(R.color.white, getTheme()));
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_white);
            chip.setTextColor(getResources().getColor(R.color.text_dark, getTheme()));
        }
    }
}
