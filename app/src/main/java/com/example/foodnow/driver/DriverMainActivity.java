package com.example.foodnow.driver;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.foodnow.R;
import com.example.foodnow.driver.fragment.DriverHomeFragment;
import com.example.foodnow.driver.fragment.DriverHistoryFragment;
import com.example.foodnow.driver.fragment.DriverProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DriverMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_driver_home) {
                showFragment(DriverHomeFragment.class, "driver_home");
                return true;
            }
            if (id == R.id.nav_driver_history) {
                showFragment(DriverHistoryFragment.class, "driver_history");
                return true;
            }
            if (id == R.id.nav_driver_profile) {
                showFragment(DriverProfileFragment.class, "driver_profile");
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_driver_home);
        } else {
            int selected = savedInstanceState.getInt("selected_tab", R.id.nav_driver_home);
            bottomNavigationView.setSelectedItemId(selected);
        }
    }

    private void showFragment(Class<?> fragmentClass, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);
        if (targetFragment == null) {
            try {
                targetFragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
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
