package com.belajar.myapplication.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.shared.AdapterSubject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class UserSubjectListFragment extends Fragment {

    private RecyclerView rvSubjects;
    private AdapterSubject adapter;
    private final List<ModelSubject> subjectList = new ArrayList<>();
    private FirebaseFirestore db;
    private ListenerRegistration subjectsListener;
    private boolean isUserPremium = false;
    private String userJurusan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_modul, container, false);
        Log.d("UserSubjectListFragment", "onCreateView called");

        TextView tvTitle = view.findViewById(R.id.tv_header_title);
        if (tvTitle != null) tvTitle.setText("Mata Pelajaran");

        rvSubjects = view.findViewById(R.id.rv_subjects_modul);
        rvSubjects.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new AdapterSubject(subjectList, R.layout.user_item_subject_modul, (subject, v) -> {
            incrementSubjectAccess(subject.getSubject_id());
            if (!isUserPremium && subject.getOrder() >= 3) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(R.id.layout_fragment_container, new UserPremiumFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                navigateToMateri(subject);
            }
        });
        rvSubjects.setAdapter(adapter);

        View btnBack = view.findViewById(R.id.btn_back_modul);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.layout_fragment_container, new UserHomeFragment())
                            .commit();
                }
            });
        }

        db = FirebaseFirestore.getInstance();
        fetchUserStatusAndSubjects();

        return view;
    }

    private void fetchUserStatusAndSubjects() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded()) {
                    isUserPremium = Boolean.TRUE.equals(doc.getBoolean("isPremium"));
                    userJurusan = doc.getString("jurusan");
                    fetchSubjectsHybrid(userJurusan);
                } else {
                    fetchSubjectsHybrid(null);
                }
            }).addOnFailureListener(e -> {
                Log.e("UserSubjectListFragment", "Failed to fetch user status", e);
                fetchSubjectsHybrid(null);
            });
        } else {
            fetchSubjectsHybrid(null);
        }
    }

    private void fetchSubjectsHybrid(String chosenJurusan) {
        if (getContext() == null) return;
        final android.content.Context context = getContext().getApplicationContext();

        // Proactive Fallback (If no data in 3 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (subjectList.isEmpty() && isAdded()) {
                Log.d("UserSubjectListFragment", "Triggering proactive fallbacks");
                addFallbackSubjects();
            }
        }, 3000);

        // 1. Room Data
        new Thread(() -> {
            try {
                List<ModelSubject> cachedSubjects = com.belajar.myapplication.data.local.AppDatabase.getInstance(context).subjectDao().getAllSubjects();
                if (cachedSubjects != null && !cachedSubjects.isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> processListFiltered(cachedSubjects, chosenJurusan));
                    }
                }
            } catch (Exception e) {
                Log.e("UserSubjectListFragment", "Room Error: " + e.getMessage());
            }
        }).start();

        // 2. Firestore Sync
        if (subjectsListener != null) subjectsListener.remove();
        subjectsListener = db.collection("subjects").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("UserSubjectListFragment", "Firestore Error: " + error.getMessage());
                if (subjectList.isEmpty() && isAdded()) addFallbackSubjects();
                return;
            }
            if (value != null && isAdded()) {
                saveAndProcessRemoteData(value, context, chosenJurusan);
            }
        });
    }

    private void saveAndProcessRemoteData(com.google.firebase.firestore.QuerySnapshot result, android.content.Context context, String chosenJurusan) {
        List<ModelSubject> remoteList = new ArrayList<>();
        for (QueryDocumentSnapshot document : result) {
            ModelSubject subject = document.toObject(ModelSubject.class);
            subject.setSubject_id(document.getId());
            remoteList.add(subject);
        }
        if (!remoteList.isEmpty()) {
            new Thread(() -> {
                try {
                    com.belajar.myapplication.data.local.AppDatabase.getInstance(context).subjectDao().insertSubjects(remoteList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> processListFiltered(remoteList, chosenJurusan));
        }
    }

    private void processListFiltered(List<ModelSubject> list, String chosenJurusan) {
        if (list == null || !isAdded()) return;
        Log.d("UserSubjectListFragment", "Processing subjects. Count: " + list.size() + ", Filter: " + chosenJurusan);
        subjectList.clear();
        for (ModelSubject subject : list) {
            if (chosenJurusan != null && !chosenJurusan.isEmpty()) {
                String subjectJurusan = subject.getJurusan();
                if (subjectJurusan == null || subjectJurusan.isEmpty() || subjectJurusan.equalsIgnoreCase(chosenJurusan)) {
                    subjectList.add(subject);
                }
            } else {
                subjectList.add(subject);
            }
        }
        Log.d("UserSubjectListFragment", "Filtered subjects count: " + subjectList.size());
        subjectList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
        adapter.notifyDataSetChanged();
    }

    private void addFallbackSubjects() {
        if (!isAdded()) return;
        subjectList.clear();
        if ("IPS".equalsIgnoreCase(userJurusan)) {
            addSubject("Geografi", "shared_ic_degree", "#D1E9FF");
            addSubject("Ekonomi", "shared_ic_stats", "#CEF7FF");
            addSubject("Sosiologi", "shared_ic_people", "#D1FADF");
            addSubject("Sejarah", "shared_ic_book", "#E9D7FE");
        } else {
            addSubject("Matematika", "shared_ic_math", "#D1E9FF");
            addSubject("Kimia", "shared_ic_chem", "#CEF7FF");
            addSubject("Biologi", "shared_ic_bio", "#D1FADF");
            addSubject("Fisika", "shared_ic_phys", "#E9D7FE");
        }
        adapter.notifyDataSetChanged();
    }

    private void addSubject(String name, String icon, String color) {
        ModelSubject s = new ModelSubject();
        s.setNama(name);
        s.setIcon_name(icon);
        s.setColor_hex(color);
        s.setSubject_id(name.toLowerCase() + "_fallback");
        subjectList.add(s);
    }

    private void navigateToMateri(ModelSubject subject) {
        UserTopicListFragment fragment = new UserTopicListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("subject_id", subject.getSubject_id());
        bundle.putString("subject_name", subject.getNama());
        fragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction().replace(R.id.layout_fragment_container, fragment).addToBackStack(null).commit();
    }

    private void incrementSubjectAccess(String subjectId) {
        if (subjectId == null) return;
        db.collection("subjects").document(subjectId).update("access_count", com.google.firebase.firestore.FieldValue.increment(1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subjectsListener != null) subjectsListener.remove();
    }
}
