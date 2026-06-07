package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminProfileFragment extends Fragment {

    private TextView tvAdminName;
    private ImageView ivProfile;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_profile, container, false);

        tvAdminName = view.findViewById(R.id.tv_admin_name);
        ivProfile = view.findViewById(R.id.iv_profile_pic);
        db = FirebaseFirestore.getInstance();

        // Back Button
        view.findViewById(R.id.btn_back_profile).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Edit Profile
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getContext(), AdminEditProfileActivity.class));
        });

        // Change Password
        view.findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getContext(), AdminChangePasswordActivity.class));
        });

        // Notifications
        view.findViewById(R.id.btn_notifications).setOnClickListener(v -> {
            startActivity(new android.content.Intent(getContext(), AdminNotificationActivity.class));
        });

        // Logout
        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            android.content.Intent intent = new android.content.Intent(getContext(), com.belajar.myapplication.auth.AuthLoginActivity.class);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        loadAdminData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAdminData();
    }

    private void loadAdminData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    
                    if (nama != null && tvAdminName != null) {
                        tvAdminName.setText(nama);
                    }
                    
                    if (photoUrl != null && !photoUrl.isEmpty() && ivProfile != null && isAdded()) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.shared_profile_pic).into(ivProfile);
                    }
                }
            });
        }
    }
}
