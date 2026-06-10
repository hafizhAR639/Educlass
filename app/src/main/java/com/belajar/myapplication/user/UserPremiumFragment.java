package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserPremiumFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_premium, container, false);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        // Logika Tombol Upgrade
        view.findViewById(R.id.btnUpgrade).setOnClickListener(v -> {
            // STEP 1: Simulasi Pembayaran (Nantinya diganti Midtrans SDK)
            // Logikanya: App panggil Backend -> Backend panggil Midtrans -> App terima SnapToken
            Toast.makeText(getContext(), "Menghubungkan ke Midtrans...", Toast.LENGTH_SHORT).show();
            
            // STEP 2: Dummy Success (Hanya untuk testing UI saat ini)
            processPremiumUpgrade();
        });

        return view;
    }

    /**
     * Fungsi untuk mengubah status user menjadi Premium di Firestore.
     * Nantinya panggil fungsi ini HANYA jika Midtrans memberikan status 'settlement'.
     */
    private void processPremiumUpgrade() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("isPremium", true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Selamat! Anda sekarang member Premium", Toast.LENGTH_LONG).show();
                        if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
                    });
        }
    }
}
