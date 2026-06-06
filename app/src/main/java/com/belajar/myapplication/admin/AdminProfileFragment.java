package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileFragment extends Fragment {

    private TextView tvAdminName;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_profile, container, false);

        tvAdminName = view.findViewById(R.id.tv_admin_name);
        db = FirebaseFirestore.getInstance();

        // Back Button
        view.findViewById(R.id.btn_back_profile).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Edit Profile
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur Edit Profil akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        // Change Password
        view.findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur Ubah Password akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        // Notifications
        view.findViewById(R.id.btn_notifications).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur Notifikasi Admin akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        loadAdminData();

        return view;
    }

    private void loadAdminData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    if (nama != null && tvAdminName != null) {
                        tvAdminName.setText(nama);
                    }
                }
            });
        }
    }
}
