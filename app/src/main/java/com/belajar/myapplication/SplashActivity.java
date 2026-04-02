package com.belajar.myapplication; // Sesuaikan dengan nama package kamu

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Pastikan ini nama file XML splash kamu

        // Delay 3000ms = 3 detik
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Perintah pindah dari Splash ke Login
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);

                // Menutup Splash agar tidak bisa balik lagi kalau ditekan tombol back
                finish();
            }
        }, 3000);
    }
}