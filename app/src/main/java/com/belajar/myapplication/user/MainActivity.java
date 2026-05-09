package com.belajar.myapplication.user;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.belajar.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_main);

        // Load Fragment pertama kali (Home)
        if (savedInstanceState == null) {
            loadFragment(new PageHomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new PageHomeFragment();
            } else if (id == R.id.nav_modul) {
                selectedFragment = new PageModulFragment();
            } else if (id == R.id.nav_chart) {
                selectedFragment = new PagePomodoroFragment();
            } else if (id == R.id.nav_profile) {
                selectedFragment = new PageProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.layout_fragment_container, fragment);
        ft.commit();
    }
}
