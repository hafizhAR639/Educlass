package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.belajar.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private int currentMenuId = R.id.nav_home;
    private final Map<Integer, Integer> menuOrder = new HashMap<>();
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_main);

        // Define menu order for transitions
        menuOrder.put(R.id.nav_home, 0);
        menuOrder.put(R.id.nav_modul, 1);
        menuOrder.put(R.id.nav_chart, 2);
        menuOrder.put(R.id.nav_profile, 3);

        bottomNav = findViewById(R.id.bottom_nav_main);

        // Register Lifecycle Callbacks to automatically update Navbar
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                updateBottomNavVisibility(f, bottomNav);
            }
        }, false);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int newId = item.getItemId();
            if (newId == currentMenuId) return true;

            Fragment selectedFragment = null;
            
            Integer newPos = menuOrder.get(newId);
            Integer currentPos = menuOrder.get(currentMenuId);
            boolean slideRight = newPos != null && currentPos != null && newPos > currentPos;

            if (newId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (newId == R.id.nav_modul) {
                selectedFragment = new ModulFragment();
            } else if (newId == R.id.nav_chart) {
                selectedFragment = new StatistikFragment();
            } else if (newId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                currentMenuId = newId;
                loadFragment(selectedFragment, slideRight);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean slideRight) {
        if (slideRight) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.layout_fragment_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.layout_fragment_container, fragment)
                    .commit();
        }
    }

    private void updateBottomNavVisibility(Fragment fragment, BottomNavigationView bottomNav) {
        if (bottomNav == null) return;
        
        if (fragment instanceof PremiumFragment || fragment instanceof NotificationFragment) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }

        // Update selected item based on fragment type
        int targetId = -1;
        if (fragment instanceof HomeFragment) {
            targetId = R.id.nav_home;
        } else if (fragment instanceof ModulFragment || fragment instanceof MateriFragment || fragment instanceof ContentFragment) {
            targetId = R.id.nav_modul;
        } else if (fragment instanceof StatistikFragment) {
            targetId = R.id.nav_chart;
        } else if (fragment instanceof ProfileFragment) {
            targetId = R.id.nav_profile;
        }

        if (targetId != -1 && targetId != currentMenuId) {
            currentMenuId = targetId;
            bottomNav.getMenu().findItem(targetId).setChecked(true);
        }
    }
}
