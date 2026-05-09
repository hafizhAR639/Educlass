package com.belajar.myapplication.auth;

import android.app.ProgressDialog;
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
import com.google.firebase.auth.FirebaseUser;

public class AuthRegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword, etConfirmPassword;
    private ImageView icEye1, icEye2;
    private Button btnSignUp;
    private TextView tvSignInLink;
    private boolean pass1Visible = false, pass2Visible = false;

    private AuthManager authManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_activity_register);

        authManager = new AuthManager();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        icEye1 = findViewById(R.id.iv_eye_toggle_1);
        icEye2 = findViewById(R.id.iv_eye_toggle_2);
        btnSignUp = findViewById(R.id.btn_register_submit);
        tvSignInLink = findViewById(R.id.tv_login_link);

        icEye1.setOnClickListener(v -> {
            pass1Visible = !pass1Visible;
            etPassword.setTransformationMethod(pass1Visible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye1.setImageResource(pass1Visible ? R.drawable.shared_ic_eye_off : R.drawable.shared_ic_eye_on);
            etPassword.setSelection(etPassword.length());
        });

        icEye2.setOnClickListener(v -> {
            pass2Visible = !pass2Visible;
            etConfirmPassword.setTransformationMethod(pass2Visible ?
                    HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
            icEye2.setImageResource(pass2Visible ? R.drawable.shared_ic_eye_off : R.drawable.shared_ic_eye_on);
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
            authManager.registerUser(email, password, fullName, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    progressDialog.dismiss();
                    Toast.makeText(AuthRegisterActivity.this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    progressDialog.dismiss();
                    Toast.makeText(AuthRegisterActivity.this, "Registrasi Gagal: " + message, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvSignInLink.setOnClickListener(v -> finish());
    }
}
