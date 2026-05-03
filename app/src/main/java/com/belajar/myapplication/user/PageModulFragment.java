package com.belajar.myapplication.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PageModulFragment extends Fragment {

    private RecyclerView rvSubjects;
    private AdapterSubject adapter;
    private final List<ModelSubject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_modul, container, false);

        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        rvSubjects = view.findViewById(R.id.rv_subjects_modul);
        rvSubjects.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new AdapterSubject(subjectList, false);
        rvSubjects.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchSubjects();

        return view;
    }

    private void fetchSubjects() {
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
