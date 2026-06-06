package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeFragment extends Fragment {

    private TextView tvGreeting, tvTotalUser, tvTotalModul, tvActiveToday, tvAvgDuration;
    private EditText etSearch;
    private FirebaseFirestore db;
    private RecyclerView rvActivities;
    private AdapterActivity activityAdapter;
    private final List<ModelActivity> activityList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvTotalUser = view.findViewById(R.id.tv_total_user);
        tvTotalModul = view.findViewById(R.id.tv_total_modul);
        tvActiveToday = view.findViewById(R.id.tv_active_today);
        tvAvgDuration = view.findViewById(R.id.tv_avg_duration);
        etSearch = view.findViewById(R.id.et_search);
        rvActivities = view.findViewById(R.id.rv_latest_activities);
        db = FirebaseFirestore.getInstance();

        setupRecyclerView();
        setupSearch();

        // Tombol Tambah Modul
        view.findViewById(R.id.rl_btn_tambah_modul).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_admin);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_modul);
                }
            }
        });

        // Tombol Kelola User
        view.findViewById(R.id.rl_btn_kelola_user).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ManageUserActivity.class));
        });

        // Tombol Statistik
        view.findViewById(R.id.rl_btn_statistik).setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav_admin);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_chart);
                }
            }
        });

        // Tombol Notifikasi
        view.findViewById(R.id.ic_notification_bell).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur Notifikasi akan segera hadir", Toast.LENGTH_SHORT).show();
        });

        loadAdminData();
        fetchStats();
        fetchActivities();

        return view;
    }

    private void setupRecyclerView() {
        if (rvActivities == null) return;
        rvActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        activityAdapter = new AdapterActivity(activityList);
        rvActivities.setAdapter(activityAdapter);
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        navigateToSearch(query);
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private void navigateToSearch(String query) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.admin_fragment_container, SearchFragment.newInstance(query))
                    .addToBackStack(null)
                    .commit();
        }
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
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalUser != null) {
                tvTotalUser.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        db.collection("subjects").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvTotalModul != null) {
                tvTotalModul.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        db.collection("users")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (tvActiveToday != null) {
                        tvActiveToday.setText(String.valueOf(queryDocumentSnapshots.size()));
                    }
                });

        if (tvAvgDuration != null) {
            tvAvgDuration.setText("45m 15s");
        }
    }

    private void fetchActivities() {
        db.collection("admin_activities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        activityList.add(doc.toObject(ModelActivity.class));
                    }
                    if (activityAdapter != null) activityAdapter.notifyDataSetChanged();
                });
    }
}
