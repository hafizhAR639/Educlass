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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
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
        adapter = new AdapterSubject(subjectList, true);
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchSubjects();
        loadUserData();

        return view;
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
        Log.d("FirebaseDebug", "Fetching subjects...");
        db.collection("subjects")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      fetchSubjectsFallback();
                  } else {
                      processSubjects(task.getResult());
                  }
              } else {
                  fetchSubjectsFallback();
              }
          });
    }

    private void fetchSubjectsFallback() {
        db.collection("subject")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && !task.getResult().isEmpty()) {
                  processSubjects(task.getResult());
              }
          });
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
