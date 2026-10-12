package com.example.phonestore.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.phonestore.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static final String PRODUCTS_NODE = "Products";

    public interface OnProductsLoadedListener {
        void onSuccess(ArrayList<Product> productList);
        void onError(String errorMessage);
    }

    public static void getAllProducts(OnProductsLoadedListener listener) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference(PRODUCTS_NODE);

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Product> productList = new ArrayList<>();

                if (snapshot.exists()) {
                    for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                        try {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null) {
                                product.setId(productSnapshot.getKey());
                                productList.add(product);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing product: " + productSnapshot.getKey(), e);
                        }
                    }
                }

                if (listener != null) {
                    listener.onSuccess(productList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        });
    }
}