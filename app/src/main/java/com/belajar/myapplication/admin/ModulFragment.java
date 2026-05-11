package com.belajar.myapplication.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
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

        // 1. Setup RecyclerView IPA (Grid 3 Kolom)
        RecyclerView rvSubjectsIpa = view.findViewById(R.id.rv_subjects_ipa);
        rvSubjectsIpa.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapterIpa = new AdapterSubject(subjectListIpa);
        rvSubjectsIpa.setAdapter(adapterIpa);

        // 2. Setup RecyclerView IPS (Grid 3 Kolom)
        RecyclerView rvSubjectsIps = view.findViewById(R.id.rv_subjects_ips);
        rvSubjectsIps.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapterIps = new AdapterSubject(subjectListIps);
        rvSubjectsIps.setAdapter(adapterIps);

        // 3. Tombol Tambah Mata Pelajaran (IPA & IPS)
        view.findViewById(R.id.btn_add_subject_ipa).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddSubjectActivity.class));
        });
        view.findViewById(R.id.btn_add_subject_ips).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddSubjectActivity.class));
        });

        // 4. Tombol Kembali
        ImageView btnBack = view.findViewById(R.id.btn_back_modul);
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getSupportFragmentManager().popBackStack();
        });

        db = FirebaseFirestore.getInstance();
        fetchSubjectsData(); // Ambil data dari Firebase

        return view;
    }

    /**
     * Mengambil data dari Firestore. Logika ini sama dengan sisi User agar data tidak ada yang tertinggal.
     */
    private void fetchSubjectsData() {
        db.collection("subjects")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        processData(task.getResult());
                    } else {
                        // Mencoba koleksi cadangan jika "subjects" kosong
                        db.collection("subject").get().addOnCompleteListener(t -> {
                            if (t.isSuccessful() && t.getResult() != null) processData(t.getResult());
                        });
                    }
                });
    }

    /**
     * Memproses data dan membagi ke dalam kategori IPA atau IPS.
     */
    private void processData(com.google.firebase.firestore.QuerySnapshot result) {
        subjectListIpa.clear();
        subjectListIps.clear();
        
        for (QueryDocumentSnapshot document : result) {
            ModelSubject subject = document.toObject(ModelSubject.class);
            subject.setSubject_id(document.getId());
            
            String jurusan = subject.getJurusan();
            // Jika jurusan adalah IPS, masukkan ke list IPS. 
            // Selain itu (IPA atau Matematika yang kosong/null), masukkan ke list IPA agar tetap muncul di layar.
            if (jurusan != null && jurusan.equalsIgnoreCase("ips")) {
                subjectListIps.add(subject);
            } else {
                subjectListIpa.add(subject);
            }
        }
        
        // Urutkan berdasarkan urutan yang sudah ditentukan di database
        subjectListIpa.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        subjectListIps.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        
        // Perbarui tampilan RecyclerView
        adapterIpa.notifyDataSetChanged();
        adapterIps.notifyDataSetChanged();
    }
}
