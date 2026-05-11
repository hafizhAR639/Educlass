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
        TextView tvTitle = view.findViewById(R.id.tv_subject_title);
        ivHeaderBg = view.findViewById(R.id.iv_subject_header_bg);

        if (tvTitle != null && subjectName != null) tvTitle.setText(subjectName);
        updateHeaderImage(subjectName); // Sesuaikan gambar header berdasarkan nama mapel

        // Listener tombol kembali
        View btnBack = view.findViewById(R.id.btn_back);
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
        adapter = new AdapterTopic(filteredTopicList);
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
     * Memperbarui gambar background header sesuai dengan mata pelajaran.
     */
    private void updateHeaderImage(String name) {
        if (name == null || ivHeaderBg == null) return;
        
        int resId = R.drawable.shared_bg_header_blue; // Default biru polos
        String lowName = name.toLowerCase();
        
        if (lowName.contains("matematika")) resId = R.drawable.user_img_header_math;
        // Tambahkan kondisi gambar lain di sini jika aset sudah tersedia
        
        ivHeaderBg.setImageResource(resId);
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
        if (subjectId == null) return;
        
        db.collection("topics")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && task.getResult() != null) {
                  if (task.getResult().isEmpty()) {
                      fetchTopicsFallback(); // Coba nama koleksi lain jika gagal
                  } else {
                      processTopics(task.getResult());
                  }
              } else {
                  fetchTopicsFallback();
              }
          });
    }

    private void fetchTopicsFallback() {
        db.collection("topic")
          .whereEqualTo("subject_id", subjectId)
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                  processTopics(task.getResult());
              }
          });
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
        applyFilter(chipGroupFilters.getCheckedChipId());
    }
}
