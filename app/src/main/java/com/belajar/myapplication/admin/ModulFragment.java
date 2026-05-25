package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.local.AppDatabase;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.shared.AdapterSubject;
import com.belajar.myapplication.shared.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment untuk mengelola Daftar Mata Pelajaran (Modul) di sisi Admin.
 * Prinsip KISS: Logika fetching data dimiripkan dengan sisi User agar semua data (termasuk Matematika) muncul.
 */
public class ModulFragment extends Fragment {

    private AdapterSubject adapterIpa, adapterIps;
    private final List<ModelSubject> subjectListIpa = new ArrayList<>();
    private final List<ModelSubject> subjectListIps = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_materi, container, false);

        View header = view.findViewById(R.id.header_admin_modul);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Modul Pelajaran");

        // 1. Setup RecyclerView IPA (Grid 3 Kolom)
        RecyclerView rvSubjectsIpa = view.findViewById(R.id.rv_subjects_ipa);
        rvSubjectsIpa.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapterIpa = new AdapterSubject(subjectListIpa, R.layout.admin_item_subject_admin, (subject, v) -> navigateToMateri(subject));
        rvSubjectsIpa.setAdapter(adapterIpa);

        // 2. Setup RecyclerView IPS (Grid 3 Kolom)
        RecyclerView rvSubjectsIps = view.findViewById(R.id.rv_subjects_ips);
        rvSubjectsIps.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapterIps = new AdapterSubject(subjectListIps, R.layout.admin_item_subject_admin, (subject, v) -> navigateToMateri(subject));
        rvSubjectsIps.setAdapter(adapterIps);

        // 3. Tombol Tambah Mata Pelajaran (IPA & IPS)
        view.findViewById(R.id.btn_add_subject_ipa).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddSubjectActivity.class));
        });
        view.findViewById(R.id.btn_add_subject_ips).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddSubjectActivity.class));
        });

        // 4. Tombol Kembali
        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
            });
        }

        db = FirebaseFirestore.getInstance();
        fetchSubjectsHybrid(); // Ambil data dengan strategi hybrid (Lokal + Online)

        return view;
    }

    private void navigateToMateri(ModelSubject subject) {
        MateriFragment fragment = new MateriFragment();
        Bundle bundle = new Bundle();
        bundle.putString("subject_id", subject.getSubject_id());
        bundle.putString("subject_name", subject.getNama());
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.admin_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Strategi Hybrid: Cek lokal dulu agar cepat, lalu update dari Firebase di background.
     */
    private void fetchSubjectsHybrid() {
        // 1. Ambil data dari Room (Lokal)
        List<ModelSubject> cachedSubjects = AppDatabase.getInstance(getContext()).subjectDao().getAllSubjects();
        if (!cachedSubjects.isEmpty()) {
            processListToAdapter(cachedSubjects); // Langsung tampilkan yang ada di memori HP
        }

        // 2. Tetap ambil data terbaru dari Firestore
        FirebaseHelper.fetchSubjects(task -> {
            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                saveAndProcessRemoteData(task.getResult());
            }
        });
    }

    private void saveAndProcessRemoteData(com.google.firebase.firestore.QuerySnapshot result) {
        List<ModelSubject> remoteList = new ArrayList<>();
        for (QueryDocumentSnapshot document : result) {
            ModelSubject subject = document.toObject(ModelSubject.class);
            subject.setSubject_id(document.getId());
            remoteList.add(subject);
        }

        // Simpan ke Room agar besok bisa dibuka secara offline
        AppDatabase.getInstance(getContext()).subjectDao().insertSubjects(remoteList);
        
        // Update tampilan UI
        processListToAdapter(remoteList);
    }

    private void processListToAdapter(List<ModelSubject> list) {
        subjectListIpa.clear();
        subjectListIps.clear();

        for (ModelSubject subject : list) {
            String jurusan = subject.getJurusan();
            if (jurusan != null && jurusan.equalsIgnoreCase("ips")) {
                subjectListIps.add(subject);
            } else {
                subjectListIpa.add(subject);
            }
        }

        // Urutkan
        subjectListIpa.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        subjectListIps.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));

        adapterIpa.notifyDataSetChanged();
        adapterIps.notifyDataSetChanged();
    }

    /**
     * Method lama (deprecedated) diganti oleh fetchSubjectsHybrid
     */
    private void fetchSubjectsData() {
        fetchSubjectsHybrid();
    }
}
