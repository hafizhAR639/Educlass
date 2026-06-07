package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.local.AppDatabase;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.shared.AdapterSubject;
import com.belajar.myapplication.shared.AdapterTopic;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvSubjects, rvPopularTopics;
    private AdapterSubject subjectAdapter;
    private AdapterTopic popularTopicAdapter;
    private final List<ModelSubject> subjectList = new ArrayList<>();
    private final List<ModelTopic> popularTopicList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvGreeting, tvHighlightTitle, tvHighlightProgressText;
    private View viewProgressFill;
    private ImageView ivUserProfile;
    private String lastTopicId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_home, container, false);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvHighlightTitle = view.findViewById(R.id.tv_highlight_title);
        tvHighlightProgressText = view.findViewById(R.id.tv_highlight_progress_text);
        viewProgressFill = view.findViewById(R.id.view_progress_fill);
        ivUserProfile = view.findViewById(R.id.iv_user_profile);

        // Continue Button Logic
        view.findViewById(R.id.btn_continue).setOnClickListener(v -> {
            if (lastTopicId != null) {
                navigateToContent(lastTopicId);
            }
        });

        // Dashboard Card also clickable
        view.findViewById(R.id.layout_highlight_card).setOnClickListener(v -> {
            if (lastTopicId != null) {
                navigateToContent(lastTopicId);
            }
        });

        // Subjects RecyclerView
        rvSubjects = view.findViewById(R.id.rv_subjects_home);
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        subjectAdapter = new AdapterSubject(subjectList, R.layout.user_item_subject_home, (subject, v) -> {
            incrementSubjectAccess(subject.getSubject_id());
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
        rvSubjects.setAdapter(subjectAdapter);

        // Popular Topics RecyclerView
        rvPopularTopics = view.findViewById(R.id.rv_popular_topics);
        rvPopularTopics.setLayoutManager(new GridLayoutManager(getContext(), 2));
        popularTopicAdapter = new AdapterTopic(popularTopicList, false, true, AdapterTopic.TYPE_POPULAR, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                navigateToContent(topic.getTopic_id());
            }
        });
        rvPopularTopics.setAdapter(popularTopicAdapter);

        view.findViewById(R.id.layout_search_bar).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.layout_fragment_container, com.belajar.myapplication.admin.SearchFragment.newInstance(""))
                        .addToBackStack(null)
                        .commit();
            }
        });

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
        loadUserProgress();
        fetchPopularTopics();

        return view;
    }

    private void navigateToContent(String topicId) {
        ContentFragment fragment = new ContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("topic_id", topicId);
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchSubjectsHybrid() {
        if (getContext() == null) return;
        // 1. Local
        List<ModelSubject> cached = AppDatabase.getInstance(getContext()).subjectDao().getAllSubjects();
        if (!cached.isEmpty()) {
            updateList(cached);
        }

        // 2. Remote
        db.collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ModelSubject> remote = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ModelSubject s = document.toObject(ModelSubject.class);
                        s.setSubject_id(document.getId());
                        remote.add(s);
                    }
                    remote.sort((a, b) -> {
                        int res = Long.compare(b.getAccess_count(), a.getAccess_count());
                        if (res == 0) return Long.compare(a.getOrder(), b.getOrder());
                        return res;
                    });
                    
                    AppDatabase.getInstance(getContext()).subjectDao().insertSubjects(remote);
                    updateList(remote);
                });
    }

    private void updateList(List<ModelSubject> list) {
        subjectList.clear();
        subjectList.addAll(list);
        if (subjectAdapter != null) subjectAdapter.notifyDataSetChanged();
    }

    private void incrementSubjectAccess(String subjectId) {
        if (subjectId == null) return;
        db.collection("subjects").document(subjectId)
                .update("access_count", com.google.firebase.firestore.FieldValue.increment(1));
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (getContext() == null || !isAdded()) return;
                if (documentSnapshot.exists()) {
                    String nama = documentSnapshot.getString("nama");
                    String photoUrl = documentSnapshot.getString("photoUrl");
                    if (nama != null && tvGreeting != null) {
                        tvGreeting.setText("Hallo, " + nama + " 👋 ");
                    }
                    if (photoUrl != null && !photoUrl.isEmpty() && ivUserProfile != null && getContext() != null) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.user_ic_profile).into(ivUserProfile);
                    }
                }
            });
        }
    }

    private void loadUserProgress() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (getContext() == null || !isAdded()) return;
                
                String lastId = null;
                long progress = 0;
                
                if (documentSnapshot.exists()) {
                    lastId = documentSnapshot.getString("last_topic_id");
                    Long p = documentSnapshot.getLong("last_topic_progress");
                    if (p != null) progress = p;
                }

                if (lastId != null) {
                    this.lastTopicId = lastId;
                    final long finalProgress = progress;
                    db.collection("topics").document(lastId).get().addOnSuccessListener(topicDoc -> {
                        if (topicDoc.exists()) {
                            String title = topicDoc.getString("judul");
                            updateDashboardUI(title, finalProgress);
                        } else {
                            setDefaultDashboard();
                        }
                    });
                } else {
                    setDefaultDashboard();
                }
            });
        }
    }

    private void setDefaultDashboard() {
        // Placeholder "Hukum Newton"
        db.collection("topics").whereEqualTo("judul", "Hukum Newton").limit(1).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                com.google.firebase.firestore.DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                this.lastTopicId = doc.getId();
                updateDashboardUI("Hukum Newton", 65); // Just a placeholder progress
            } else {
                updateDashboardUI("Hukum Newton", 65);
            }
        });
    }

    private void updateDashboardUI(String title, long progress) {
        if (tvHighlightTitle != null) tvHighlightTitle.setText(title);
        if (tvHighlightProgressText != null) tvHighlightProgressText.setText(progress + "% completed");
        if (viewProgressFill != null) {
            viewProgressFill.post(() -> {
                ViewGroup.LayoutParams params = viewProgressFill.getLayoutParams();
                if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).weight = progress;
                    viewProgressFill.setLayoutParams(params);
                }
            });
        }
    }

    private void fetchPopularTopics() {
        db.collection("topics")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getContext() == null || !isAdded()) return;
                    List<ModelTopic> allTopics = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ModelTopic topic = doc.toObject(ModelTopic.class);
                        topic.setTopic_id(doc.getId());
                        allTopics.add(topic);
                    }
                    
                    allTopics.sort((a, b) -> Long.compare(b.getViews_count_long(), a.getViews_count_long()));

                    popularTopicList.clear();
                    for (int i = 0; i < Math.min(allTopics.size(), 6); i++) {
                        popularTopicList.add(allTopics.get(i));
                    }

                    if (popularTopicAdapter != null) popularTopicAdapter.notifyDataSetChanged();
                });
    }
}
