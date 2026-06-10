package com.belajar.myapplication.admin;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.android.material.navigation.NavigationBarView;
import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {

    private int currentMenuId = R.id.nav_home;
    private final Map<Integer, Integer> menuOrder = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

        // Define menu order for transitions
        menuOrder.put(R.id.nav_home, 0);
        menuOrder.put(R.id.nav_modul, 1);
        menuOrder.put(R.id.nav_chart, 2);
        menuOrder.put(R.id.nav_profile, 3);

        NavigationBarView bottomNav = findViewById(R.id.bottom_nav_admin);

        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment(), false);
        }

        // Sync Navbar on Back Pressed
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.admin_fragment_container);
            if (currentFragment instanceof AdminHomeFragment) {
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                currentMenuId = R.id.nav_home;
            } else if (currentFragment instanceof AdminSubjectListFragment || currentFragment instanceof AdminTopicListFragment) {
                bottomNav.getMenu().findItem(R.id.nav_modul).setChecked(true);
                currentMenuId = R.id.nav_modul;
            } else if (currentFragment instanceof AdminStatistikFragment) {
                bottomNav.getMenu().findItem(R.id.nav_chart).setChecked(true);
                currentMenuId = R.id.nav_chart;
            } else if (currentFragment instanceof AdminProfileFragment) {
                bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                currentMenuId = R.id.nav_profile;
            }
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int newId = item.getItemId();
            if (newId == currentMenuId) return true;

            Fragment selectedFragment = null;
            
            Integer newPos = menuOrder.get(newId);
            Integer currentPos = menuOrder.get(currentMenuId);
            boolean slideRight = newPos != null && currentPos != null && newPos > currentPos;

            if (newId == R.id.nav_home) {
                selectedFragment = new AdminHomeFragment();
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                loadFragment(selectedFragment, slideRight, false);
                currentMenuId = newId;
                return true;
            } else if (newId == R.id.nav_modul) {
                selectedFragment = new AdminSubjectListFragment();
            } else if (newId == R.id.nav_chart) {
                selectedFragment = new AdminStatistikFragment();
            } else if (newId == R.id.nav_profile) {
                selectedFragment = new AdminProfileFragment();
            }
            
            if (selectedFragment != null) {
                currentMenuId = newId;
                loadFragment(selectedFragment, slideRight, true);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean slideRight, boolean addToBackStack) {
        if (fragment == null) return;
        
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        if (slideRight) {
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
        }
        
        transaction.replace(R.id.admin_fragment_container, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
    }

    private void loadFragment(Fragment fragment, boolean slideRight) {
        loadFragment(fragment, slideRight, false);
    }
}
