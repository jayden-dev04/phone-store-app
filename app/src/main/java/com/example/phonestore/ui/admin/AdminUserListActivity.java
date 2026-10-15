package com.example.phonestore.ui.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.AdminUserAdapter;
import com.example.phonestore.databinding.ActivityAdminUserListBinding;
import com.example.phonestore.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminUserListActivity extends AppCompatActivity implements AdminUserAdapter.OnUserClickListener {

    private static final String TAG = "AdminUserListActivity";
    private ActivityAdminUserListBinding binding;
    private AdminUserAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        binding.toolbarAdminUsers.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        loadUsersFromFirestore();
    }

    private void setupRecyclerView() {
        binding.rvAdminUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(new ArrayList<>(), this);
        binding.rvAdminUsers.setAdapter(adapter);
    }

    private void loadUsersFromFirestore() {
        binding.progressBarAdminUsers.setVisibility(View.VISIBLE);

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBarAdminUsers.setVisibility(View.GONE);
                    ArrayList<User> userList = new ArrayList<>();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            try {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    if (user.getUid() == null) {
                                        user.setUid(doc.getId());
                                    }
                                    userList.add(user);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi nạp user: " + doc.getId(), e);
                            }
                        }
                    }
                    adapter.setUsers(userList);
                })
                .addOnFailureListener(e -> {
                    binding.progressBarAdminUsers.setVisibility(View.GONE);
                    Toast.makeText(AdminUserListActivity.this, "Lỗi nạp danh sách tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onToggleRole(User user) {
        if (user == null || user.getUid() == null) return;

        int newRole = (user.getRole() == User.ROLE_ADMIN) ? User.ROLE_USER : User.ROLE_ADMIN;
        String roleName = (newRole == User.ROLE_ADMIN) ? "Quản Trị Viên (Role 1)" : "Khách Hàng (Role 2)";

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận chuyển Role")
                .setMessage("Bạn có chắc muốn chuyển vai trò của tài khoản " + user.getEmail() + " thành " + roleName + " không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    db.collection("users").document(user.getUid())
                            .update("role", newRole)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminUserListActivity.this, "Cập nhật Role thành công!", Toast.LENGTH_SHORT).show();
                                loadUsersFromFirestore();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(AdminUserListActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
