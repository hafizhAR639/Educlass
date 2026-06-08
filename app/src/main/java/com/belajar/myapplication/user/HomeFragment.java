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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        
        view.findViewById(R.id.btn_continue).setOnClickListener(v -> 
            Toast.makeText(getContext(), "Melanjutkan materi...", Toast.LENGTH_SHORT).show()
        );

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
        // Logic: Menampilkan dashboard hanya jika ada materi yang terakhir diakses
        ModelTopic lastAccessed = getLastAccessedFromDatabase();
        
        if (lastAccessed != null && lastAccessed.getProgress() > 0) {
            cardHighlight.setVisibility(View.VISIBLE);
            tvHighlightTitle.setText(lastAccessed.getJudul());
            tvHighlightProgress.setText(lastAccessed.getProgress() + "% completed");
            pbHighlight.setProgress(lastAccessed.getProgress());
        } else {
            cardHighlight.setVisibility(View.GONE);
        }
    }

    private ModelTopic getLastAccessedFromDatabase() {
        // Simulasi pengambilan data terakhir diakses
        // Return null jika user belum pernah membuka materi
        ModelTopic dummy = new ModelTopic();
        dummy.setJudul("Hukum Newton");
        dummy.setProgress(65);
        return dummy; 
    }

    private void setupSubjects() {
        List<ModelSubject> list = new ArrayList<>();
        
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

        ModelSubject bio = new ModelSubject();
        bio.setNama("Biology");
        bio.setIcon_name("shared_ic_bio");
        bio.setColor_hex("#D1FFD1");
        list.add(bio);

        ModelSubject phys = new ModelSubject();
        phys.setNama("Physics");
        phys.setIcon_name("shared_ic_phys");
        phys.setColor_hex("#E9D1FF");
        list.add(phys);

        AdapterSubject adapter = new AdapterSubject(list, R.layout.user_item_subject_home, (subject, v) -> {
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
    }

    private void setupPopularTopics() {
        List<ModelTopic> list = new ArrayList<>();

        ModelTopic aljabar = new ModelTopic();
        aljabar.setJudul("Aljabar");
        aljabar.setViews_count(10000);
        aljabar.setPremium(false);
        list.add(aljabar);

        ModelTopic pertidaksamaan = new ModelTopic();
        pertidaksamaan.setJudul("Pertidaksamaan");
        pertidaksamaan.setViews_count(15000);
        pertidaksamaan.setPremium(false);
        list.add(pertidaksamaan);
        
        ModelTopic trigonometri = new ModelTopic();
        trigonometri.setJudul("Trigonometri");
        trigonometri.setViews_count(8000);
        trigonometri.setPremium(true);
        list.add(trigonometri);

        // Mengurutkan berdasarkan views_count secara descending (Materi Terpopuler)
        Collections.sort(list, (t1, t2) -> Long.compare(t2.getViews_count_long(), t1.getViews_count_long()));

        AdapterTopic adapter = new AdapterTopic(list, false, false, AdapterTopic.TYPE_POPULAR, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                Toast.makeText(getContext(), "Buka " + topic.getJudul(), Toast.LENGTH_SHORT).show();
            }
        });

        rvPopular.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvPopular.setAdapter(adapter);
    }
}
