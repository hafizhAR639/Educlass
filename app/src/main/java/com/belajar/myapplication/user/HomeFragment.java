package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.shared.AdapterSubject;
import com.belajar.myapplication.shared.AdapterTopic;
import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;

public class HomeFragment extends Fragment {

    private RecyclerView rvSubjects, rvPopular;
    private EditText etSearch;
    private CardView cardHighlight;
    private TextView tvHighlightTitle, tvHighlightProgress, tvGreeting;
    private LinearProgressIndicator pbHighlight;
    private ImageView ivProfile;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSubjects = view.findViewById(R.id.rv_subjects_home);
        rvPopular = view.findViewById(R.id.rv_popular_topics);
        etSearch = view.findViewById(R.id.et_search_home);
        
        cardHighlight = view.findViewById(R.id.layout_highlight_card);
        tvHighlightTitle = view.findViewById(R.id.tv_highlight_title);
        tvHighlightProgress = view.findViewById(R.id.tv_highlight_progress_text);
        pbHighlight = view.findViewById(R.id.pb_highlight);
        
        tvGreeting = view.findViewById(R.id.tv_greeting);
        ivProfile = view.findViewById(R.id.iv_user_profile);
        db = FirebaseFirestore.getInstance();

        loadUserData();
        setupHighlight();
        setupSubjects();
        setupPopularTopics();
        setupSearch();
        
        view.findViewById(R.id.btn_continue).setOnClickListener(v -> {
            if (cardHighlight.getVisibility() == View.VISIBLE) {
                cardHighlight.performClick();
            } else {
                Toast.makeText(getContext(), "Belum ada materi terakhir", Toast.LENGTH_SHORT).show();
            }
        });

        // Notif and Premium clicks
        View btnNotif = view.findViewById(R.id.layout_notif);
        if (btnNotif != null) {
            btnNotif.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new NotificationFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        View btnPremium = view.findViewById(R.id.iv_premium_badge);
        if (btnPremium != null) {
            btnPremium.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new PremiumFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                String name = doc.getString("nama");
                String photoUrl = doc.getString("photoUrl");

                if (name != null && tvGreeting != null) {
                    tvGreeting.setText("Hallo, " + name + " 👋");
                }

                if (photoUrl != null && !photoUrl.isEmpty() && ivProfile != null) {
                    Glide.with(this).load(photoUrl).into(ivProfile);
                }
            }
        });
    }

    private void setupHighlight() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                String lastTopicId = doc.getString("last_topic_id");
                String lastTopicTitle = doc.getString("last_topic_title");
                Long progress = doc.getLong("last_topic_progress");

                if (lastTopicId != null && lastTopicTitle != null) {
                    cardHighlight.setVisibility(View.VISIBLE);
                    tvHighlightTitle.setText(lastTopicTitle);
                    int p = progress != null ? progress.intValue() : 0;
                    tvHighlightProgress.setText(p + "% completed");
                    pbHighlight.setProgress(p);

                    cardHighlight.setOnClickListener(v -> {
                        ContentFragment fragment = new ContentFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("topic_id", lastTopicId);
                        bundle.putString("topic_judul", lastTopicTitle);
                        fragment.setArguments(bundle);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.layout_fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                } else {
                    cardHighlight.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    performSearch(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String userStyle = userDoc.getString("gaya_belajar");
            if (userStyle == null) userStyle = "Visual";
            final String finalStyle = userStyle.toLowerCase();

            db.collection("topics")
                    .whereGreaterThanOrEqualTo("judul", query)
                    .whereLessThanOrEqualTo("judul", query + "\uf8ff")
                    .limit(10)
                    .get()
                    .addOnSuccessListener(result -> {
                        List<ModelTopic> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : result) {
                            ModelTopic t = doc.toObject(ModelTopic.class);
                            t.setTopic_id(doc.getId());
                            
                            List<String> styles = t.getLearning_styles();
                            if (styles != null && styles.contains(finalStyle)) {
                                list.add(t);
                            }
                        }
                        if (!list.isEmpty()) {
                            showSearchResults(list);
                        }
                    });
        });
    }

    private void showSearchResults(List<ModelTopic> results) {
        // Implementasi sederhana: ganti list terpopuler dengan hasil pencarian
        AdapterTopic adapter = new AdapterTopic(results, false, false, AdapterTopic.TYPE_POPULAR, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
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
        });
        rvPopular.setAdapter(adapter);
    }

    private void setupSubjects() {
        db.collection("subjects").orderBy("access_count", Query.Direction.DESCENDING).get().addOnSuccessListener(result -> {
            List<ModelSubject> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : result) {
                ModelSubject s = doc.toObject(ModelSubject.class);
                s.setSubject_id(doc.getId());
                list.add(s);
            }

            if (list.isEmpty()) {
                // Fallback jika DB kosong
                addFallbackSubjects(list);
            }

            AdapterSubject adapter = new AdapterSubject(list, R.layout.user_item_subject_home, (subject, v) -> {
                // Increment access count
                db.collection("subjects").document(subject.getSubject_id())
                        .update("access_count", com.google.firebase.firestore.FieldValue.increment(1));

                MateriFragment fragment = new MateriFragment();
                Bundle bundle = new Bundle();
                bundle.putString("subject_id", subject.getSubject_id());
                bundle.putString("subject_name", subject.getNama());
                fragment.setArguments(bundle);

                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            });

            rvSubjects.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvSubjects.setAdapter(adapter);
        });
    }

    private void addFallbackSubjects(List<ModelSubject> list) {
        ModelSubject math = new ModelSubject();
        math.setNama("Mathematics");
        math.setIcon_name("shared_ic_math");
        math.setColor_hex("#D1E9FF");
        list.add(math);

        ModelSubject chem = new ModelSubject();
        chem.setNama("Chemistry");
        chem.setIcon_name("shared_ic_chem");
        chem.setColor_hex("#CEF7FF");
        list.add(chem);
    }

    private void setupPopularTopics() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String userStyle = userDoc.getString("gaya_belajar");
            if (userStyle == null) userStyle = "Visual";
            final String finalStyle = userStyle.toLowerCase();

            db.collection("topics").orderBy("views_count", com.google.firebase.firestore.Query.Direction.DESCENDING).limit(20).get()
                    .addOnSuccessListener(result -> {
                        List<ModelTopic> list = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : result) {
                            ModelTopic topic = doc.toObject(ModelTopic.class);
                            topic.setTopic_id(doc.getId());
                            
                            List<String> styles = topic.getLearning_styles();
                            if (styles != null && styles.contains(finalStyle)) {
                                list.add(topic);
                            }
                        }

                        AdapterTopic adapter = new AdapterTopic(list, false, false, AdapterTopic.TYPE_POPULAR, new AdapterTopic.OnTopicClickListener() {
                            @Override
                            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                                MateriFragment fragment = new MateriFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("subject_id", topic.getSubject_id());
                                bundle.putString("subject_name", "Materi");
                                fragment.setArguments(bundle);

                                getParentFragmentManager().beginTransaction()
                                        .replace(R.id.layout_fragment_container, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        });

                        rvPopular.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        rvPopular.setAdapter(adapter);
                    });
        });
    }
}
