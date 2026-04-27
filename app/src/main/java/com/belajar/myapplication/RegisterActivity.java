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

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    ImageView icEye1, icEye2;
    Button btnSignUp;
    TextView tvSignInLink;
    boolean pass1Visible = false, pass2Visible = false;

    // DatabaseHelper db; // Migrated to Firebase
    private Authenticate authManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // db = new DatabaseHelper(this); // Disabled
        authManager = new Authenticate();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        icEye1 = findViewById(R.id.icEye1);
        icEye2 = findViewById(R.id.icEye2);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignInLink = findViewById(R.id.tvSignUp);

        icEye1.setOnClickListener(v -> {
            pass1Visible = !pass1Visible;
            etPassword.setTransformationMethod(pass1Visible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye1.setImageResource(pass1Visible ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            etPassword.setSelection(etPassword.length());
        });

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

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Semua field harus diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPwd)) {
                etConfirmPassword.setError("Password tidak cocok!");
                return;
            }

            progressDialog.show();
            authManager.registerUser(email, password, fullName, new Authenticate.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Registrasi Gagal: " + message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSignInLink.setOnClickListener(v -> finish());
    }
}
