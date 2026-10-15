package com.example.phonestore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.phonestore.databinding.ActivityMainBinding;
import com.example.phonestore.model.User;
import com.example.phonestore.ui.LoginActivity;
import com.example.phonestore.ui.ProfileActivity;
import com.example.phonestore.ui.admin.AdminMainActivity;
import com.example.phonestore.ui.fragment.CartFragment;
import com.example.phonestore.ui.fragment.HomeFragment;
import com.example.phonestore.ui.fragment.SavedFragment;
import com.example.phonestore.ui.fragment.SearchFragment;
import com.example.phonestore.utils.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    public static final int TAB_HOME = 0;
    public static final int TAB_SEARCH = 1;
    public static final int TAB_SAVED = 2;
    public static final int TAB_CART = 3;

    private ActivityMainBinding binding;
    private PreferenceManager prefManager;

    private Fragment homeFragment;
    private Fragment searchFragment;
    private SavedFragment savedFragment;
    private Fragment cartFragment;

    private int currentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = new PreferenceManager(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHeaderAndListeners();
        setupBottomNavigation();

        selectTab(TAB_HOME);
    }

    private void setupHeaderAndListeners() {
        View.OnClickListener accountClickListener = v -> handleAccountClick();
        binding.cardAvatar.setOnClickListener(accountClickListener);
        binding.btnAvatar.setOnClickListener(accountClickListener);

        binding.btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo của bạn", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupBottomNavigation() {
        binding.btnNavHome.setOnClickListener(v -> selectTab(TAB_HOME));
        binding.btnNavSearch.setOnClickListener(v -> selectTab(TAB_SEARCH));
        binding.btnNavGrid.setOnClickListener(v -> selectTab(TAB_SAVED));
        binding.btnNavBag.setOnClickListener(v -> selectTab(TAB_CART));
    }

    public void selectTab(int tabIndex) {
        if (currentTab == tabIndex) return;
        currentTab = tabIndex;

        Fragment selectedFragment;
        ImageButton activeBtn;

        switch (tabIndex) {
            case TAB_SEARCH:
                if (searchFragment == null) searchFragment = new SearchFragment();
                selectedFragment = searchFragment;
                activeBtn = binding.btnNavSearch;
                break;

            case TAB_SAVED:
                if (savedFragment == null) savedFragment = new SavedFragment();
                selectedFragment = savedFragment;
                activeBtn = binding.btnNavGrid;
                break;

            case TAB_CART:
                if (cartFragment == null) cartFragment = new CartFragment();
                selectedFragment = cartFragment;
                activeBtn = binding.btnNavBag;
                break;

            case TAB_HOME:
            default:
                if (homeFragment == null) homeFragment = new HomeFragment();
                selectedFragment = homeFragment;
                activeBtn = binding.btnNavHome;
                break;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, selectedFragment)
                .commit();

        updateBottomNavTint(activeBtn);
    }

    public void openCategoryWithBrand(String brandName) {
        selectTab(TAB_SAVED);
        if (savedFragment != null) {
            savedFragment.selectBrand(brandName);
        }
    }

    private void updateBottomNavTint(ImageButton activeBtn) {
        int activeColor = Color.parseColor("#111111");
        int inactiveColor = Color.parseColor("#888888");

        binding.btnNavHome.setColorFilter(binding.btnNavHome == activeBtn ? activeColor : inactiveColor);
        binding.btnNavSearch.setColorFilter(binding.btnNavSearch == activeBtn ? activeColor : inactiveColor);
        binding.btnNavGrid.setColorFilter(binding.btnNavGrid == activeBtn ? activeColor : inactiveColor);
        binding.btnNavBag.setColorFilter(binding.btnNavBag == activeBtn ? activeColor : inactiveColor);
    }

    private void handleAccountClick() {
        if (prefManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
