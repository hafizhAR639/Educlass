package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Aktivitas untuk mengelola daftar pengguna (User).
 * Admin dapat melihat dan mengatur data pengguna aplikasi.
 */
public class ManageUserActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private AdapterUserAdmin adapter;
    private final List<ModelUser> userList = new ArrayList<>();
    private FirebaseFirestore db;
    private EditText etSearch;
    private TextView btnSemua, btnAktif, btnTidakAktif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_user);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        ImageView btnBack = findViewById(R.id.btn_back_manage);
        rvUsers = findViewById(R.id.rv_manage_users);
        etSearch = findViewById(R.id.et_search_user);
        btnSemua = findViewById(R.id.btn_filter_semua);
        btnAktif = findViewById(R.id.btn_filter_aktif);
        btnTidakAktif = findViewById(R.id.btn_filter_tidak_aktif);

        // Tombol Kembali: Menutup Activity
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupRecyclerView();
        setupSearch();
        setupFilters();
        fetchUsers();
    }

    private void setupRecyclerView() {
        if (rvUsers == null) return;
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterUserAdmin(userList);
        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.setSearchQuery(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        btnSemua.setOnClickListener(v -> {
            updateFilterUI(btnSemua);
            adapter.setFilter("Semua");
        });

        btnAktif.setOnClickListener(v -> {
            updateFilterUI(btnAktif);
            adapter.setFilter("Aktif");
        });

        btnTidakAktif.setOnClickListener(v -> {
            updateFilterUI(btnTidakAktif);
            adapter.setFilter("Tidak Aktif");
        });
    }

    private void updateFilterUI(TextView selected) {
        // Reset all to default style
        resetFilterButtonStyle(btnSemua);
        resetFilterButtonStyle(btnAktif);
        resetFilterButtonStyle(btnTidakAktif);

        // Set active style to selected
        selected.setBackgroundResource(R.drawable.shared_bg_nav_indicator);
        selected.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        selected.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetFilterButtonStyle(TextView btn) {
        btn.setBackgroundResource(R.drawable.bg_card_white_r20);
        btn.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        btn.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void fetchUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelUser user = document.toObject(ModelUser.class);
                            
                            // Default to active if field is missing or null in Firestore
                            if (document.get("active") == null) {
                                user.setActive(true);
                            }

                            userList.add(user);
                        }
                        adapter.updateData(userList);
                    } else {
                        Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
