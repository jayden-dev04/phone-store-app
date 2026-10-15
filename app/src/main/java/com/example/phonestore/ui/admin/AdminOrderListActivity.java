package com.example.phonestore.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.AdminOrderAdapter;
import com.example.phonestore.databinding.ActivityAdminOrderListBinding;
import com.example.phonestore.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminOrderListActivity extends AppCompatActivity implements AdminOrderAdapter.OnAdminOrderClickListener {

    private static final String TAG = "AdminOrderListActivity";
    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";

    private ActivityAdminOrderListBinding binding;
    private AdminOrderAdapter adapter;
    private ArrayList<Order> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarAdminOrders.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadAllOrdersFromFirebase();
    }

    private void setupRecyclerView() {
        binding.rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(new ArrayList<>(), this);
        binding.rvAdminOrders.setAdapter(adapter);
    }

    private void loadAllOrdersFromFirebase() {
        binding.progressBarAdminOrders.setVisibility(View.VISIBLE);

        DatabaseReference ordersRef = FirebaseDatabase.getInstance(DB_URL).getReference("Orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.progressBarAdminOrders.setVisibility(View.GONE);
                allOrders.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        for (DataSnapshot orderSnapshot : userSnapshot.getChildren()) {
                            try {
                                Order order = orderSnapshot.getValue(Order.class);
                                if (order != null) {
                                    allOrders.add(0, order); // Recent orders first
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi đọc đơn hàng admin: " + orderSnapshot.getKey(), e);
                            }
                        }
                    }
                }

                adapter.setOrders(allOrders);
                if (allOrders.isEmpty()) {
                    Toast.makeText(AdminOrderListActivity.this, "Chưa có đơn hàng nào trên hệ thống.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBarAdminOrders.setVisibility(View.GONE);
                Toast.makeText(AdminOrderListActivity.this, "Lỗi nạp đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeStatus(Order order) {
        if (order == null || order.getOrderId() == null || order.getUserEmail() == null) return;

        String[] statuses = {"Đã thanh toán", "Đang giao hàng", "Đã giao / Hoàn thành", "Đã hủy"};

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái đợt mua #" + order.getOrderId())
                .setItems(statuses, (dialog, which) -> {
                    String statusVal = statuses[which];
                    if (which == 2) statusVal = "Đã hoàn thành";
                    final String finalStatus = statusVal;

                    String cleanEmail = order.getUserEmail().replace(".", "_").replace("@", "_at_");
                    DatabaseReference orderRef = FirebaseDatabase.getInstance(DB_URL)
                            .getReference("Orders")
                            .child(cleanEmail)
                            .child(order.getOrderId());

                    orderRef.child("status").setValue(finalStatus)
                            .addOnSuccessListener(aVoid -> Toast.makeText(AdminOrderListActivity.this, "Đã cập nhật trạng thái đơn hàng thành: " + finalStatus, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(AdminOrderListActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
