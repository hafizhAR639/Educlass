package com.belajar.myapplication.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment untuk menampilkan Daftar Topik (Materi) di dalam suatu Mata Pelajaran.
 * Dilengkapi dengan filter gaya belajar (Visual, Audio, Kinestik).
 */
public class MateriFragment extends Fragment {

    private String subjectId, subjectName;
    private RecyclerView rvTopics;
    private AdapterTopic adapter;
    private final List<ModelTopic> allTopicList = new ArrayList<>();
    private final List<ModelTopic> filteredTopicList = new ArrayList<>();
    private FirebaseFirestore db;
    private ChipGroup chipGroupFilters;
    private ImageView ivHeaderBg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Menggunakan layout detail materi admin
        View view = inflater.inflate(R.layout.admin_fragment_materi_detail, container, false);
        
        // Ambil data Mata Pelajaran yang dikirim lewat bundle/argument
        if (getArguments() != null) {
            subjectId = getArguments().getString("subject_id");
            subjectName = getArguments().getString("subject_name");
        }

        // Inisialisasi UI Header
        View header = view.findViewById(R.id.header_admin_detail);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        ivHeaderBg = header.findViewById(R.id.iv_shared_header_bg);

        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);
        UIUtils.setHeaderImage(subjectName, ivHeaderBg);

        // Listener tombol kembali
        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        db = FirebaseFirestore.getInstance();
        
        // Setup RecyclerView Daftar Topik (List)
        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterTopic(filteredTopicList, true, true, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                // Admin detail logic if any
            }
            @Override
            public void onEditClick(ModelTopic topic) {
                Toast.makeText(getContext(), "Edit: " + topic.getJudul(), Toast.LENGTH_SHORT).show();
            }
        });
        rvTopics.setAdapter(adapter);

        // Setup Filter bar (Chips)
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        setupFilters();

        // Tombol Tambah Materi
        view.findViewById(R.id.btn_add_materi).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur tambah materi sedang dikembangkan", Toast.LENGTH_SHORT).show();
        });

        fetchTopics(); // Ambil data dari Firestore

        return view;
    }

    /**
     * Mengatur logika ketika filter gaya belajar dipilih.
     */
    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            applyFilter(checkedIds.get(0));
        });
    }

    /**
     * Menyaring list materi berdasarkan filter yang aktif.
     */
    private void applyFilter(int chipId) {
        filteredTopicList.clear();
        if (chipId == R.id.chip_semua) {
            filteredTopicList.addAll(allTopicList);
        } else {
            // Logika filter (Contoh: hanya tampilkan jika memiliki konten V/A/K)
            filteredTopicList.addAll(allTopicList);
            // Anda dapat menambahkan logika pencocokan field khusus di sini di masa mendatang
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Mengambil data topik yang memiliki subject_id yang cocok dari database.
     */
    private void fetchTopics() {
        FirebaseHelper.fetchTopics(subjectId, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                processTopics(task.getResult());
            }
        });
    }

    private void fetchTopicsFallback() {
        // Shared
    }

    /**
     * Memetakan hasil Firestore ke objek ModelTopic dan menampilkannya di list.
     */
    private void processTopics(com.google.firebase.firestore.QuerySnapshot result) {
        allTopicList.clear();
        for (QueryDocumentSnapshot document : result) {
            ModelTopic topic = document.toObject(ModelTopic.class);
            topic.setTopic_id(document.getId());
            allTopicList.add(topic);
        }
        // Urutkan berdasarkan field order
        allTopicList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));

        applyFilter(chipGroupFilters.getCheckedChipId());
    }
}
