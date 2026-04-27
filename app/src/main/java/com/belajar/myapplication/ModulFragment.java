package com.belajar.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.models.Subject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ModulFragment extends Fragment {

    private RecyclerView rvSubjects;
    private SubjectAdapter adapter;
    private List<Subject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modul, container, false);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> requireActivity().onBackPressed());

        rvSubjects = view.findViewById(R.id.rv_subjects_modul);
        rvSubjects.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new SubjectAdapter(subjectList, false);
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchSubjects();

        return view;
    }

    private void fetchSubjects() {
        android.util.Log.d("FirebaseDebug", "ModulFragment: Fetching subjects...");
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
            Subject subject = document.toObject(Subject.class);
            subject.setSubject_id(document.getId());
            subjectList.add(subject);
        }
        subjectList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        adapter.notifyDataSetChanged();
    }
}
