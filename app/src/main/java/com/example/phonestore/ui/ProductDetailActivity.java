package com.example.phonestore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.phonestore.R;
import com.example.phonestore.adapter.ProductHorizontalAdapter;
import com.example.phonestore.data.CartRepository;
import com.example.phonestore.data.FirebaseHelper;
import com.example.phonestore.databinding.ActivityProductDetailBinding;
import com.example.phonestore.model.CartItem;
import com.example.phonestore.model.Product;
import com.example.phonestore.model.Variant;
import com.example.phonestore.utils.PreferenceManager;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
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

        PreferenceManager prefManager = new PreferenceManager(this);

        binding.btnAddToCart.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Yêu cầu đăng nhập")
                        .setMessage("Bạn cần đăng nhập tài khoản để thêm sản phẩm vào giỏ hàng.")
                        .setPositiveButton("Đăng nhập ngay", (dialog, which) -> {
                            Intent intent = new Intent(ProductDetailActivity.this, LoginActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Đóng", (dialog, which) -> dialog.dismiss())
                        .show();
                return;
            }

            String capacity = selectedVariant != null ? selectedVariant.getCapacity() : "Tiêu chuẩn";
            long price = (selectedVariant != null && selectedVariant.getPrice() > 0) ? selectedVariant.getPrice() : product.getMinPrice();
            String imageUrl = (product.getImages() != null && !product.getImages().isEmpty()) ? product.getImages().get(0) : "";
            String cartItemId = product.getId() + "_" + capacity.replaceAll("\\s+", "");

            CartItem cartItem = new CartItem(
                    cartItemId,
                    product.getId(),
                    product.getName(),
                    imageUrl,
                    product.getBrand(),
                    capacity,
                    price,
                    1
            );

            binding.btnAddToCart.setEnabled(false);
            CartRepository.addToCart(prefManager.getUserEmail(), cartItem, new CartRepository.CartCallback() {
                @Override
                public void onSuccess(String message) {
                    binding.btnAddToCart.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String errorMessage) {
                    binding.btnAddToCart.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

        setupRelatedProducts(product);
    }

    private void setupRelatedProducts(Product currentProduct) {
        if (currentProduct == null) return;

        binding.rvRelatedProducts.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        FirebaseHelper.getAllProducts(this, new FirebaseHelper.OnProductsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Product> productList) {
                if (productList == null || productList.isEmpty()) {
                    hideRelatedProducts();
                    return;
                }

                ArrayList<Product> relatedList = new ArrayList<>();
                String currentBrand = currentProduct.getBrand();
                String currentId = currentProduct.getId();

                // 1. Filter products of same brand (excluding current product)
                for (Product p : productList) {
                    boolean isSameBrand = currentBrand != null && currentBrand.equalsIgnoreCase(p.getBrand());
                    boolean isDifferentProduct = currentId == null || !currentId.equalsIgnoreCase(p.getId());

                    if (isSameBrand && isDifferentProduct) {
                        relatedList.add(p);
                    }
                }

                // 2. Fallback: If not enough products from same brand, add other products (excluding current product)
                if (relatedList.size() < 4) {
                    for (Product p : productList) {
                        boolean isDifferentProduct = currentId == null || !currentId.equalsIgnoreCase(p.getId());
                        if (isDifferentProduct && !relatedList.contains(p)) {
                            relatedList.add(p);
                        }
                        if (relatedList.size() >= 4) break;
                    }
                }

                // 3. Limit to max 4 items
                int maxCount = Math.min(4, relatedList.size());
                if (maxCount > 0) {
                    ArrayList<Product> top4Related = new ArrayList<>(relatedList.subList(0, maxCount));
                    ProductHorizontalAdapter adapter = new ProductHorizontalAdapter(top4Related, relatedProduct -> {
                        Intent intent = new Intent(ProductDetailActivity.this, ProductDetailActivity.class);
                        intent.putExtra(EXTRA_PRODUCT, relatedProduct);
                        startActivity(intent);
                    });
                    binding.rvRelatedProducts.setAdapter(adapter);
                    binding.dividerRelated.setVisibility(View.VISIBLE);
                    binding.tvRelatedTitle.setVisibility(View.VISIBLE);
                    binding.rvRelatedProducts.setVisibility(View.VISIBLE);
                } else {
                    hideRelatedProducts();
                }
            }

            @Override
            public void onError(String errorMessage) {
                hideRelatedProducts();
            }
        });
    }

    private void hideRelatedProducts() {
        binding.dividerRelated.setVisibility(View.GONE);
        binding.tvRelatedTitle.setVisibility(View.GONE);
        binding.rvRelatedProducts.setVisibility(View.GONE);
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
