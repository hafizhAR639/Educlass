package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;

/**
 * Fragment untuk menampilkan data statistik penggunaan aplikasi.
 * Menampilkan ringkasan aktivitas melalui grafik dan angka.
 */
public class StatistikFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout statistik
        View view = inflater.inflate(R.layout.admin_fragment_statistik, container, false);

        // Setup Header menggunakan include
        View header = view.findViewById(R.id.header_admin_statistik);
        android.widget.TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Statistik");

        // Inisialisasi tombol kembali dari shared header
        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    // Kembali ke fragment sebelumnya (Home)
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }

        return view;
    }
}
