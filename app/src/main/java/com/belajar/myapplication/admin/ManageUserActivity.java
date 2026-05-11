package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_manage_user);

        db = FirebaseFirestore.getInstance();

        // Inisialisasi UI
        ImageView btnBack = findViewById(R.id.btn_back_manage);
        rvUsers = findViewById(R.id.rv_manage_users);

        // Tombol Kembali: Menutup Activity
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        setupRecyclerView();
        fetchUsers();
    }

    /**
     * Menyiapkan daftar user dengan layout linear (vertikal).
     */
    private void setupRecyclerView() {
        if (rvUsers == null) return;
        
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdapterUserAdmin(userList);
        rvUsers.setAdapter(adapter);
    }

    private void fetchUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ModelUser user = document.toObject(ModelUser.class);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
