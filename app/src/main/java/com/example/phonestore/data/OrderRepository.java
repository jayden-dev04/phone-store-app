package com.example.phonestore.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.phonestore.model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderRepository {

    private static final String TAG = "OrderRepository";
    private static final String ORDERS_NODE = "Orders";
    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OrderCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface OnOrdersLoadedListener {
        void onSuccess(ArrayList<Order> orderList);
        void onError(String errorMessage);
    }

    private static String cleanEmail(String email) {
        if (email == null) return "guest_user";
        return email.replace(".", "_").replace("@", "_at_");
    }

    private static DatabaseReference getOrdersReference(String userEmail) {
        return FirebaseDatabase.getInstance(DB_URL)
                .getReference(ORDERS_NODE)
                .child(cleanEmail(userEmail));
    }

    public static void createOrder(Order order, OrderCallback callback) {
        if (order == null || order.getUserEmail() == null) {
            if (callback != null) callback.onError("Thanh toán thất bại: Thông tin không hợp lệ.");
            return;
        }

        DatabaseReference orderRef = getOrdersReference(order.getUserEmail()).child(order.getOrderId());
        orderRef.setValue(order)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess("Thanh toán & Đặt hàng thành công!");
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError("Lỗi lưu hóa đơn: " + e.getLocalizedMessage());
                });
    }

    public static void getUserOrders(String userEmail, OnOrdersLoadedListener listener) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            if (listener != null) listener.onSuccess(new ArrayList<>());
            return;
        }

        getOrdersReference(userEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Order> orderList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        try {
                            Order order = itemSnapshot.getValue(Order.class);
                            if (order != null) {
                                orderList.add(0, order); // Most recent orders first
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc hóa đơn: " + itemSnapshot.getKey(), e);
                        }
                    }
                }
                if (listener != null) listener.onSuccess(orderList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onError("Không thể nạp lịch sử hóa đơn: " + error.getMessage());
            }
        });
    }

    public static void cancelOrder(String userEmail, String orderId, String cancelReason, OrderCallback callback) {
        if (userEmail == null || orderId == null) return;

        DatabaseReference orderRef = getOrdersReference(userEmail).child(orderId);
        if (cancelReason != null) {
            orderRef.child("cancelReason").setValue(cancelReason);
        }
        orderRef.child("status").setValue(Order.STATUS_CANCELLED)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess("Đã hủy đợt mua thành công!");
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError("Lỗi hủy đợt mua: " + e.getLocalizedMessage());
                });
    }

    public static void cancelOrder(String userEmail, String orderId, OrderCallback callback) {
        cancelOrder(userEmail, orderId, "Không có lý do", callback);
    }
}
