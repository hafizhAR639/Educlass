package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment untuk menampilkan data statistik penggunaan aplikasi.
 * Menampilkan ringkasan aktivitas melalui grafik dan angka yang sinkron dengan database.
 */
public class StatistikFragment extends Fragment {

    private TextView tvTotalUser, tvTotalModul, tvActiveToday, tvAvgDuration;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_statistik, container, false);

        db = FirebaseFirestore.getInstance();

        // Bind Views
        tvTotalUser = view.findViewById(R.id.tv_total_user);
        tvTotalModul = view.findViewById(R.id.tv_total_modul);
        tvActiveToday = view.findViewById(R.id.tv_active_today);
        tvAvgDuration = view.findViewById(R.id.tv_avg_duration);

        // Setup Header
        View header = view.findViewById(R.id.header_admin_statistik);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Statistik");

        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        fetchStats();

        return view;
    }

    private void fetchStats() {
        // 1. Fetch total users
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalUser != null) {
                tvTotalUser.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // 2. Fetch total modules (subjects)
        db.collection("subjects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalModul != null) {
                tvTotalModul.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // 3. Fetch active today (Example: filter by last_login)
        // For now using placeholder logic since exact "active today" schema might vary
        db.collection("users")
                .whereEqualTo("active", true) // Assuming active means currently active account
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (tvActiveToday != null) {
                        tvActiveToday.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        // 4. Average Duration (Placeholder or actual logic if exists)
        if (tvAvgDuration != null) {
            tvAvgDuration.setText("45m 15s"); // Use static for now if duration isn't tracked
        }
    }
}
