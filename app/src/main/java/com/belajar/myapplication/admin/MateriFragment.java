package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private TextView tvStatMateri, tvStatSiswa, tvStatAvg;

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

        // Inisialisasi UI
        tvStatMateri = view.findViewById(R.id.tv_stat_materi);
        tvStatSiswa = view.findViewById(R.id.tv_stat_siswa);
        tvStatAvg = view.findViewById(R.id.tv_stat_avg);

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
                Intent intent = new Intent(getActivity(), EditTopicActivity.class);
                intent.putExtra("topic_id", topic.getTopic_id());
                intent.putExtra("subject_id", subjectId);
                intent.putExtra("subject_name", subjectName);
                intent.putExtra("topic_title", topic.getJudul());
                intent.putExtra("topic_desc", topic.getDeskripsi());
                startActivity(intent);
            }
            @Override
            public void onEditClick(View view, ModelTopic topic) {
                showTopicOptions(view, topic);
            }
        });
        rvTopics.setAdapter(adapter);

        // Setup Filter bar (Chips)
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        setupFilters();

        // Tombol Tambah Materi
        view.findViewById(R.id.btn_add_materi).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTopicActivity.class);
            intent.putExtra("subject_id", subjectId);
            startActivity(intent);
        });

        // Tombol Opsi Mapel (Titik 3)
        View btnOptions = view.findViewById(R.id.btn_subject_options);
        if (btnOptions != null) {
            btnOptions.setOnClickListener(this::showSubjectOptions);
        }

        fetchTopics(); // Ambil data dari Firestore
        fetchSubjectStats(); // Ambil data statistik

        return view;
    }

    private void fetchSubjectStats() {
        // 1. Total Siswa (Total Users)
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (tvStatSiswa != null) {
                tvStatSiswa.setText(String.valueOf(queryDocumentSnapshots.size()));
            }
        });

        // 2. Total Materi & Rata-rata (from topics collection)
        db.collection("topics")
                .whereEqualTo("subject_id", subjectId)
                .get()
                .addOnSuccessListener(result -> {
                    int count = result.size();
                    if (tvStatMateri != null) tvStatMateri.setText(String.valueOf(count));

                    if (count > 0) {
                        double totalProgress = 0;
                        for (QueryDocumentSnapshot doc : result) {
                            Long progress = doc.getLong("progress");
                            totalProgress += (progress != null ? progress : 0);
                        }
                        int avg = (int) (totalProgress / count);
                        if (tvStatAvg != null) tvStatAvg.setText(avg + "%");
                    } else {
                        if (tvStatAvg != null) tvStatAvg.setText("0%");
                    }
                });
    }

    private void showTopicOptions(View v, ModelTopic topic) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.pop_up_menu_topic, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_topic) {
                deleteTopic(topic);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteTopic(ModelTopic topic) {
        if (topic.getTopic_id() == null) return;
        db.collection("topics").document(topic.getTopic_id()).delete().addOnSuccessListener(aVoid -> {
            // Hapus juga data content agar tidak duplikasi/sampah
            db.collection("content").document(topic.getTopic_id()).delete();

            // Log Activity
            Map<String, Object> log = new HashMap<>();
            log.put("description", "Admin menghapus materi: " + topic.getJudul());
            log.put("type", "delete");
            log.put("timestamp", com.google.firebase.Timestamp.now());
            db.collection("admin_activities").add(log);

            Toast.makeText(getContext(), "Materi berhasil dihapus", Toast.LENGTH_SHORT).show();
            fetchTopics(); // Refresh list
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal menghapus materi", Toast.LENGTH_SHORT).show());
    }

    private void showSubjectOptions(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.pop_up_menu_subject, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit_subject) {
                Toast.makeText(getContext(), "Fitur Edit Mapel akan segera hadir", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_delete_subject) {
                deleteSubject();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteSubject() {
        if (subjectId == null) return;
        db.collection("subjects").document(subjectId).delete().addOnSuccessListener(aVoid -> {
            // Log Activity
            Map<String, Object> log = new HashMap<>();
            log.put("description", "Admin menghapus mapel: " + subjectName);
            log.put("type", "delete");
            log.put("timestamp", com.google.firebase.Timestamp.now());
            db.collection("admin_activities").add(log);

            Toast.makeText(getContext(), "Mata pelajaran berhasil dihapus", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Gagal menghapus mata pelajaran", Toast.LENGTH_SHORT).show());
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
        String style = "";
        
        if (chipId == R.id.chip_visual) style = "visual";
        else if (chipId == R.id.chip_audio) style = "audio";
        else if (chipId == R.id.chip_kinestik) style = "kinestetik"; // Sesuaikan ID layout dengan value DB

        if (chipId == R.id.chip_semua || style.isEmpty()) {
            filteredTopicList.addAll(allTopicList);
        } else {
            for (ModelTopic topic : allTopicList) {
                if (topic.getLearning_styles() != null && topic.getLearning_styles().contains(style)) {
                    filteredTopicList.add(topic);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Mengambil data topik yang memiliki subject_id yang cocok dari database.
     */
    private void fetchTopics() {
        db.collection("topics")
                .whereEqualTo("subject_id", subjectId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        processTopics(value);
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
