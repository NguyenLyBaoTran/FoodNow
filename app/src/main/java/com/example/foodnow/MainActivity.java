package com.example.foodnow;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodnow.fragment.HomeFragment;
import com.example.foodnow.fragment.OrdersFragment;
import com.example.foodnow.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(HomeFragment.class, "home");
                return true;
            }
            if (id == R.id.nav_orders) {
                showFragment(OrdersFragment.class, "orders");
                return true;
            }
            if (id == R.id.nav_me) {
                showFragment(ProfileFragment.class, "profile");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("open_orders", false)) {
                bottomNavigationView.setSelectedItemId(R.id.nav_orders);
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        } else {
            int selected = savedInstanceState.getInt("selected_tab", R.id.nav_home);
            bottomNavigationView.setSelectedItemId(selected);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("open_orders", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_orders);
        }
    }

    private void showFragment(Class<?> fragmentClass, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);
        if (targetFragment == null) {
            if (fragmentClass == HomeFragment.class) {
                targetFragment = new HomeFragment();
            } else if (fragmentClass == OrdersFragment.class) {
                targetFragment = new OrdersFragment();
            } else {
                targetFragment = new ProfileFragment();
            }
        }

        if (currentFragment != null && currentFragment != targetFragment && currentFragment.isAdded()) {
            transaction.hide(currentFragment);
        }

        if (targetFragment.isAdded()) {
            transaction.show(targetFragment);
        } else {
            transaction.add(R.id.fragmentContainer, targetFragment, tag);
        }

        transaction.commit();
        currentFragment = targetFragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", bottomNavigationView.getSelectedItemId());
    }
}