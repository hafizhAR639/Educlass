package com.belajar.myapplication.user;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PremiumFragment extends Fragment {

    private MaterialCardView cardMonthly, cardAnnual;
    private TextView tvTotalPrice, tvTotalLabel;
    private boolean isAnnualSelected = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_premium, container, false);

        initViews(view);
        setupSelection();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        view.findViewById(R.id.btnUpgrade).setOnClickListener(v -> {
            String packageType = isAnnualSelected ? "Tahunan" : "Bulanan";
            String price = isAnnualSelected ? "Rp 399.000" : "Rp 49.000";
            
            // Logic for future Payment Gateway Integration (e.g., Midtrans)
            // 1. Call Backend to create transaction
            // 2. Open Payment UI
            // 3. Handle callback
            
            Toast.makeText(getContext(), "Memproses pembayaran " + packageType + " (" + price + ")...", Toast.LENGTH_SHORT).show();
            processPremiumUpgrade();
        });

        return view;
    }

    private void initViews(View v) {
        cardMonthly = v.findViewById(R.id.cardBulanan);
        cardAnnual = v.findViewById(R.id.cardTahunan);
        tvTotalPrice = v.findViewById(R.id.tvTotalPrice);
        tvTotalLabel = v.findViewById(R.id.tvTotalLabel);
        
        checkCurrentStatus(v);
    }

    private void checkCurrentStatus(View v) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isPremium")) && isAdded()) {
                    com.google.android.material.button.MaterialButton btn = v.findViewById(R.id.btnUpgrade);
                    if (btn != null) {
                        btn.setText("Layanan Premium Aktif");
                        btn.setEnabled(false);
                    }
                }
            });
        }
    }

    private void setupSelection() {
        cardMonthly.setOnClickListener(v -> selectPackage(false));
        cardAnnual.setOnClickListener(v -> selectPackage(true));
        
        // Initial state
        updateUI();
    }

    private void selectPackage(boolean annual) {
        isAnnualSelected = annual;
        updateUI();
    }

    private void updateUI() {
        if (isAnnualSelected) {
            // Annual selected
            cardAnnual.setStrokeColor(Color.parseColor("#2563EB"));
            cardAnnual.setStrokeWidth(convertDpToPx(2.5f));
            cardAnnual.setCardElevation(convertDpToPx(8f));

            cardMonthly.setStrokeColor(Color.parseColor("#E2E8F0"));
            cardMonthly.setStrokeWidth(convertDpToPx(1f));
            cardMonthly.setCardElevation(convertDpToPx(2f));

            tvTotalLabel.setText("Total (Tahunan)");
            tvTotalPrice.setText("Rp 399.000");
        } else {
            // Monthly selected
            cardMonthly.setStrokeColor(Color.parseColor("#2563EB"));
            cardMonthly.setStrokeWidth(convertDpToPx(2.5f));
            cardMonthly.setCardElevation(convertDpToPx(8f));

            cardAnnual.setStrokeColor(Color.parseColor("#E2E8F0"));
            cardAnnual.setStrokeWidth(convertDpToPx(1f));
            cardAnnual.setCardElevation(convertDpToPx(2f));

            tvTotalLabel.setText("Total (Bulanan)");
            tvTotalPrice.setText("Rp 49.000");
        }
    }

    private int convertDpToPx(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void processPremiumUpgrade() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("isPremium", true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Selamat! Anda sekarang member Premium", Toast.LENGTH_LONG).show();
                        if (isAdded() && getParentFragmentManager() != null) {
                            getParentFragmentManager().popBackStack();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Gagal melakukan upgrade: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
