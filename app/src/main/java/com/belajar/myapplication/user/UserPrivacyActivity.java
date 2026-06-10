package com.belajar.myapplication.user;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;

public class UserPrivacyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_privacy);

        TextView tvTitle = findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Privasi & Keamanan");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
}
