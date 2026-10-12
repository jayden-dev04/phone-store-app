package com.example.phonestore.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.R;
import com.example.phonestore.databinding.ActivityProductDetailBinding;
import com.example.phonestore.model.Product;

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT = "extra_product";

    private ActivityProductDetailBinding binding;

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

        long minPrice = product.getMinPrice();
        if (minPrice > 0) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvDetailPrice.setText(formatter.format(minPrice));
        } else {
            binding.tvDetailPrice.setText("Liên hệ");
        }

        String description = product.getShort_description();
        if (description == null || description.trim().isEmpty()) {
            description = product.getDetail_description();
        }
        binding.tvDetailDescription.setText(description != null ? description : "Chưa có mô tả.");

        binding.imgDetailProduct.setImageResource(R.drawable.ic_phone_store);
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String imageUrl = product.getImages().get(0);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    URL url = new URL(imageUrl);
                    InputStream input = url.openConnection().getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    binding.getRoot().post(() -> binding.imgDetailProduct.setImageBitmap(bitmap));
                } catch (Exception ignored) {
                }
            });
        }

        binding.btnAddToCart.setOnClickListener(v ->
                Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng!", Toast.LENGTH_SHORT).show()
        );
    }
}
