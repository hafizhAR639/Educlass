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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.local.AppDatabase;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.shared.AdapterSubject;
import com.belajar.myapplication.shared.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private RecyclerView rvSubjects;
    private AdapterSubject adapter;
    private final List<ModelSubject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvGreeting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        rvSubjects = view.findViewById(R.id.rv_subjects_home);
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        
        adapter = new AdapterSubject(subjectList, R.layout.user_item_subject_home, (subject, v) -> {
            MateriFragment fragment = new MateriFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", subject.getSubject_id());
            bundle.putString("subject_name", subject.getNama());
            fragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.layout_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvSubjects.setAdapter(adapter);

        view.findViewById(R.id.iv_premium_badge).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.layout_fragment_container, new PremiumFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        db = FirebaseFirestore.getInstance();
        fetchSubjectsHybrid();
        loadUserData();

        return view;
    }

    private void fetchSubjectsHybrid() {
        // 1. Ambil dari lokal (Room)
        List<ModelSubject> cached = AppDatabase.getInstance(getContext()).subjectDao().getAllSubjects();
        if (!cached.isEmpty()) {
            updateList(cached);
        }

        // 2. Ambil dari online (Firestore)
        FirebaseHelper.fetchSubjects(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                saveAndRefresh(task.getResult());
            }
        });
    }

    private void saveAndRefresh(com.google.firebase.firestore.QuerySnapshot result) {
        List<ModelSubject> remote = new ArrayList<>();
        for (QueryDocumentSnapshot document : result) {
            ModelSubject s = document.toObject(ModelSubject.class);
            s.setSubject_id(document.getId());
            remote.add(s);
        }
        AppDatabase.getInstance(getContext()).subjectDao().insertSubjects(remote);
        updateList(remote);
    }

    private void updateList(List<ModelSubject> list) {
        subjectList.clear();
        subjectList.addAll(list);
        subjectList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        adapter.notifyDataSetChanged();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    if (nama != null && tvGreeting != null) {
                        tvGreeting.setText("Hallo, " + nama + " 👋 ");
                    }
                }
            });
        }
    }

    private void fetchSubjects() {
        fetchSubjectsHybrid();
    }

    private void fetchSubjectsFallback() {
        fetchSubjectsHybrid();
    }

    private void processSubjects(com.google.firebase.firestore.QuerySnapshot result) {
        saveAndRefresh(result);
    }
}
