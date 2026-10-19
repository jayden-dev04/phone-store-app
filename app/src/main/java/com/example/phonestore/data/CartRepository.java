package com.example.phonestore.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.phonestore.model.CartItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    private static final String TAG = "CartRepository";
    private static final String CARTS_NODE = "Carts";
    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface CartCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface OnCartItemsLoadedListener {
        void onSuccess(ArrayList<CartItem> cartItems);
        void onError(String errorMessage);
    }

    private static String cleanEmail(String email) {
        if (email == null) return "guest_user";
        return email.replace(".", "_").replace("@", "_at_");
    }

    private static DatabaseReference getCartReference(String userEmail) {
        return FirebaseDatabase.getInstance(DB_URL)
                .getReference(CARTS_NODE)
                .child(cleanEmail(userEmail));
    }

    public static void addToCart(String userEmail, CartItem newItem, CartCallback callback) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            if (callback != null) callback.onError("Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng.");
            return;
        }

        DatabaseReference cartRef = getCartReference(userEmail).child(newItem.getId());

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    CartItem existing = snapshot.getValue(CartItem.class);
                    if (existing != null) {
                        newItem.setQuantity(existing.getQuantity() + newItem.getQuantity());
                    }
                }
                cartRef.setValue(newItem)
                        .addOnSuccessListener(aVoid -> {
                            if (callback != null) callback.onSuccess("Đã thêm sản phẩm vào giỏ hàng!");
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) callback.onError("Lỗi thêm vào giỏ hàng: " + e.getLocalizedMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError("Lỗi kết nối Firebase: " + error.getMessage());
            }
        });
    }

    public static void getCartItems(String userEmail, OnCartItemsLoadedListener listener) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            if (listener != null) listener.onSuccess(new ArrayList<>());
            return;
        }

        getCartReference(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CartItem> cartList = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        try {
                            CartItem item = itemSnapshot.getValue(CartItem.class);
                            if (item != null) {
                                cartList.add(item);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc giỏ hàng: " + itemSnapshot.getKey(), e);
                        }
                    }
                }
                if (listener != null) listener.onSuccess(cartList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onError("Không thể nạp giỏ hàng: " + error.getMessage());
            }
        });
    }

    public static void updateQuantity(String userEmail, String itemId, int newQuantity) {
        if (userEmail == null || itemId == null) return;
        DatabaseReference itemRef = getCartReference(userEmail).child(itemId);
        if (newQuantity <= 0) {
            itemRef.removeValue();
        } else {
            itemRef.child("quantity").setValue(newQuantity);
        }
    }

    public static void updateSelection(String userEmail, String itemId, boolean isSelected) {
        if (userEmail == null || itemId == null) return;
        getCartReference(userEmail).child(itemId).child("selected").setValue(isSelected);
    }

    public static void deleteCartItem(String userEmail, String itemId) {
        if (userEmail == null || itemId == null) return;
        getCartReference(userEmail).child(itemId).removeValue();
    }

    public static void clearPurchasedItems(String userEmail, List<CartItem> purchasedItems) {
        if (userEmail == null || purchasedItems == null) return;
        DatabaseReference cartRef = getCartReference(userEmail);
        for (CartItem item : purchasedItems) {
            if (item.getId() != null) {
                cartRef.child(item.getId()).removeValue();
            }
        }
    }
}
