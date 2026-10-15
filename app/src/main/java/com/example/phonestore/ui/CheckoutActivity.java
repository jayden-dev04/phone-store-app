package com.example.phonestore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.R;
import com.example.phonestore.data.CartRepository;
import com.example.phonestore.data.OrderRepository;
import com.example.phonestore.databinding.ActivityCheckoutBinding;
import com.example.phonestore.model.CartItem;
import com.example.phonestore.model.Order;
import com.example.phonestore.utils.PreferenceManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_ITEMS = "extra_selected_items";

    private ActivityCheckoutBinding binding;
    private PreferenceManager prefManager;
    private ArrayList<CartItem> selectedItems = new ArrayList<>();
    private long totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PreferenceManager(this);

        binding.toolbarCheckout.setNavigationOnClickListener(v -> finish());

        selectedItems = (ArrayList<CartItem>) getIntent().getSerializableExtra(EXTRA_SELECTED_ITEMS);
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn để thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupCustomerInfo();
        displaySelectedItems();
        setupListeners();
    }

    private void setupCustomerInfo() {
        if (prefManager.isLoggedIn()) {
            binding.etCheckoutName.setText(prefManager.getUserName());
            binding.etCheckoutPhone.setText("0987654321");
            binding.etCheckoutAddress.setText("123 Đường 3/2, Quận 10, TP. Hồ Chí Minh");
        }
    }

    private void displaySelectedItems() {
        binding.layoutCheckoutItems.removeAllViews();
        totalAmount = 0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (CartItem item : selectedItems) {
            long itemSubtotal = item.getPrice() * item.getQuantity();
            totalAmount += itemSubtotal;

            View itemView = LayoutInflater.from(this).inflate(R.layout.item_product_horizontal, binding.layoutCheckoutItems, false);
            // Dynamic simple row for checkout summary
            TextView tvItem = new TextView(this);
            tvItem.setPadding(0, 8, 0, 8);
            tvItem.setTextSize(14);
            tvItem.setTextColor(getResources().getColor(R.color.text_dark, getTheme()));
            String text = "• " + item.getProductName() + " (" + item.getCapacity() + ")\n   " +
                    item.getQuantity() + "x " + currencyFormat.format(item.getPrice()) + " = " + currencyFormat.format(itemSubtotal);
            tvItem.setText(text);

            binding.layoutCheckoutItems.addView(tvItem);
        }

        binding.tvSubtotalPrice.setText(currencyFormat.format(totalAmount));
        binding.tvDiscountPrice.setText("0 đ");
        binding.tvFinalTotalPrice.setText(currencyFormat.format(totalAmount));
    }

    private void setupListeners() {
        binding.btnConfirmOrder.setOnClickListener(v -> processCheckout());
    }

    private void processCheckout() {
        String name = binding.etCheckoutName.getText() != null ? binding.etCheckoutName.getText().toString().trim() : "";
        String phone = binding.etCheckoutPhone.getText() != null ? binding.etCheckoutPhone.getText().toString().trim() : "";
        String address = binding.etCheckoutAddress.getText() != null ? binding.etCheckoutAddress.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = binding.rbCOD.isChecked() ? "Thanh toán khi nhận hàng (COD)" : "Chuyển khoản Ngân hàng";
        String orderId = "ORD_" + System.currentTimeMillis();

        Order order = new Order(
                orderId,
                prefManager.getUserEmail(),
                name,
                phone,
                address,
                System.currentTimeMillis(),
                totalAmount,
                0,
                paymentMethod,
                Order.STATUS_PAID,
                selectedItems
        );

        binding.btnConfirmOrder.setEnabled(false);

        OrderRepository.createOrder(order, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(String message) {
                binding.btnConfirmOrder.setEnabled(true);

                // Clear purchased items from Firebase Cart
                CartRepository.clearPurchasedItems(prefManager.getUserEmail(), selectedItems);

                new AlertDialog.Builder(CheckoutActivity.this)
                        .setTitle("Đặt hàng thành công 🎉")
                        .setMessage("Đơn hàng #" + orderId + " đã được lưu và xuất hóa đơn thành công!")
                        .setPositiveButton("Xem Lịch Sử Mua Hàng", (dialog, which) -> {
                            Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }

            @Override
            public void onError(String errorMessage) {
                binding.btnConfirmOrder.setEnabled(true);
                Toast.makeText(CheckoutActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
