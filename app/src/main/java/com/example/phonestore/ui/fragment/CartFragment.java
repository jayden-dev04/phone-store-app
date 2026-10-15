package com.example.phonestore.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.phonestore.adapter.CartAdapter;
import com.example.phonestore.data.CartRepository;
import com.example.phonestore.databinding.FragmentCartBinding;
import com.example.phonestore.model.CartItem;
import com.example.phonestore.ui.CheckoutActivity;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.ui.OrderHistoryActivity;
import com.example.phonestore.utils.PreferenceManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemChangeListener {

    private FragmentCartBinding binding;
    private PreferenceManager prefManager;
    private CartAdapter cartAdapter;
    private ArrayList<CartItem> currentCartItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PreferenceManager(requireContext());

        setupRecyclerView();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserAndLoadCart();
    }

    private void setupRecyclerView() {
        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        cartAdapter = new CartAdapter(new ArrayList<>(), this);
        binding.rvCartItems.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        binding.btnLoginCart.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        binding.tvOrderHistoryLink.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử mua hàng.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(requireContext(), OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        binding.btnCheckout.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : currentCartItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }

            if (selectedItems.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(requireContext(), CheckoutActivity.class);
            intent.putExtra(CheckoutActivity.EXTRA_SELECTED_ITEMS, selectedItems);
            startActivity(intent);
        });
    }

    private void checkUserAndLoadCart() {
        if (!prefManager.isLoggedIn()) {
            binding.layoutNotLoggedIn.setVisibility(View.VISIBLE);
            binding.layoutEmptyCart.setVisibility(View.GONE);
            binding.rvCartItems.setVisibility(View.GONE);
            binding.cardCartSummary.setVisibility(View.GONE);
            binding.progressBarCart.setVisibility(View.GONE);
            return;
        }

        binding.layoutNotLoggedIn.setVisibility(View.GONE);
        binding.progressBarCart.setVisibility(View.VISIBLE);

        CartRepository.getCartItems(prefManager.getUserEmail(), new CartRepository.OnCartItemsLoadedListener() {
            @Override
            public void onSuccess(ArrayList<CartItem> cartItems) {
                if (!isAdded() || binding == null) return;
                binding.progressBarCart.setVisibility(View.GONE);
                currentCartItems = cartItems != null ? cartItems : new ArrayList<>();

                if (currentCartItems.isEmpty()) {
                    binding.layoutEmptyCart.setVisibility(View.VISIBLE);
                    binding.rvCartItems.setVisibility(View.GONE);
                    binding.cardCartSummary.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyCart.setVisibility(View.GONE);
                    binding.rvCartItems.setVisibility(View.VISIBLE);
                    binding.cardCartSummary.setVisibility(View.VISIBLE);
                    cartAdapter.setCartItems(currentCartItems);
                    updateTotalPrice();
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded() || binding == null) return;
                binding.progressBarCart.setVisibility(View.GONE);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalPrice() {
        long total = 0;
        if (currentCartItems != null) {
            for (CartItem item : currentCartItems) {
                if (item.isSelected()) {
                    total += (item.getPrice() * item.getQuantity());
                }
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvTotalCartPrice.setText(currencyFormat.format(total));
    }

    @Override
    public void onSelectionChanged(CartItem item, boolean isSelected) {
        if (prefManager.isLoggedIn() && item != null) {
            CartRepository.updateSelection(prefManager.getUserEmail(), item.getId(), isSelected);
            updateTotalPrice();
        }
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (prefManager.isLoggedIn() && item != null) {
            CartRepository.updateQuantity(prefManager.getUserEmail(), item.getId(), newQuantity);
            updateTotalPrice();
        }
    }

    @Override
    public void onItemDeleted(CartItem item) {
        if (prefManager.isLoggedIn() && item != null) {
            CartRepository.deleteCartItem(prefManager.getUserEmail(), item.getId());
            Toast.makeText(requireContext(), "Đã xóa " + item.getProductName() + " khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
