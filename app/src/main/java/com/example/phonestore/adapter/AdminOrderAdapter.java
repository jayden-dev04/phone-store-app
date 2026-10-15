package com.example.phonestore.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonestore.databinding.ItemAdminOrderBinding;
import com.example.phonestore.model.CartItem;
import com.example.phonestore.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {

    public interface OnAdminOrderClickListener {
        void onChangeStatus(Order order);
    }

    private List<Order> orderList;
    private final OnAdminOrderClickListener listener;

    public AdminOrderAdapter(List<Order> orderList, OnAdminOrderClickListener listener) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminOrderBinding binding = ItemAdminOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AdminOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminOrderBinding binding;

        public AdminOrderViewHolder(@NonNull ItemAdminOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.tvAdminOrderId.setText("Hóa đơn #" + (order.getOrderId() != null ? order.getOrderId() : "N/A"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", new Locale("vi", "VN"));
            binding.tvAdminOrderDate.setText(sdf.format(new Date(order.getOrderDate())));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvAdminOrderTotal.setText(currencyFormat.format(order.getTotalAmount()));

            String status = order.getStatus() != null ? order.getStatus() : Order.STATUS_PAID;
            binding.tvAdminOrderStatus.setText(status);

            if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                binding.tvAdminOrderStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvAdminOrderStatus.setTextColor(Color.parseColor("#D32F2F"));

                binding.layoutCancelReason.setVisibility(View.VISIBLE);
                String reason = (order.getCancelReason() != null && !order.getCancelReason().trim().isEmpty())
                        ? order.getCancelReason()
                        : "Khách hàng không nhập lý do";
                binding.tvAdminCancelReason.setText(reason);
            } else {
                binding.tvAdminOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvAdminOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
                binding.layoutCancelReason.setVisibility(View.GONE);
            }

            String userInfo = "👤 Khách: " + (order.getUserName() != null ? order.getUserName() : "N/A") +
                    " (" + (order.getUserEmail() != null ? order.getUserEmail() : "") + ")\n" +
                    "📞 SĐT: " + (order.getUserPhone() != null ? order.getUserPhone() : "N/A") + "\n" +
                    "📍 Địa chỉ: " + (order.getShippingAddress() != null ? order.getShippingAddress() : "N/A");
            binding.tvAdminOrderUser.setText(userInfo);

            // Populate items breakdown
            binding.layoutAdminOrderItems.removeAllViews();
            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    TextView tv = new TextView(itemView.getContext());
                    tv.setPadding(0, 2, 0, 2);
                    tv.setTextSize(13);
                    tv.setTextColor(Color.parseColor("#333333"));
                    long subtotal = item.getPrice() * item.getQuantity();
                    String line = "• " + item.getProductName() + " (" + item.getCapacity() + ") - " +
                            item.getQuantity() + "x " + currencyFormat.format(item.getPrice()) + " = " + currencyFormat.format(subtotal);
                    tv.setText(line);
                    binding.layoutAdminOrderItems.addView(tv);
                }
            }

            binding.btnChangeOrderStatus.setOnClickListener(v -> {
                if (listener != null) listener.onChangeStatus(order);
            });
        }
    }
}
