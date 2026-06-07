package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_change_password);

        user = FirebaseAuth.getInstance().getCurrentUser();

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_save_password).setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPass = etOldPassword.getText().toString();
        String newPass = etNewPassword.getText().toString();
        String confirmPass = etConfirmPassword.getText().toString();

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
            return;
        }

        if (user != null) {
            user.updatePassword(newPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AdminChangePasswordActivity.this, "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminChangePasswordActivity.this, "Gagal mengubah password. Pastikan Anda baru saja login.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
