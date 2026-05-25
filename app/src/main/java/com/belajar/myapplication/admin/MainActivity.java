package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_admin);

        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment(), false);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int newId = item.getItemId();
            if (newId == currentMenuId) return true;

            Fragment selectedFragment = null;
            
            Integer newPos = menuOrder.get(newId);
            Integer currentPos = menuOrder.get(currentMenuId);
            boolean slideRight = newPos != null && currentPos != null && newPos > currentPos;

            if (newId == R.id.nav_home) {
                selectedFragment = new AdminHomeFragment();
            } else if (newId == R.id.nav_modul) {
                selectedFragment = new ModulFragment();
            } else if (newId == R.id.nav_chart) {
                selectedFragment = new StatistikFragment();
            }
            
            if (selectedFragment != null) {
                currentMenuId = newId;
                loadFragment(selectedFragment, slideRight);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment, boolean slideRight) {
        if (fragment == null) return;
        
        if (slideRight) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.admin_fragment_container, fragment)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.admin_fragment_container, fragment)
                    .commit();
        }
    }
}
