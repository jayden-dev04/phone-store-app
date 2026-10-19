package com.example.phonestore.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.phonestore.model.Product;
import com.example.phonestore.model.Variant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static final String PRODUCTS_NODE = "Products";
    // Đường dẫn Firebase Realtime Database chính thức của bạn
    private static final String DB_URL = "https://phonestore-f9b65-default-rtdb.asia-southeast1.firebasedatabase.app";

    public interface OnProductsLoadedListener {
        void onSuccess(ArrayList<Product> productList);
        void onError(String errorMessage);
    }

    public static void getAllProducts(Context context, OnProductsLoadedListener listener) {
        try {
            // Kết nối TRỰC TIẾP đến Online Firebase Realtime Database
            DatabaseReference databaseRef = FirebaseDatabase.getInstance(DB_URL).getReference(PRODUCTS_NODE);

            databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                Log.e(TAG, "Lỗi đọc sản phẩm từ Firebase: " + productSnapshot.getKey(), e);
                            }
                        }
                    }

                    Log.d(TAG, "Đã nạp " + productList.size() + " sản phẩm TRỰC TIẾP từ Firebase Online thành công!");
                    if (listener != null) {
                        listener.onSuccess(productList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Lỗi kết nối Firebase: " + error.getMessage());
                    if (listener != null) {
                        listener.onError("Lỗi kết nối Firebase: " + error.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi kết nối Firebase Online", e);
            if (listener != null) {
                listener.onError("Lỗi kết nối Firebase: " + e.getLocalizedMessage());
            }
        }
    }

    public static void getAllProducts(OnProductsLoadedListener listener) {
        getAllProducts(null, listener);
    }

    public static ArrayList<Product> parseLocalJson(Context context) {
        ArrayList<Product> productList = new ArrayList<>();
        try {
            String jsonString = null;
            if (context != null) {
                try {
                    InputStream is = context.getAssets().open("product.json");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    is.close();
                    jsonString = new String(buffer, StandardCharsets.UTF_8);
                } catch (Exception ignored) {
                }
            }

            if (jsonString == null || jsonString.isEmpty()) {
                java.io.File file = new java.io.File("product.json");
                if (file.exists()) {
                    java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    fis.read(data);
                    fis.close();
                    jsonString = new String(data, StandardCharsets.UTF_8);
                }
            }

            if (jsonString != null && !jsonString.isEmpty()) {
                JSONObject rootObj = new JSONObject(jsonString);
                JSONObject productsObj = rootObj.optJSONObject("Products");
                if (productsObj != null) {
                    Iterator<String> keys = productsObj.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject prodJson = productsObj.getJSONObject(key);

                        Product product = new Product();
                        product.setId(key);
                        product.setName(prodJson.optString("name"));
                        product.setBrand(prodJson.optString("brand"));
                        product.setShort_description(prodJson.optString("short_description"));
                        product.setDetail_description(prodJson.optString("detail_description"));

                        JSONArray imgArray = prodJson.optJSONArray("images");
                        if (imgArray != null) {
                            List<String> images = new ArrayList<>();
                            for (int i = 0; i < imgArray.length(); i++) {
                                images.add(imgArray.getString(i));
                            }
                            product.setImages(images);
                        }

                        JSONObject varObj = prodJson.optJSONObject("variants");
                        if (varObj != null) {
                            HashMap<String, Variant> variantMap = new HashMap<>();
                            Iterator<String> varKeys = varObj.keys();
                            while (varKeys.hasNext()) {
                                String vKey = varKeys.next();
                                JSONObject vJson = varObj.getJSONObject(vKey);
                                Variant variant = new Variant(
                                        vJson.optString("capacity"),
                                        vJson.optLong("price"),
                                        vJson.optInt("stock")
                                );
                                variantMap.put(vKey, variant);
                            }
                            product.setVariants(variantMap);
                        }

                        productList.add(product);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đọc JSON cục bộ", e);
        }
        return productList;
    }
}
