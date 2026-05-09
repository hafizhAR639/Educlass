package com.belajar.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.belajar.myapplication.auth.AuthLoginActivity;
import com.belajar.myapplication.auth.AuthManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PageProfileFragment extends Fragment {

    private AuthManager authManager;
    private FirebaseFirestore db;
    private TextView tvName, tvEmail, tvStyle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menggunakan layout buatan user (user_activiry_profile.xml)
        return inflater.inflate(R.layout.user_activiry_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager();
        db = FirebaseFirestore.getInstance();

        tvName = view.findViewById(R.id.tv_profile_name);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        tvStyle = view.findViewById(R.id.tv_profile_style);
        View btnLogout = view.findViewById(R.id.btn_logout_card);

        loadUserData();

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                authManager.logout();
                Intent intent = new Intent(getActivity(), AuthLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    private void loadUserData() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            if (tvEmail != null) tvEmail.setText(email);

            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    String gayaBelajar = documentSnapshot.getString("gaya_belajar");

                    if (nama != null && tvName != null) {
                        tvName.setText(nama);
                    }
                    if (gayaBelajar != null && tvStyle != null) {
                        tvStyle.setText("Gaya Belajar " + gayaBelajar);
                    }
                }
            });
        }
    }
}
