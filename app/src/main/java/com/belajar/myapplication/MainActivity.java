package com.belajar.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    LinearLayout pageHome, pageModul, pageChart, pageProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi semua halaman
        pageHome    = findViewById(R.id.pageHome);
        pageModul   = findViewById(R.id.pageModul);
        pageChart   = findViewById(R.id.pageChart);
        pageProfile = findViewById(R.id.pageProfile);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Logika pindah halaman saat menu diklik
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showPage(pageHome);
            } else if (id == R.id.nav_modul) {
                showPage(pageModul);
            } else if (id == R.id.nav_chart) {
                showPage(pageChart);
            } else if (id == R.id.nav_profile) {
                showPage(pageProfile);
            }

            return true;
        });
    }

    private void showPage(LinearLayout aktif) {
        // Sembunyikan semua halaman
        pageHome.setVisibility(View.GONE);
        pageModul.setVisibility(View.GONE);
        pageChart.setVisibility(View.GONE);
        pageProfile.setVisibility(View.GONE);

        // Tampilkan halaman yang dipilih
        aktif.setVisibility(View.VISIBLE);
    }
}