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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.models.Topic;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MateriFragment extends Fragment {

    private String subjectId, subjectName;
    private RecyclerView rvTopics;
    private TopicAdapter adapter;
    private List<Topic> topicList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materi, container, false);
        
        if (getArguments() != null) {
            subjectId = getArguments().getString("subject_id");
            subjectName = getArguments().getString("subject_name");
        }

        TextView tvTitle = view.findViewById(R.id.tv_subject_title); // Needs to be added to XML
        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // In existing XML, topics are hardcoded. We should replace them with a RecyclerView.
        // For now, let's find a container or replace the hardcoded part.
        // Looking at fragment_materi.xml, there is a LinearLayout for "List Materi Section".
        // Let's assume we add a RecyclerView with ID rv_topics there.
        
        db = FirebaseFirestore.getInstance();
        
        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TopicAdapter(topicList);
        rvTopics.setAdapter(adapter);

        fetchTopics();

        return view;
    }

    private void fetchTopics() {
        if (subjectId == null) {
            android.util.Log.e("FirebaseDebug", "SubjectId is null, cannot fetch topics");
            return;
        }
        
        android.util.Log.d("FirebaseDebug", "Fetching topics from 'topics' where subject_id == " + subjectId);
        db.collection("topics")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      android.util.Log.w("FirebaseDebug", "No topics found for subject_id: " + subjectId + " in 'topics'. Trying 'topic'...");
                      fetchTopicsFallback();
                  } else {
                      processTopics(task.getResult());
                  }
              } else {
                  android.util.Log.e("FirebaseDebug", "Error fetching 'topics'", task.getException());
                  fetchTopicsFallback();
              }
          });
    }

    private void fetchTopicsFallback() {
        db.collection("topic")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  if (task.getResult().isEmpty()) {
                      android.util.Log.w("FirebaseDebug", "No topics found in 'topic' for subject_id: " + subjectId);
                  } else {
                      processTopics(task.getResult());
                  }
              } else {
                  android.util.Log.e("FirebaseDebug", "Error fetching 'topic'", task.getException());
              }
          });
    }

    private void processTopics(com.google.firebase.firestore.QuerySnapshot result) {
        topicList.clear();
        android.util.Log.d("FirebaseDebug", "Fetched " + result.size() + " topics");
        for (QueryDocumentSnapshot document : result) {
            Topic topic = document.toObject(Topic.class);
            topic.setTopic_id(document.getId());
            android.util.Log.d("FirebaseDebug", "Topic Doc ID: " + document.getId() + " Data: " + document.getData());
            topicList.add(topic);
        }
        adapter.notifyDataSetChanged();
    }
}
