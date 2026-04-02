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

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    ImageView icEye1, icEye2;
    Button btnSignUp;
    TextView tvSignInLink; // Link untuk balik ke login
    boolean pass1Visible = false, pass2Visible = false;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);

        // Inisialisasi View berdasarkan ID di XML ScrollView kamu
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        icEye1 = findViewById(R.id.icEye1);
        icEye2 = findViewById(R.id.icEye2);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Di XML kamu ID-nya tvSignUp meskipun teksnya "login"
        tvSignInLink = findViewById(R.id.tvSignUp);

        // Toggle Password 1
        icEye1.setOnClickListener(v -> {
            pass1Visible = !pass1Visible;
            etPassword.setTransformationMethod(pass1Visible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye1.setImageResource(pass1Visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            etPassword.setSelection(etPassword.length());
        });

        // Toggle Password 2
        icEye2.setOnClickListener(v -> {
            pass2Visible = !pass2Visible;
            etConfirmPassword.setTransformationMethod(pass2Visible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye2.setImageResource(pass2Visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            etConfirmPassword.setSelection(etConfirmPassword.length());
        });

        btnSignUp.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPwd = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPwd)) {
                etConfirmPassword.setError("Password tidak cocok!");
                return;
            }

            boolean berhasil = db.registerUser(fullName, email, password);
            if (berhasil) {
                Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();
                finish(); // Kembali ke LoginActivity
            } else {
                Toast.makeText(this, "Registrasi Gagal / Email sudah ada!", Toast.LENGTH_SHORT).show();
            }
        });

        // Klik teks "login" balik ke halaman depan
        tvSignInLink.setOnClickListener(v -> finish());
    }
}