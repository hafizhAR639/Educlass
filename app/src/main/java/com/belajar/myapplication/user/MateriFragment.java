package com.belajar.myapplication.user;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.shared.AdapterTopic;
import com.belajar.myapplication.shared.FirebaseHelper;
import com.belajar.myapplication.shared.UIUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MateriFragment extends Fragment {

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

        View header = view.findViewById(R.id.header_materi);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        ImageView ivHeaderBg = header.findViewById(R.id.iv_shared_header_bg);
        
        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);
        UIUtils.setHeaderImage(subjectName, ivHeaderBg);

        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        db = FirebaseFirestore.getInstance();
        
        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Cek status premium untuk Adapter
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                boolean isPremium = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isPremium"));
                setupAdapter(isPremium);
            });
        } else {
            setupAdapter(false);
        }

        fetchTopics();

        return view;
    }

    private void setupAdapter(boolean isPremium) {
        adapter = new AdapterTopic(topicList, false, isPremium, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                if (isLocked) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.layout_fragment_container, new PremiumFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    ContentFragment fragment = new ContentFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("topic_id", topic.getTopic_id());
                    bundle.putString("topic_judul", topic.getJudul());
                    fragment.setArguments(bundle);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.layout_fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
        rvTopics.setAdapter(adapter);
    }

    private void fetchTopics() {
        FirebaseHelper.fetchTopics(subjectId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                processTopics(task.getResult());
            }
        });
    }

    private void fetchTopicsFallback() {
        // Handled by FirebaseHelper
    }

    private void processTopics(com.google.firebase.firestore.QuerySnapshot result) {
        topicList.clear();
        for (QueryDocumentSnapshot document : result) {
            ModelTopic topic = document.toObject(ModelTopic.class);
            topic.setTopic_id(document.getId());
            topicList.add(topic);
        }
        // Urutkan berdasarkan field order
        topicList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));

        adapter.notifyDataSetChanged();
    }
}
