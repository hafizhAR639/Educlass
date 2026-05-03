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
import com.belajar.myapplication.data.models.ModelTopic;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PageMateriFragment extends Fragment {

    private String subjectId, subjectName;
    private RecyclerView rvTopics;
    private AdapterTopic adapter;
    private final List<ModelTopic> topicList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_materi, container, false);
        
        if (getArguments() != null) {
            subjectId = getArguments().getString("subject_id");
            subjectName = getArguments().getString("subject_name");
        }

        TextView tvTitle = view.findViewById(R.id.tv_subject_title);
        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);

        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        db = FirebaseFirestore.getInstance();
        
        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterTopic(topicList);
        rvTopics.setAdapter(adapter);

        fetchTopics();

        return view;
    }

    private void fetchTopics() {
        if (subjectId == null) return;
        
        db.collection("topics")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      fetchTopicsFallback();
                  } else {
                      processTopics(task.getResult());
                  }
              } else {
                  fetchTopicsFallback();
              }
          });
    }

    private void fetchTopicsFallback() {
        db.collection("topic")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && !task.getResult().isEmpty()) {
                  processTopics(task.getResult());
              }
          });
    }

    private void processTopics(com.google.firebase.firestore.QuerySnapshot result) {
        topicList.clear();
        for (QueryDocumentSnapshot document : result) {
            ModelTopic topic = document.toObject(ModelTopic.class);
            topic.setTopic_id(document.getId());
            topicList.add(topic);
        }
        adapter.notifyDataSetChanged();
    }
}
