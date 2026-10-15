package com.example.phonestore.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.OrderAdapter;
import com.example.phonestore.data.OrderRepository;
import com.example.phonestore.databinding.ActivityOrderHistoryBinding;
import com.example.phonestore.model.Order;
import com.example.phonestore.utils.PreferenceManager;

import java.util.ArrayList;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private ActivityOrderHistoryBinding binding;
    private PreferenceManager prefManager;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PreferenceManager(this);

        binding.toolbarOrders.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadOrdersFromFirebase();
    }

    private void setupRecyclerView() {
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(new ArrayList<>(), this);
        binding.rvOrders.setAdapter(orderAdapter);
    }

    private void loadOrdersFromFirebase() {
        if (!prefManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử mua hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBarOrders.setVisibility(View.VISIBLE);

        OrderRepository.getUserOrders(prefManager.getUserEmail(), new OrderRepository.OnOrdersLoadedListener() {
            @Override
            public void onSuccess(ArrayList<Order> orderList) {
                binding.progressBarOrders.setVisibility(View.GONE);
                if (orderList != null && !orderList.isEmpty()) {
                    binding.layoutEmptyOrders.setVisibility(View.GONE);
                    binding.rvOrders.setVisibility(View.VISIBLE);
                    orderAdapter.setOrders(orderList);
                } else {
                    binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
                    binding.rvOrders.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                binding.progressBarOrders.setVisibility(View.GONE);
                Toast.makeText(OrderHistoryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCancelOrder(Order order) {
        if (order == null || order.getOrderId() == null) return;

        String currentStatus = order.getStatus() != null ? order.getStatus() : "";
        if (Order.STATUS_CANCELLED.equalsIgnoreCase(currentStatus)
                || "Đang giao hàng".equalsIgnoreCase(currentStatus)
                || "Đã giao".equalsIgnoreCase(currentStatus)
                || "Đã hoàn thành".equalsIgnoreCase(currentStatus)) {
            Toast.makeText(this, "Không thể hủy đơn hàng ở trạng thái: " + currentStatus, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] cancelReasons = {
                "Thay đổi địa chỉ nhận hàng",
                "Có mặt hàng khác giá tốt hơn",
                "Thay đổi phương thức thanh toán",
                "Chưa hợp nhu cầu"
        };

        final int[] selectedIndex = {0};

        new AlertDialog.Builder(this)
                .setTitle("Lý do hủy đợt mua #" + order.getOrderId())
                .setSingleChoiceItems(cancelReasons, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Xác nhận Hủy", (dialog, which) -> {
                    String reason = cancelReasons[selectedIndex[0]];
                    OrderRepository.cancelOrder(prefManager.getUserEmail(), order.getOrderId(), reason, new OrderRepository.OrderCallback() {
                        @Override
                        public void onSuccess(String message) {
                            Toast.makeText(OrderHistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(OrderHistoryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Quay lại", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
