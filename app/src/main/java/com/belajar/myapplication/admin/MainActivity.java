package com.belajar.myapplication.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.belajar.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Aktivitas Utama untuk panel Admin.
 * Mengelola navigasi antar fragment menggunakan Bottom Navigation.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_main);

        // Inisialisasi Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_admin);

        // Tampilkan HomeFragment secara default saat aplikasi dibuka
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Listener untuk menangani klik pada menu navigasi bawah
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_modul) {
                selectedFragment = new ModulFragment();
            } else if (id == R.id.nav_chart) {
                selectedFragment = new StatistikFragment();
            }
            
            // Jika ada fragment yang dipilih, lakukan penggantian fragment
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    /**
     * Fungsi sederhana untuk mengganti fragment di container utama admin.
     * @param fragment Fragment tujuan yang akan ditampilkan.
     */
    private void loadFragment(Fragment fragment) {
        if (fragment == null) return;
        
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        // Mengganti isi frame_container dengan fragment baru
        ft.replace(R.id.admin_fragment_container, fragment);
        ft.commit();
    }
}
