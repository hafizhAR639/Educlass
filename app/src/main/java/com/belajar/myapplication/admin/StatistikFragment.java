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

        // Inisialisasi tombol kembali
        ImageView btnBack = view.findViewById(R.id.btn_back_statistik);
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
