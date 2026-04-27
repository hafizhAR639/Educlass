package com.belajar.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    ImageView icEye;
    Button btnLogin;
    TextView tvSignUp;
    boolean passwordVisible = false;
    
    // DatabaseHelper db; // Migrated to Firebase
    private Authenticate authManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // db = new DatabaseHelper(this); // Disabled
        authManager = new Authenticate();
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        icEye = findViewById(R.id.icEye);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        icEye.setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                icEye.setImageResource(R.drawable.ic_eye_off);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                icEye.setImageResource(R.drawable.ic_eye);
            }
            etPassword.setSelection(etPassword.length());
        });

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();
            authManager.loginUser(email, password, new Authenticate.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}
