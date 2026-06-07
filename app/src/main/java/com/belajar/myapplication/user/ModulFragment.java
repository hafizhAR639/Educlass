package com.belajar.myapplication.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.shared.AdapterSubject;
import com.belajar.myapplication.shared.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ModulFragment extends Fragment {

    private RecyclerView rvSubjects;
    private AdapterSubject adapter;
    private final List<ModelSubject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_modul, container, false);

        TextView tvTitle = view.findViewById(R.id.tv_header_title);
        if (tvTitle != null) tvTitle.setText("Mata Pelajaran");

        rvSubjects = view.findViewById(R.id.rv_subjects_modul);
        rvSubjects.setLayoutManager(new GridLayoutManager(getContext(), 2));
        
        adapter = new AdapterSubject(subjectList, R.layout.user_item_subject_modul, (subject, v) -> {
            incrementSubjectAccess(subject.getSubject_id());
            // Check premium status if needed here or rely on the fact that this is User side
            // Actually the original AdapterSubject had premium check inside. 
            // I'll add a simple premium check here by reading from Firestore or passing it.
            
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnSuccessListener(doc -> {
                    boolean isPremium = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isPremium"));
                    if (!isPremium && subject.getOrder() >= 3) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.layout_fragment_container, new PremiumFragment())
                                .addToBackStack(null)
                                .commit();
                    } else {
                        navigateToMateri(subject);
                    }
                });
            } else {
                navigateToMateri(subject);
            }
        });
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchSubjects();

        return view;
    }

    private void navigateToMateri(ModelSubject subject) {
        MateriFragment fragment = new MateriFragment();
        Bundle bundle = new Bundle();
        bundle.putString("subject_id", subject.getSubject_id());
        bundle.putString("subject_name", subject.getNama());
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void incrementSubjectAccess(String subjectId) {
        if (subjectId == null) return;
        FirebaseFirestore.getInstance().collection("subjects").document(subjectId)
                .update("access_count", com.google.firebase.firestore.FieldValue.increment(1));
    }

    private void fetchSubjects() {
        FirebaseHelper.fetchSubjects(task -> {
            if (getContext() == null || !isAdded()) return;
            if (task.isSuccessful() && task.getResult() != null) {
                processSubjects(task.getResult());
            }
        });
    }

    private void fetchSubjectsFallback() {
        // Redundant with FirebaseHelper now
    }

    private void processSubjects(com.google.firebase.firestore.QuerySnapshot result) {
        subjectList.clear();
        for (QueryDocumentSnapshot document : result) {
            ModelSubject subject = document.toObject(ModelSubject.class);
            subject.setSubject_id(document.getId());
            subjectList.add(subject);
        }
        subjectList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        adapter.notifyDataSetChanged();
    }
}
