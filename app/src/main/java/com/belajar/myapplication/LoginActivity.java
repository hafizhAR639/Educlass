package com.belajar.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    ImageView icEye;
    Button btnLogin;
    TextView tvSignUp;
    boolean passwordVisible = false;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        icEye = findViewById(R.id.icEye);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        icEye.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            etPassword.setTransformationMethod(passwordVisible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye.setImageResource(passwordVisible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            etPassword.setSelection(etPassword.length());
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (db.loginUser(email, password)) {
                Toast.makeText(this, "Selamat Datang!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomepageActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email atau Password Salah!", Toast.LENGTH_SHORT).show();
            }
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}