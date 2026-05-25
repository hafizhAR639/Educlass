package com.belajar.myapplication.admin;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminHomeFragment extends Fragment {

    private TextView tvGreeting, tvTotalUser, tvTotalModul;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvTotalUser = view.findViewById(R.id.tv_total_user);
        tvTotalModul = view.findViewById(R.id.tv_total_modul);
        db = FirebaseFirestore.getInstance();

        // Tombol Tambah Modul
        view.findViewById(R.id.rl_btn_tambah_modul).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, new ModulFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Tombol Kelola User
        view.findViewById(R.id.rl_btn_kelola_user).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ManageUserActivity.class));
        });

        // Tombol Statistik
        view.findViewById(R.id.rl_btn_statistik).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.admin_fragment_container, new StatistikFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        loadAdminData();
        fetchStats();

        return view;
    }

    private void loadAdminData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    if (nama != null && tvGreeting != null) {
                        tvGreeting.setText(nama + " 👋");
                    }
                }
            });
        }
    }

    private void fetchStats() {
        // Fetch total users
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalUser != null) {
                tvTotalUser.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // Fetch total modules (subjects)
        db.collection("subjects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalModul != null) {
                tvTotalModul.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });
    }
}
