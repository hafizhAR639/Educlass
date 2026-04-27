package com.belajar.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class VisualFragment extends Fragment {
    private FirebaseFirestore db;
    private TextView tvJudulMateri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visual, container, false);
        
        db = FirebaseFirestore.getInstance();
        tvJudulMateri = view.findViewById(R.id.r8p2j2jpedem); // Based on your XML

        Bundle bundle = getArguments();
        if (bundle != null) {
            String subjectId = bundle.getString("subject_id");
            if (subjectId != null) {
                loadMateri(subjectId);
            }
        }

        return view;
    }

    private void loadMateri(String subjectId) {
        db.collection("topics")
            .whereEqualTo("subject_id", subjectId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String judul = document.getString("judul");
                        tvJudulMateri.setText(judul);
                        break; // Display first found for now
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal memuat materi", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
