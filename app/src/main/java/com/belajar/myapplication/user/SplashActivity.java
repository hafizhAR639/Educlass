package com.belajar.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.belajar.myapplication.auth.AuthLoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_splash);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, AuthLoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
