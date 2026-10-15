package com.example.phonestore.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phonestore.R;
import com.example.phonestore.databinding.ItemOrderBinding;
import com.example.phonestore.model.CartItem;
import com.example.phonestore.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onCancelOrder(Order order);
    }

    private List<Order> orderList;
    private final OnOrderClickListener listener;

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orderList = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orderList.get(position));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final ItemOrderBinding binding;

        public OrderViewHolder(@NonNull ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Order order) {
            binding.tvOrderId.setText("Hóa đơn #" + order.getOrderId());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", new Locale("vi", "VN"));
            binding.tvOrderDate.setText(sdf.format(new Date(order.getOrderDate())));

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvOrderTotal.setText(currencyFormat.format(order.getTotalAmount()));

            String status = order.getStatus() != null ? order.getStatus() : Order.STATUS_PAID;
            binding.tvOrderStatus.setText(status);

            if (Order.STATUS_CANCELLED.equalsIgnoreCase(status)) {
                binding.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                binding.tvOrderStatus.setTextColor(Color.parseColor("#D32F2F"));
                binding.btnCancelOrder.setVisibility(View.GONE);
            } else {
                binding.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                binding.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
                binding.btnCancelOrder.setVisibility(View.VISIBLE);
            }

            String customerInfo = "👤 Khách hàng: " + (order.getUserName() != null ? order.getUserName() : "N/A") +
                    " (" + (order.getUserPhone() != null ? order.getUserPhone() : "N/A") + ")\n" +
                    "📍 Địa chỉ: " + (order.getShippingAddress() != null ? order.getShippingAddress() : "N/A");
            binding.tvOrderCustomerInfo.setText(customerInfo);

            // Populate Items List
            binding.layoutOrderItems.removeAllViews();
            if (order.getItems() != null) {
                for (CartItem item : order.getItems()) {
                    TextView tvItem = new TextView(itemView.getContext());
                    tvItem.setPadding(0, 4, 0, 4);
                    tvItem.setTextSize(13);
                    tvItem.setTextColor(Color.parseColor("#333333"));
                    long subtotal = item.getPrice() * item.getQuantity();
                    String lineText = "• " + item.getProductName() + " (" + item.getCapacity() + ") - " +
                            item.getQuantity() + "x " + currencyFormat.format(item.getPrice()) +
                            " = " + currencyFormat.format(subtotal);
                    tvItem.setText(lineText);
                    binding.layoutOrderItems.addView(tvItem);
                }
            }

            binding.btnCancelOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCancelOrder(order);
                }
            });
        }
    }
}
