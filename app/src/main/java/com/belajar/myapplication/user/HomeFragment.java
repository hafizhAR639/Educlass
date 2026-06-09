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
    private ImageView ivProfile, ivHeaderHighlight;
    private FirebaseFirestore db;
    private String userJurusan;

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

        // Navigasi ke Profil saat foto atau nama diklik
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        if (tvGreeting != null) {
            tvGreeting.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

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
                userJurusan = doc.getString("jurusan");

                if (name != null && tvGreeting != null) {
                    tvGreeting.setText("Hallo, " + name + " 👋");
                }

                if (photoUrl != null && !photoUrl.isEmpty() && ivProfile != null) {
                    Glide.with(this).load(photoUrl).into(ivProfile);
                }

                // Update Header Image based on major
                if (ivHeaderHighlight != null) {
                    // Use the same stylish header image for both to maintain visual consistency
                    ivHeaderHighlight.setImageResource(R.drawable.user_img_header_math);
                    ivHeaderHighlight.setAlpha(0.35f);
                    
                    // If IPS, we can use a slightly different tint or background color if desired
                    if ("IPS".equalsIgnoreCase(userJurusan)) {
                        cardHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#1E293B")); // Darker slate for IPS
                    } else {
                        cardHighlight.setCardBackgroundColor(android.graphics.Color.parseColor("#2D3E50")); // Original dark blue
                    }
                }

                // Load data that depends on major
                setupHighlight();
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
                    // Show a welcome card instead of hiding it, so the layout stays consistent
                    cardHighlight.setVisibility(View.VISIBLE);
                    tvHighlightTitle.setText("Ayo mulai belajar hari ini! 🚀");
                    tvHighlightProgress.setText("Pilih materi untuk memulai");
                    pbHighlight.setVisibility(View.GONE);
                    
                    cardHighlight.setOnClickListener(v -> {
                        // Scroll to subjects or show a toast
                        rvSubjects.smoothScrollToPosition(0);
                    });
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
                
                // Filter berdasarkan jurusan user
                if (userJurusan != null && !userJurusan.isEmpty()) {
                    String subJurusan = s.getJurusan();
                    String nama = s.getNama() != null ? s.getNama().toLowerCase() : "";
                    
                    // Specific exclusion to avoid mixing IPA/IPS incorrectly if DB tags are missing
                    if (userJurusan.equalsIgnoreCase("IPS")) {
                        if (nama.contains("matematika") || nama.contains("math") || nama.contains("fisika") || nama.contains("kimia") || nama.contains("biologi")) {
                            continue;
                        }
                    } else if (userJurusan.equalsIgnoreCase("IPA")) {
                        if (nama.contains("ekonomi") || nama.contains("geografi") || nama.contains("sosiologi") || nama.contains("sejarah")) {
                            continue;
                        }
                    }

                    if (subJurusan == null || subJurusan.isEmpty() || subJurusan.equalsIgnoreCase(userJurusan)) {
                        list.add(s);
                    }
                } else {
                    list.add(s);
                }
            }

            if (list.isEmpty()) {
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
        if ("IPS".equalsIgnoreCase(userJurusan)) {
            // 1. Geografi (Blue) - Consistent with Admin
            ModelSubject geo = new ModelSubject();
            geo.setNama("Geografi");
            geo.setIcon_name("shared_ic_degree");
            geo.setColor_hex("#D1E9FF");
            list.add(geo);

            // 2. Ekonomi (Cyan) - Consistent with Admin
            ModelSubject econ = new ModelSubject();
            econ.setNama("Ekonomi");
            econ.setIcon_name("shared_ic_stats");
            econ.setColor_hex("#CEF7FF");
            list.add(econ);

            // 3. Sosiologi (Green) - Consistent with Admin
            ModelSubject sos = new ModelSubject();
            sos.setNama("Sosiologi");
            sos.setIcon_name("shared_ic_people");
            sos.setColor_hex("#D1FADF");
            list.add(sos);

            // 4. Sejarah (Purple) - Consistent with Admin
            ModelSubject hist = new ModelSubject();
            hist.setNama("Sejarah");
            hist.setIcon_name("shared_ic_book");
            hist.setColor_hex("#E9D7FE");
            list.add(hist);
        } else {
            // IPA Fallback (matching the image)
            ModelSubject math = new ModelSubject();
            math.setNama("Matematika");
            math.setIcon_name("shared_ic_math");
            math.setColor_hex("#D1E9FF");
            list.add(math);

            ModelSubject chem = new ModelSubject();
            chem.setNama("Kimia");
            chem.setIcon_name("shared_ic_chem");
            chem.setColor_hex("#CEF7FF");
            list.add(chem);

            ModelSubject bio = new ModelSubject();
            bio.setNama("Biologi");
            bio.setIcon_name("shared_ic_bio");
            bio.setColor_hex("#D1FADF");
            list.add(bio);

            ModelSubject phys = new ModelSubject();
            phys.setNama("Fisika");
            phys.setIcon_name("shared_ic_phys");
            phys.setColor_hex("#E9D7FE");
            list.add(phys);
        }
    }

    private void setupPopularTopics() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            String userStyle = userDoc.getString("gaya_belajar");
            if (userStyle == null) userStyle = "Visual";
            final String finalStyle = userStyle.toLowerCase();
            final String major = userDoc.getString("jurusan");

            // Step 1: Get all subjects that belong to the user's major
            db.collection("subjects")
                    .whereEqualTo("jurusan", major)
                    .get()
                    .addOnSuccessListener(subjectResult -> {
                        java.util.Set<String> majorSubjectIds = new java.util.HashSet<>();
                        for (QueryDocumentSnapshot subDoc : subjectResult) {
                            majorSubjectIds.add(subDoc.getId());
                        }

                        // Step 2: Fetch topics and filter strictly by majorSubjectIds
                        db.collection("topics")
                                .orderBy("views_count", Query.Direction.DESCENDING)
                                .limit(100)
                                .get()
                                .addOnSuccessListener(result -> {
                                    List<ModelTopic> list = new ArrayList<>();
                                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : result) {
                                        ModelTopic topic = doc.toObject(ModelTopic.class);
                                        topic.setTopic_id(doc.getId());
                                        
                                        String title = topic.getJudul() != null ? topic.getJudul().toLowerCase() : "";

                                        // Safety check: even if subject_id matches, exclude based on title keywords if they conflict with major
                                        if ("IPS".equalsIgnoreCase(major)) {
                                            if (title.contains("matematika") || title.contains("math") || title.contains("aljabar") || 
                                                title.contains("fisika") || title.contains("kimia") || title.contains("biologi") || 
                                                title.contains("turunan") || title.contains("integral") || title.contains("sel") || title.contains("atom")) {
                                                continue;
                                            }
                                        } else if ("IPA".equalsIgnoreCase(major)) {
                                            if (title.contains("ekonomi") || title.contains("geografi") || title.contains("sosiologi") || 
                                                title.contains("sejarah") || title.contains("pasar") || title.contains("peta") || title.contains("sosial")) {
                                                continue;
                                            }
                                        }

                                        // CRITICAL FILTER: Must belong to a subject of the user's major OR match the title keywords for that major
                                        boolean isMajorSubject = majorSubjectIds.contains(topic.getSubject_id());
                                        
                                        if (isMajorSubject) {
                                            List<String> styles = topic.getLearning_styles();
                                            if (styles != null && styles.contains(finalStyle)) {
                                                list.add(topic);
                                            }
                                        }
                                        if (list.size() >= 4) break;
                                    }

                                    // Fallbacks if no data found for this major
                                    if (list.isEmpty()) {
                                        if ("IPS".equalsIgnoreCase(major)) {
                                            addFallbackIpsTopics(list);
                                        } else {
                                            addFallbackIpaTopics(list);
                                        }
                                    }

                                    AdapterTopic adapter = new AdapterTopic(list, false, false, AdapterTopic.TYPE_POPULAR, new AdapterTopic.OnTopicClickListener() {
                                        @Override
                                        public void onTopicClick(ModelTopic topic, boolean isLocked) {
                                            // Directly open content or navigate to subject
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
        });
    }

    private void addFallbackIpaTopics(List<ModelTopic> list) {
        ModelTopic t1 = new ModelTopic();
        t1.setJudul("Hukum Newton");
        t1.setViews_count("25rb views");
        t1.setSubject_id("phys_id");
        list.add(t1);

        ModelTopic t2 = new ModelTopic();
        t2.setJudul("Sistem Reproduksi");
        t2.setViews_count("20rb views");
        t2.setSubject_id("bio_id");
        list.add(t2);
    }

    private void addFallbackIpsTopics(List<ModelTopic> list) {
        ModelTopic t1 = new ModelTopic();
        t1.setJudul("Letak Geografis Indonesia");
        t1.setViews_count("12rb views");
        t1.setSubject_id("geo_id"); // Dummy ID
        list.add(t1);

        ModelTopic t2 = new ModelTopic();
        t2.setJudul("Konsep Dasar Ekonomi");
        t2.setViews_count("10rb views");
        t2.setSubject_id("econ_id"); // Dummy ID
        list.add(t2);
        
        ModelTopic t3 = new ModelTopic();
        t3.setJudul("Interaksi Sosial");
        t3.setViews_count("8rb views");
        t3.setSubject_id("sos_id"); // Dummy ID
        list.add(t3);
        
        ModelTopic t4 = new ModelTopic();
        t4.setJudul("Zaman Praaksara");
        t4.setViews_count("15rb views");
        t4.setSubject_id("hist_id"); // Dummy ID
        list.add(t4);
    }
}
