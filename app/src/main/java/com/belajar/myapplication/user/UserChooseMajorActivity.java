package com.belajar.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.belajar.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserChooseMajorActivity extends AppCompatActivity {

    private View cardIpa, cardIps;
    private ImageView ivCheckIpa, ivCheckIps;
    private MaterialButton btnContinue;
    private TextView tvGreeting;
    private String selectedMajor = null;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_major);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        tvGreeting = findViewById(R.id.tv_greeting);
        cardIpa = findViewById(R.id.card_ipa);
        cardIps = findViewById(R.id.card_ips);
        ivCheckIpa = findViewById(R.id.iv_check_ipa);
        ivCheckIps = findViewById(R.id.iv_check_ips);
        btnContinue = findViewById(R.id.btn_continue);

        loadUserData();

        cardIpa.setOnClickListener(v -> selectMajor("IPA"));
        cardIps.setOnClickListener(v -> selectMajor("IPS"));

        btnContinue.setOnClickListener(v -> saveSelectedMajor());
    }

    private void loadUserData() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    if (nama != null && !nama.isEmpty()) {
                        tvGreeting.setText("Halo, " + nama + "! 👋");
                    }
                }
            });
        }
    }

    private void selectMajor(String major) {
        selectedMajor = major;
        
        if (major.equals("IPA")) {
            cardIpa.setSelected(true);
            cardIps.setSelected(false);
            ivCheckIpa.setVisibility(View.VISIBLE);
            ivCheckIps.setVisibility(View.GONE);
        } else {
            cardIpa.setSelected(false);
            cardIps.setSelected(true);
            ivCheckIpa.setVisibility(View.GONE);
            ivCheckIps.setVisibility(View.VISIBLE);
        }

        btnContinue.setEnabled(true);
    }

    private void saveSelectedMajor() {
        if (selectedMajor == null || user == null) return;

        btnContinue.setEnabled(false);
        db.collection("users").document(user.getUid())
                .update("jurusan", selectedMajor)
                .addOnSuccessListener(aVoid -> {
                    startActivity(new Intent(UserChooseMajorActivity.this, UserMainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, "Gagal menyimpan pilihan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
