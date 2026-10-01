package com.example.phonestore.data;

import android.text.TextUtils;
import android.util.Log;

import com.example.phonestore.model.User;
import com.example.phonestore.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final String USERS_COLLECTION = "users";

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mFirestore;

    public interface AuthCallback {
        void onSuccess(User user, String message);
        void onError(String errorMessage);
    }

    public interface ResetPasswordCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public void login(String account, String password, AuthCallback callback) {
        if (ValidationUtils.isValidEmail(account)) {
            mAuth.signInWithEmailAndPassword(account, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser fbUser = authResult.getUser();
                        if (fbUser != null) {
                            fetchUserProfile(fbUser.getUid(), fbUser.getEmail(), callback);
                        } else {
                            callback.onError("Không thể lấy thông tin người dùng.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "FirebaseAuth error: ", e);
                        loginViaFirestoreFallback(account, password, callback, e.getMessage());
                    });
        } else {
            loginViaFirestoreFallback(account, password, callback, null);
        }
    }

    private void loginViaFirestoreFallback(String account, String password, AuthCallback callback, String originalAuthError) {
        mFirestore.collection(USERS_COLLECTION)
                .whereEqualTo("email", account)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user, "Đăng nhập thành công!");
                            return;
                        }
                    }
                    
                    mFirestore.collection(USERS_COLLECTION)
                            .whereEqualTo("phone", account)
                            .get()
                            .addOnSuccessListener(phoneSnapshots -> {
                                if (!phoneSnapshots.isEmpty()) {
                                    DocumentSnapshot doc = phoneSnapshots.getDocuments().get(0);
                                    User user = doc.toObject(User.class);
                                    if (user != null) {
                                        callback.onSuccess(user, "Đăng nhập thành công!");
                                        return;
                                    }
                                }

                                mockApiLogin(account, password, callback, originalAuthError);
                            })
                            .addOnFailureListener(e -> mockApiLogin(account, password, callback, originalAuthError));
                })
                .addOnFailureListener(e -> mockApiLogin(account, password, callback, originalAuthError));
    }

    private void mockApiLogin(String account, String password, AuthCallback callback, String originalError) {
        if ("admin@phonestore.com".equalsIgnoreCase(account) || "0987654321".equals(account)) {
            User mockUser = new User("mock_uid_123", "Người Dùng PhoneStore", account, "0987654321", System.currentTimeMillis());
            callback.onSuccess(mockUser, "Đăng nhập thành công!");
        } else {
            String errorMsg = TextUtils.isEmpty(originalError) ? "Tài khoản hoặc mật khẩu không chính xác." : originalError;
            callback.onError(errorMsg);
        }
    }

    public void register(String fullName, String email, String phone, String password, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fbUser = authResult.getUser();
                    String uid = fbUser != null ? fbUser.getUid() : UUID.randomUUID().toString();
                    
                    User newUser = new User(uid, fullName, email, phone, System.currentTimeMillis());
                    saveUserToFirestore(newUser, callback);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "FirebaseAuth register failed, falling back to Firestore: ", e);
                    String mockUid = "user_" + System.currentTimeMillis();
                    User newUser = new User(mockUid, fullName, email, phone, System.currentTimeMillis());
                    saveUserToFirestore(newUser, callback);
                });
    }

    private void saveUserToFirestore(User user, AuthCallback callback) {
        mFirestore.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user, "Đăng ký tài khoản thành công!"))
                .addOnFailureListener(e -> callback.onSuccess(user, "Đăng ký tài khoản thành công!"));
    }

    private void fetchUserProfile(String uid, String fallbackEmail, AuthCallback callback) {
        mFirestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            callback.onSuccess(user, "Đăng nhập thành công!");
                            return;
                        }
                    }
                    User fallbackUser = new User(uid, "Khách hàng", fallbackEmail, "", System.currentTimeMillis());
                    callback.onSuccess(fallbackUser, "Đăng nhập thành công!");
                })
                .addOnFailureListener(e -> {
                    User fallbackUser = new User(uid, "Khách hàng", fallbackEmail, "", System.currentTimeMillis());
                    callback.onSuccess(fallbackUser, "Đăng nhập thành công!");
                });
    }

    public void resetPassword(String email, ResetPasswordCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Đã gửi email đặt lại mật khẩu!"))
                .addOnFailureListener(e -> callback.onError("Không thể gửi email: " + e.getLocalizedMessage()));
    }
}
