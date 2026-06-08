package com.belajar.myapplication.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.belajar.myapplication.auth.AuthLoginActivity;
import com.belajar.myapplication.auth.AuthManager;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private AuthManager authManager;
    private FirebaseFirestore db;
    private TextView tvName, tvInitial, tvEmail, tvJoinDate, tvJurusan, tvStyle, tvStatus;
    private ImageView ivProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_activiry_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authManager = new AuthManager();
        db = FirebaseFirestore.getInstance();

        // Header Views
        tvName = view.findViewById(R.id.tv_profile_name);
        tvInitial = view.findViewById(R.id.tv_profile_initial);
        ivProfilePhoto = view.findViewById(R.id.iv_profile_photo);
        tvStatus = view.findViewById(R.id.tv_profile_status);
        
        // Info Views
        tvEmail = view.findViewById(R.id.tv_info_email);
        tvJoinDate = view.findViewById(R.id.tv_info_join_date);
        tvJurusan = view.findViewById(R.id.tv_info_jurusan);
        tvStyle = view.findViewById(R.id.tv_info_gaya_belajar);
        
        // Buttons
        View btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        View btnChangePassword = view.findViewById(R.id.btn_change_password);
        View btnPrivacy = view.findViewById(R.id.btn_privacy);
        View btnLogout = view.findViewById(R.id.btn_logout_card);
        View btnBack = view.findViewById(R.id.btn_back_profile);

        loadUserData();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                                .replace(R.id.layout_fragment_container, new HomeFragment())
                                .commit();
                    }
                }
            });
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
            });
        }

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), PrivacyActivity.class));
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                authManager.logout();
                Intent intent = new Intent(getActivity(), AuthLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = authManager.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            if (tvEmail != null) tvEmail.setText(email);

            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && isAdded()) {
                    String nama = documentSnapshot.getString("nama");
                    String gayaBelajar = documentSnapshot.getString("gaya_belajar");
                    String jurusan = documentSnapshot.getString("jurusan");
                    String joinDate = documentSnapshot.getString("join_date");
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    boolean isPremium = Boolean.TRUE.equals(documentSnapshot.getBoolean("isPremium"));

                    if (nama != null) {
                        if (tvName != null) tvName.setText(nama);
                        if (tvInitial != null && !nama.isEmpty()) {
                            tvInitial.setText(String.valueOf(nama.charAt(0)).toUpperCase());
                        }
                    }

                    if (photoUrl != null && !photoUrl.isEmpty() && ivProfilePhoto != null) {
                        ivProfilePhoto.setVisibility(View.VISIBLE);
                        if (tvInitial != null) tvInitial.setVisibility(View.GONE);
                        Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.user_ic_profile)
                                .into(ivProfilePhoto);
                    } else {
                        if (ivProfilePhoto != null) ivProfilePhoto.setVisibility(View.GONE);
                        if (tvInitial != null) tvInitial.setVisibility(View.VISIBLE);
                    }

                    if (tvStatus != null) {
                        tvStatus.setText(isPremium ? "Premium Account" : "Free Account");
                    }
                    
                    if (jurusan != null && tvJurusan != null) {
                        tvJurusan.setText("Jurusan " + jurusan);
                    }
                    
                    if (gayaBelajar != null && tvStyle != null) {
                        tvStyle.setText("Gaya Belajar " + gayaBelajar);
                    }
                    
                    if (joinDate != null && tvJoinDate != null) {
                        tvJoinDate.setText("Bergabung " + joinDate);
                    }
                }
            });
        }
    }
}
