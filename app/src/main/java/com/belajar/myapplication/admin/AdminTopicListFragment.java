package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.shared.AdapterTopic;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment untuk menampilkan daftar topik (materi) di bawah satu mata pelajaran tertentu.
 * Admin dapat menambah, mengedit, atau menghapus topik di sini.
 */
public class AdminTopicListFragment extends Fragment {

    private String subjectId, subjectName;
    private RecyclerView rvTopics;
    private AdapterTopic adapter;
    private final List<ModelTopic> allTopics = new ArrayList<>();
    private final List<ModelTopic> filteredTopics = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentFilter = "Semua";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_materi_detail, container, false);

        if (getArguments() != null) {
            subjectId = getArguments().getString("subject_id");
            subjectName = getArguments().getString("subject_name");
        }

        // Setup Header
        View header = view.findViewById(R.id.header_admin_detail);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);

        header.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView
        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        
        setupAdapter();

        // Setup Buttons
        view.findViewById(R.id.btn_add_materi).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AdminAddTopicActivity.class);
            intent.putExtra("subject_id", subjectId);
            startActivity(intent);
        });

        // Setup Chips Filter
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filters);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_semua) currentFilter = "Semua";
            else if (id == R.id.chip_visual) currentFilter = "visual";
            else if (id == R.id.chip_audio) currentFilter = "audio";
            else if (id == R.id.chip_kinestik) currentFilter = "kinestetik";
            applyFilter();
        });

        fetchTopics();

        return view;
    }

    private void setupAdapter() {
        adapter = new AdapterTopic(filteredTopics, true, true, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                openEditTopic(topic);
            }

            @Override
            public void onEditClick(View view, ModelTopic topic) {
                showPopupMenu(view, topic);
            }
        });
        rvTopics.setAdapter(adapter);
    }

    private void showPopupMenu(View view, ModelTopic topic) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Hapus");
        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Edit")) {
                openEditTopic(topic);
            } else if (item.getTitle().equals("Hapus")) {
                deleteTopic(topic);
            }
            return true;
        });
        popup.show();
    }

    private void openEditTopic(ModelTopic topic) {
        Intent intent = new Intent(getActivity(), AdminEditTopicActivity.class);
        intent.putExtra("topic_id", topic.getTopic_id());
        intent.putExtra("subject_id", subjectId);
        startActivity(intent);
    }

    private void deleteTopic(ModelTopic topic) {
        db.collection("topics").document(topic.getTopic_id()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Topic deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchTopics() {
        if (subjectId == null) return;
        db.collection("topics")
                .whereEqualTo("subject_id", subjectId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (isAdded()) Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value == null) return;

                    allTopics.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        ModelTopic topic = doc.toObject(ModelTopic.class);
                        topic.setTopic_id(doc.getId());
                        allTopics.add(topic);
                    }
                    allTopics.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
                    applyFilter();
                });
    }

    private void applyFilter() {
        filteredTopics.clear();
        for (ModelTopic topic : allTopics) {
            if (currentFilter.equals("Semua")) {
                filteredTopics.add(topic);
            } else {
                List<String> styles = topic.getLearning_styles();
                String lowFilter = currentFilter.toLowerCase();
                
                boolean matches = false;
                if (styles != null && styles.contains(lowFilter)) {
                    matches = true;
                } else {
                    // Fallback to booleans
                    if (lowFilter.contains("visual") && topic.isHas_visual()) matches = true;
                    else if (lowFilter.contains("audio") && topic.isHas_audio()) matches = true;
                    else if (lowFilter.contains("kinestetik") && topic.isHas_kinestetik()) matches = true;
                }
                
                if (matches) filteredTopics.add(topic);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        updateStats();
    }

    private void updateStats() {
        if (getView() == null) return;
        TextView tvMateri = getView().findViewById(R.id.tv_stat_materi);
        if (tvMateri != null) tvMateri.setText(String.valueOf(allTopics.size()));
        
        // Stats Siswa & Rata-rata bisa di-load dari analytics jika ada
        TextView tvSiswa = getView().findViewById(R.id.tv_stat_siswa);
        if (tvSiswa != null) tvSiswa.setText("12"); // Placeholder
        
        TextView tvAvg = getView().findViewById(R.id.tv_stat_avg);
        if (tvAvg != null) tvAvg.setText("80%"); // Placeholder
    }
}
