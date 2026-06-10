package com.belajar.myapplication.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class UserHomeFragment extends Fragment {

    private RecyclerView rvSubjects, rvPopular;
    private EditText etSearch;
    private CardView cardHighlight;
    private TextView tvHighlightTitle, tvHighlightProgress, tvGreeting;
    private LinearProgressIndicator pbHighlight;
    private ImageView ivProfile, ivHeaderHighlight;
    private FirebaseFirestore db;
    private com.google.firebase.firestore.ListenerRegistration subjectsListenerHome;
    private String userJurusan;
    private final List<ModelSubject> subjectListHome = new ArrayList<>();
    private AdapterSubject subjectAdapterHome;

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
        ivHeaderHighlight = view.findViewById(R.id.iv_header_highlight);
        db = FirebaseFirestore.getInstance();

        // Navigasi ke Profil
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> navigateToProfile());
        }
        if (tvGreeting != null) {
            tvGreeting.setOnClickListener(v -> navigateToProfile());
        }

        setupInitialAdapters();
        loadUserData();
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
                        .replace(R.id.layout_fragment_container, new UserNotificationFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        View btnPremium = view.findViewById(R.id.iv_premium_badge);
        if (btnPremium != null) {
            btnPremium.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new UserPremiumFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void setupInitialAdapters() {
        if (subjectAdapterHome == null) {
            subjectAdapterHome = new AdapterSubject(subjectListHome, R.layout.user_item_subject_home, (subject, v) -> {
                db.collection("subjects").document(subject.getSubject_id())
                        .update("access_count", com.google.firebase.firestore.FieldValue.increment(1));

                UserTopicListFragment fragment = new UserTopicListFragment();
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
        }
        
        rvSubjects.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSubjects.setAdapter(subjectAdapterHome);

        if (rvPopular.getAdapter() == null) {
            AdapterTopic emptyAdapter = new AdapterTopic(new ArrayList<>(), false, false, AdapterTopic.TYPE_POPULAR, (topic, isLocked) -> {
                 UserTopicListFragment fragment = new UserTopicListFragment();
                 Bundle bundle = new Bundle();
                 bundle.putString("subject_id", topic.getSubject_id());
                 bundle.putString("subject_name", "Materi");
                 fragment.setArguments(bundle);
                 getParentFragmentManager().beginTransaction()
                         .replace(R.id.layout_fragment_container, fragment)
                         .addToBackStack(null)
                         .commit();
            });
            rvPopular.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvPopular.setAdapter(emptyAdapter);
        }
    }

    private void navigateToProfile() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.layout_fragment_container, new UserProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                String name = doc.getString("nama");
                String photoUrl = doc.getString("photoUrl");
                userJurusan = doc.getString("jurusan");

                if (name != null && tvGreeting != null) {
                    tvGreeting.setText("Hallo, " + name + " 👋");
                }

                if (photoUrl != null && !photoUrl.isEmpty() && ivProfile != null) {
                    Glide.with(this).load(photoUrl).into(ivProfile);
                }

                if (ivHeaderHighlight != null) {
                    ivHeaderHighlight.setImageResource(R.drawable.user_img_header_math);
                    ivHeaderHighlight.setAlpha(0.35f);
                    if ("IPS".equalsIgnoreCase(userJurusan)) {
                        cardHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#1E293B"));
                    } else {
                        cardHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#2D3E50"));
                    }
                }

                setupHighlight();
                setupSubjects();
                setupPopularTopics();
            }
        }).addOnFailureListener(e -> {
            if (isAdded()) {
                setupSubjects();
                setupPopularTopics();
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
                    pbHighlight.setVisibility(View.VISIBLE);
                    pbHighlight.setProgress(p);

                    cardHighlight.setOnClickListener(v -> {
                        UserContentFragment fragment = new UserContentFragment();
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
                    cardHighlight.setVisibility(View.VISIBLE);
                    tvHighlightTitle.setText("Ayo mulai belajar hari ini! 🚀");
                    tvHighlightProgress.setText("Pilih materi untuk memulai");
                    pbHighlight.setVisibility(View.GONE);
                    cardHighlight.setOnClickListener(v -> rvSubjects.smoothScrollToPosition(0));
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
                if (s.length() > 2) performSearch(s.toString());
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
                        if (!list.isEmpty()) showSearchResults(list);
                    });
        });
    }

    private void showSearchResults(List<ModelTopic> results) {
        AdapterTopic adapter = new AdapterTopic(results, false, false, AdapterTopic.TYPE_POPULAR, (topic, isLocked) -> {
            UserContentFragment fragment = new UserContentFragment();
            Bundle bundle = new Bundle();
            bundle.putString("topic_id", topic.getTopic_id());
            bundle.putString("topic_judul", topic.getJudul());
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.layout_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvPopular.setAdapter(adapter);
    }

    private void setupSubjects() {
        if (getContext() == null) return;
        final android.content.Context context = getContext().getApplicationContext();

        // 1. Proactive Fallback (If no data in 3 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (subjectListHome.isEmpty() && isAdded()) {
                List<ModelSubject> fallback = new ArrayList<>();
                addFallbackSubjects(fallback);
                processSubjectsToAdapter(fallback);
            }
        }, 3000);

        // 2. Room Data
        new Thread(() -> {
            try {
                List<ModelSubject> cachedSubjects = com.belajar.myapplication.data.local.AppDatabase.getInstance(context).subjectDao().getAllSubjects();
                if (cachedSubjects != null && !cachedSubjects.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> processSubjectsToAdapter(cachedSubjects));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 3. Firestore Sync
        if (subjectsListenerHome != null) subjectsListenerHome.remove();
        subjectsListenerHome = db.collection("subjects").orderBy("access_count", Query.Direction.DESCENDING).addSnapshotListener((result, error) -> {
            if (error != null || result == null) {
                if (subjectListHome.isEmpty() && isAdded()) {
                    List<ModelSubject> fallback = new ArrayList<>();
                    addFallbackSubjects(fallback);
                    processSubjectsToAdapter(fallback);
                }
                return;
            }
            List<ModelSubject> remoteList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : result) {
                ModelSubject s = doc.toObject(ModelSubject.class);
                s.setSubject_id(doc.getId());
                remoteList.add(s);
            }
            if (!remoteList.isEmpty()) {
                new Thread(() -> {
                    try {
                        com.belajar.myapplication.data.local.AppDatabase.getInstance(context).subjectDao().insertSubjects(remoteList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> processSubjectsToAdapter(remoteList));
                }
            }
        });
    }

    private void processSubjectsToAdapter(List<ModelSubject> list) {
        if (!isAdded()) return;
        android.util.Log.d("UserHomeFragment", "Processing subjects. Count: " + list.size() + ", Jurusan: " + userJurusan);
        subjectListHome.clear();
        for (ModelSubject s : list) {
            if (userJurusan != null && !userJurusan.isEmpty()) {
                String subJurusan = s.getJurusan();
                String nama = s.getNama() != null ? s.getNama().toLowerCase() : "";
                
                // Specific exclusion to keep IPA/IPS clean
                if (userJurusan.equalsIgnoreCase("IPS")) {
                    if (nama.contains("matematika") || nama.contains("math") || nama.contains("fisika") || nama.contains("kimia") || nama.contains("biologi")) continue;
                } else if (userJurusan.equalsIgnoreCase("IPA")) {
                    if (nama.contains("ekonomi") || nama.contains("geografi") || nama.contains("sosiologi") || nama.contains("sejarah")) continue;
                }

                if (subJurusan == null || subJurusan.isEmpty() || subJurusan.equalsIgnoreCase(userJurusan)) {
                    subjectListHome.add(s);
                }
            } else {
                // If no major selected, show everything
                subjectListHome.add(s);
            }
        }
        
        android.util.Log.d("UserHomeFragment", "Filtered subjects count: " + subjectListHome.size());
        
        if (subjectAdapterHome != null) {
            subjectAdapterHome.notifyDataSetChanged();
            // Force layout update if empty to show something is happening
            if (subjectListHome.isEmpty()) {
                rvSubjects.setVisibility(View.GONE);
            } else {
                rvSubjects.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addFallbackSubjects(List<ModelSubject> list) {
        if ("IPS".equalsIgnoreCase(userJurusan)) {
            addSubject(list, "Geografi", "shared_ic_degree", "#D1E9FF");
            addSubject(list, "Ekonomi", "shared_ic_stats", "#CEF7FF");
            addSubject(list, "Sosiologi", "shared_ic_people", "#D1FADF");
            addSubject(list, "Sejarah", "shared_ic_book", "#E9D7FE");
        } else {
            addSubject(list, "Matematika", "shared_ic_math", "#D1E9FF");
            addSubject(list, "Kimia", "shared_ic_chem", "#CEF7FF");
            addSubject(list, "Biologi", "shared_ic_bio", "#D1FADF");
            addSubject(list, "Fisika", "shared_ic_phys", "#E9D7FE");
        }
    }

    private void addSubject(List<ModelSubject> list, String name, String icon, String color) {
        ModelSubject s = new ModelSubject();
        s.setNama(name);
        s.setIcon_name(icon);
        s.setColor_hex(color);
        s.setSubject_id(name.toLowerCase() + "_fallback");
        list.add(s);
    }

    private void setupPopularTopics() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Proactive Fallback for Popular Topics
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (rvPopular != null && (rvPopular.getAdapter() == null || rvPopular.getAdapter().getItemCount() == 0)) {
                List<ModelTopic> fallback = new ArrayList<>();
                if ("IPS".equalsIgnoreCase(userJurusan)) addFallbackIpsTopics(fallback);
                else addFallbackIpaTopics(fallback);
                updatePopularTopicsAdapter(fallback);
            }
        }, 4000);

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String userStyle = userDoc.getString("gaya_belajar");
            if (userStyle == null) userStyle = "Visual";
            final String finalStyle = userStyle.toLowerCase();
            final String major = userDoc.getString("jurusan");

            db.collection("subjects").whereEqualTo("jurusan", major).get().addOnSuccessListener(subjectResult -> {
                java.util.Set<String> majorSubjectIds = new java.util.HashSet<>();
                for (QueryDocumentSnapshot subDoc : subjectResult) majorSubjectIds.add(subDoc.getId());

                db.collection("topics").orderBy("views_count", Query.Direction.DESCENDING).limit(50).get().addOnSuccessListener(result -> {
                    List<ModelTopic> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : result) {
                        ModelTopic topic = doc.toObject(ModelTopic.class);
                        topic.setTopic_id(doc.getId());
                        if (majorSubjectIds.contains(topic.getSubject_id())) {
                            List<String> styles = topic.getLearning_styles();
                            if (styles != null && styles.contains(finalStyle)) list.add(topic);
                        }
                        if (list.size() >= 4) break;
                    }
                    if (list.isEmpty()) {
                        if ("IPS".equalsIgnoreCase(major)) addFallbackIpsTopics(list);
                        else addFallbackIpaTopics(list);
                    }
                    updatePopularTopicsAdapter(list);
                });
            });
        });
    }

    private void updatePopularTopicsAdapter(List<ModelTopic> list) {
        if (!isAdded()) return;
        AdapterTopic adapter = new AdapterTopic(list, false, false, AdapterTopic.TYPE_POPULAR, (topic, isLocked) -> {
            UserTopicListFragment fragment = new UserTopicListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", topic.getSubject_id());
            bundle.putString("subject_name", "Materi");
            fragment.setArguments(bundle);
            getParentFragmentManager().beginTransaction().replace(R.id.layout_fragment_container, fragment).addToBackStack(null).commit();
        });
        rvPopular.setAdapter(adapter);
    }

    private void addFallbackIpaTopics(List<ModelTopic> list) {
        ModelTopic t1 = new ModelTopic(); t1.setJudul("Hukum Newton"); t1.setViews_count("25rb views"); t1.setSubject_id("phys_fallback"); list.add(t1);
        ModelTopic t2 = new ModelTopic(); t2.setJudul("Sistem Reproduksi"); t2.setViews_count("20rb views"); t2.setSubject_id("bio_fallback"); list.add(t2);
    }

    private void addFallbackIpsTopics(List<ModelTopic> list) {
        ModelTopic t1 = new ModelTopic(); t1.setJudul("Letak Geografis"); t1.setViews_count("12rb views"); t1.setSubject_id("geo_fallback"); list.add(t1);
        ModelTopic t2 = new ModelTopic(); t2.setJudul("Dasar Ekonomi"); t2.setViews_count("10rb views"); t2.setSubject_id("econ_fallback"); list.add(t2);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subjectsListenerHome != null) subjectsListenerHome.remove();
    }
}
