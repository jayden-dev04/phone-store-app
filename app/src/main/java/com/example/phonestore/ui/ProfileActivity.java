package com.example.phonestore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phonestore.MainActivity;
import com.example.phonestore.databinding.ActivityProfileBinding;
import com.example.phonestore.model.User;
import com.example.phonestore.ui.admin.AdminMainActivity;
import com.example.phonestore.utils.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private PreferenceManager prefManager;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefManager = new PreferenceManager(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (!prefManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông tin cá nhân", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        binding.toolbarProfile.setNavigationOnClickListener(v -> finish());

        setupUserInfo();
        loadProfileFromFirestore();
        setupListeners();
    }

    private void setupUserInfo() {
        String email = prefManager.getUserEmail();
        String name = prefManager.getUserName();
        int role = prefManager.getUserRole();

        binding.tvProfileEmail.setText(email);
        binding.tvProfileName.setText(TextUtils.isEmpty(name) ? "Khách hàng" : name);
        binding.etProfileFullName.setText(name);

        if (role == User.ROLE_ADMIN) {
            binding.tvProfileRole.setText("VAI TRÒ: QUẢN TRỊ VIÊN (ROLE 1)");
            binding.btnProfileAdminDashboard.setVisibility(View.VISIBLE);
        } else {
            binding.tvProfileRole.setText("VAI TRÒ: KHÁCH HÀNG (ROLE 2)");
            binding.btnProfileAdminDashboard.setVisibility(View.GONE);
        }
    }

    private void loadProfileFromFirestore() {
        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser != null) {
            currentUid = fbUser.getUid();
        }

        if (TextUtils.isEmpty(currentUid)) {
            // Fallback query by email if UID not direct
            db.collection("users")
                    .whereEqualTo("email", prefManager.getUserEmail())
                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        if (!querySnapshots.isEmpty()) {
                            var doc = querySnapshots.getDocuments().get(0);
                            currentUid = doc.getId();
                            bindUserDoc(doc.getData());
                        }
                    });
        } else {
            db.collection("users").document(currentUid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            bindUserDoc(doc.getData());
                        }
                    });
        }
    }

    private void bindUserDoc(Map<String, Object> data) {
        if (data == null) return;

        if (data.containsKey("fullName") && data.get("fullName") != null) {
            String fullName = data.get("fullName").toString();
            binding.etProfileFullName.setText(fullName);
            binding.tvProfileName.setText(fullName);
        }

        if (data.containsKey("phone") && data.get("phone") != null) {
            binding.etProfilePhone.setText(data.get("phone").toString());
        } else {
            binding.etProfilePhone.setText("0987654321");
        }

        if (data.containsKey("address") && data.get("address") != null) {
            binding.etProfileAddress.setText(data.get("address").toString());
        } else {
            binding.etProfileAddress.setText("123 Đường 3/2, Quận 10, TP. Hồ Chí Minh");
        }
    }

    private void setupListeners() {
        binding.btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        binding.btnProfileOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        binding.btnProfileResetPassword.setOnClickListener(v -> resetPassword());

        binding.btnProfileAdminDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AdminMainActivity.class);
            startActivity(intent);
        });

        binding.btnProfileLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        prefManager.clearSession();
                        auth.signOut();
                        Toast.makeText(ProfileActivity.this, "Đã đăng xuất tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void saveProfileChanges() {
        String newName = binding.etProfileFullName.getText() != null ? binding.etProfileFullName.getText().toString().trim() : "";
        String newPhone = binding.etProfilePhone.getText() != null ? binding.etProfilePhone.getText().toString().trim() : "";
        String newAddress = binding.etProfileAddress.getText() != null ? binding.etProfileAddress.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "Vui lòng nhập họ và tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSaveProfile.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("phone", newPhone);
        updates.put("address", newAddress);

        if (!TextUtils.isEmpty(currentUid)) {
            db.collection("users").document(currentUid).update(updates)
                    .addOnSuccessListener(aVoid -> onSaveSuccess(newName))
                    .addOnFailureListener(e -> {
                        // Fallback set if doc doesn't exist yet
                        db.collection("users").document(currentUid).set(updates)
                                .addOnSuccessListener(aVoid -> onSaveSuccess(newName))
                                .addOnFailureListener(err -> {
                                    binding.btnSaveProfile.setEnabled(true);
                                    Toast.makeText(ProfileActivity.this, "Lỗi cập nhật: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
        } else {
            onSaveSuccess(newName);
        }
    }

    private void onSaveSuccess(String newName) {
        binding.btnSaveProfile.setEnabled(true);
        prefManager.saveUserSession(true, prefManager.getUserEmail(), newName, prefManager.getUserRole());
        binding.tvProfileName.setText(newName);
        Toast.makeText(this, "Đã cập nhật thông tin cá nhân thành công! 🎉", Toast.LENGTH_SHORT).show();
    }

    private void resetPassword() {
        String email = prefManager.getUserEmail();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Không tìm thấy email tài khoản.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ProfileActivity.this, "Đã gửi email khôi phục/đổi mật khẩu đến " + email, Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(ProfileActivity.this, "Không thể gửi email: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
