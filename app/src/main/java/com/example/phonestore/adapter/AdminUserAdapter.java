package com.example.phonestore.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonestore.databinding.ItemAdminUserBinding;
import com.example.phonestore.model.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.AdminUserViewHolder> {

    public interface OnUserClickListener {
        void onToggleRole(User user);
    }

    private List<User> userList;
    private final OnUserClickListener listener;

    public AdminUserAdapter(List<User> userList, OnUserClickListener listener) {
        this.userList = userList != null ? userList : new ArrayList<>();
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.userList = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminUserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminUserViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    class AdminUserViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminUserBinding binding;

        public AdminUserViewHolder(@NonNull ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            binding.tvAdminUserName.setText(user.getFullName() != null ? user.getFullName() : "Chưa đặt tên");
            binding.tvAdminUserEmail.setText(user.getEmail() != null ? user.getEmail() : "N/A");
            binding.tvAdminUserPhone.setText("SĐT: " + (user.getPhone() != null ? user.getPhone() : "Chưa có"));

            if (user.getRole() == User.ROLE_ADMIN) {
                binding.tvAdminUserRole.setText("ROLE 1: QUẢN TRỊ VIÊN");
                binding.tvAdminUserRole.setBackgroundColor(Color.parseColor("#1565C0"));
                binding.tvAdminUserRole.setTextColor(Color.WHITE);
            } else {
                binding.tvAdminUserRole.setText("ROLE 2: KHÁCH HÀNG");
                binding.tvAdminUserRole.setBackgroundColor(Color.parseColor("#E0E0E0"));
                binding.tvAdminUserRole.setTextColor(Color.parseColor("#333333"));
            }

            binding.btnToggleUserRole.setOnClickListener(v -> {
                if (listener != null) listener.onToggleRole(user);
            });
        }
    }
}
