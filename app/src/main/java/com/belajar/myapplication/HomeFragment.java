package com.belajar.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.models.Subject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private List<Subject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvSubjects = view.findViewById(R.id.rv_subjects_home);
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new SubjectAdapter(subjectList, true);
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchSubjects();

        return view;
    }

    private void fetchSubjects() {
        android.util.Log.d("FirebaseDebug", "Fetching subjects from collection 'subjects'...");
        db.collection("subjects")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      android.util.Log.w("FirebaseDebug", "No documents in 'subjects'. Trying 'subject'...");
                      fetchSubjectsFallback();
                  } else {
                      processSubjects(task.getResult());
                  }
              } else {
                  android.util.Log.e("FirebaseDebug", "Error fetching 'subjects'", task.getException());
                  fetchSubjectsFallback();
              }
          });
    }

    private void fetchSubjectsFallback() {
        db.collection("subject")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      android.util.Log.w("FirebaseDebug", "No documents in 'subject' either.");
                  } else {
                      processSubjects(task.getResult());
                  }
              } else {
                  android.util.Log.e("FirebaseDebug", "Error fetching 'subject'", task.getException());
              }
          });
    }

    private void processSubjects(com.google.firebase.firestore.QuerySnapshot result) {
        subjectList.clear();
        android.util.Log.d("FirebaseDebug", "Fetched " + result.size() + " documents");
        for (QueryDocumentSnapshot document : result) {
            Subject subject = document.toObject(Subject.class);
            subject.setSubject_id(document.getId());
            android.util.Log.d("FirebaseDebug", "Doc ID: " + document.getId() + " Data: " + document.getData());
            subjectList.add(subject);
        }
        subjectList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        adapter.notifyDataSetChanged();
    }
}
