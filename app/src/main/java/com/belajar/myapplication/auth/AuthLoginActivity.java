package com.belajar.myapplication.auth;

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
import com.belajar.myapplication.R;
import com.belajar.myapplication.user.MainActivity;
import com.google.firebase.auth.FirebaseUser;

public class AuthLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageView icEye;
    private Button btnLogin;
    private TextView tvSignUp;
    private boolean passwordVisible = false;
    
    private AuthManager authManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_login);

        authManager = new AuthManager();
        
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
                icEye.setImageResource(R.drawable.shared_ic_eye_off);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                icEye.setImageResource(R.drawable.shared_ic_eye_on);
            }
            etPassword.setSelection(etPassword.length());
        });

        // Tombol Login: Alur dimulai di sini
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.show();

            // ALUR AUTENTIKASI (STEP 1):
            // Kita tidak melakukan login langsung di Activity, tapi "mendelegasikan" tugas ke AuthManager.
            // Ini agar kode Activity tetap bersih (prinsip KISS).
            authManager.loginUser(email, password, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    // ALUR AUTENTIKASI (STEP 3 - BERHASIL):
                    // Firebase mengembalikan objek 'user' yang berisi UID, Email, dll.
                    // Data ini SUDAH SAMA dengan yang ada di Firebase Console.
                    progressDialog.dismiss();
                    Toast.makeText(AuthLoginActivity.this, "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    
                    // Pindah ke halaman utama
                    startActivity(new Intent(AuthLoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    // ALUR AUTENTIKASI (STEP 3 - GAGAL):
                    // Jika password salah atau email tidak terdaftar, pesan error dari Firebase diterima di sini.
                    progressDialog.dismiss();
                    Toast.makeText(AuthLoginActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, AuthRegisterActivity.class));
        });
    }
}
