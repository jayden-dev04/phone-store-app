package com.example.phonestore.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.R;
import com.example.phonestore.databinding.ActivityAdminEditProductBinding;
import com.example.phonestore.model.Product;
import com.example.phonestore.model.Variant;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";
    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";

    private ActivityAdminEditProductBinding binding;
    private final String[] brands = {"Apple", "Samsung", "Xiaomi", "OPPO", "Honor", "ZTE", "Vivo", "Nubia", "Poco"};
    private final Map<String, Variant> variantMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarEditProduct.setNavigationOnClickListener(v -> finish());

        setupSpinner();

        Product product = (Product) getIntent().getSerializableExtra(EXTRA_PRODUCT);
        if (product != null) {
            populateProductData(product);
        } else {
            binding.etEditProductId.setText("prod_" + System.currentTimeMillis());
        }

        setupVariantAdder();
        binding.btnSaveProduct.setOnClickListener(v -> saveProductToFirebase());
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, brands);
        binding.spEditProductBrand.setAdapter(adapter);
    }

    private void populateProductData(Product product) {
        binding.etEditProductId.setText(product.getId());
        binding.etEditProductId.setEnabled(false);
        binding.etEditProductName.setText(product.getName());
        binding.etEditShortDesc.setText(product.getShort_description());
        binding.etEditDetailDesc.setText(product.getDetail_description());

        // Set brand spinner
        if (product.getBrand() != null) {
            for (int i = 0; i < brands.length; i++) {
                if (brands[i].equalsIgnoreCase(product.getBrand())) {
                    binding.spEditProductBrand.setSelection(i);
                    break;
                }
            }
        }

        // Set images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < product.getImages().size(); i++) {
                sb.append(product.getImages().get(i));
                if (i < product.getImages().size() - 1) sb.append(", ");
            }
            binding.etEditProductImages.setText(sb.toString());
        }

        // Set variants
        if (product.getVariants() != null) {
            variantMap.putAll(product.getVariants());
            renderVariantsList();
        }
    }

    private void setupVariantAdder() {
        binding.btnAddVariant.setOnClickListener(v -> {
            String cap = binding.etVariantCap.getText() != null ? binding.etVariantCap.getText().toString().trim() : "";
            String priceStr = binding.etVariantPrice.getText() != null ? binding.etVariantPrice.getText().toString().trim() : "";

            if (TextUtils.isEmpty(cap) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(this, "Vui lòng nhập dung lượng và giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                long price = Long.parseLong(priceStr);
                Variant variant = new Variant(cap, price, 100);
                variantMap.put(cap, variant);
                binding.etVariantCap.setText("");
                binding.etVariantPrice.setText("");
                renderVariantsList();
            } catch (Exception e) {
                Toast.makeText(this, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderVariantsList() {
        binding.layoutVariantsContainer.removeAllViews();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (Map.Entry<String, Variant> entry : variantMap.entrySet()) {
            TextView tv = new TextView(this);
            tv.setPadding(0, 4, 0, 4);
            tv.setTextSize(13);
            tv.setTextColor(getResources().getColor(R.color.text_dark, getTheme()));
            tv.setText("• " + entry.getKey() + ": " + currencyFormat.format(entry.getValue().getPrice()) + " (Nhấn để xóa)");

            tv.setOnClickListener(vClick -> {
                variantMap.remove(entry.getKey());
                renderVariantsList();
            });

            binding.layoutVariantsContainer.addView(tv);
        }
    }

    private void saveProductToFirebase() {
        String id = binding.etEditProductId.getText() != null ? binding.etEditProductId.getText().toString().trim() : "";
        String name = binding.etEditProductName.getText() != null ? binding.etEditProductName.getText().toString().trim() : "";
        String brand = brands[binding.spEditProductBrand.getSelectedItemPosition()];
        String imagesStr = binding.etEditProductImages.getText() != null ? binding.etEditProductImages.getText().toString().trim() : "";
        String shortDesc = binding.etEditShortDesc.getText() != null ? binding.etEditShortDesc.getText().toString().trim() : "";
        String detailDesc = binding.etEditDetailDesc.getText() != null ? binding.etEditDetailDesc.getText().toString().trim() : "";

        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập ID và Tên sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> imagesList = new ArrayList<>();
        if (!TextUtils.isEmpty(imagesStr)) {
            String[] split = imagesStr.split(",");
            for (String img : split) {
                if (!img.trim().isEmpty()) {
                    imagesList.add(img.trim());
                }
            }
        }

        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setBrand(brand);
        product.setShort_description(shortDesc);
        product.setDetail_description(detailDesc);
        product.setImages(imagesList);
        product.setVariants(new HashMap<>(variantMap));

        binding.btnSaveProduct.setEnabled(false);

        FirebaseDatabase.getInstance(DB_URL)
                .getReference("Products")
                .child(id)
                .setValue(product)
                .addOnSuccessListener(aVoid -> {
                    binding.btnSaveProduct.setEnabled(true);
                    Toast.makeText(AdminEditProductActivity.this, "Lưu sản phẩm thành công 🎉", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSaveProduct.setEnabled(true);
                    Toast.makeText(AdminEditProductActivity.this, "Lỗi lưu sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
