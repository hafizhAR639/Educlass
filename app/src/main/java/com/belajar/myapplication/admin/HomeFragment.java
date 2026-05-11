package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.belajar.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment Beranda Admin.
 * Menampilkan ringkasan statistik dan akses cepat ke fitur admin.
 */
public class HomeFragment extends Fragment {

    private TextView tvTotalUser, tvTotalModul;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Mengambil layout XML untuk Home Admin
        View view = inflater.inflate(R.layout.admin_fragment_home, container, false);

        // Inisialisasi Database dan Komponen UI
        db = FirebaseFirestore.getInstance();
        tvTotalUser = view.findViewById(R.id.tv_total_user);
        tvTotalModul = view.findViewById(R.id.tv_total_modul);
        
        LinearLayout btnManageUser = view.findViewById(R.id.rl_btn_kelola_user);
        LinearLayout btnStatistik = view.findViewById(R.id.rl_btn_statistik);
        LinearLayout btnTambahModul = view.findViewById(R.id.rl_btn_tambah_modul);

        // Ambil data terbaru dari Firestore
        fetchStats();

        // Navigasi ke halaman Kelola User (Activity baru)
        btnManageUser.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageUserActivity.class);
            startActivity(intent);
        });

        // Pindah ke Fragment Statistik
        btnStatistik.setOnClickListener(v -> loadFragment(new StatistikFragment()));

        // Pindah ke Fragment Modul (Mata Pelajaran)
        btnTambahModul.setOnClickListener(v -> loadFragment(new ModulFragment()));

        return view;
    }

    /**
     * Mengambil jumlah user dan mata pelajaran dari database Firestore secara real-time.
     */
    private void fetchStats() {
        // Ambil jumlah total dokumen di koleksi "users"
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                tvTotalUser.setText(String.valueOf(task.getResult().size()));
            }
        });

        // Ambil jumlah total dokumen di koleksi "subjects"
        db.collection("subjects").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                tvTotalModul.setText(String.valueOf(task.getResult().size()));
            } else {
                // Coba koleksi alternatif "subject" jika "subjects" kosong (fallback)
                db.collection("subject").get().addOnCompleteListener(taskFallback -> {
                    if (taskFallback.isSuccessful() && taskFallback.getResult() != null) {
                        tvTotalModul.setText(String.valueOf(taskFallback.getResult().size()));
                    }
                });
            }
        });
    }

    /**
     * Fungsi pembantu untuk navigasi antar fragment di level admin.
     */
    private void loadFragment(Fragment fragment) {
        if (getActivity() != null) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.admin_fragment_container, fragment);
            ft.addToBackStack(null); // Tambahkan ke stack agar bisa tombol 'back'
            ft.commit();
        }
    }
}
