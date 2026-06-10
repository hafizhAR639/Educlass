package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.belajar.myapplication.shared.AdapterSubject;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class UserSearchFragment extends Fragment {

    private String query;
    private RecyclerView rvResults;
    private AdapterSubject adapter;
    private final List<ModelSubject> resultList = new ArrayList<>();
    private FirebaseFirestore db;

    public static UserSearchFragment newInstance(String query) {
        UserSearchFragment fragment = new UserSearchFragment();
        Bundle args = new Bundle();
        args.putString("query", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            query = getArguments().getString("query");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_fragment_search, container, false);

        View header = view.findViewById(R.id.header_search);
        TextView tvTitle = header.findViewById(R.id.tv_shared_header_title);
        if (tvTitle != null) tvTitle.setText("Hasil Pencarian: " + query);

        View btnBack = header.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        rvResults = view.findViewById(R.id.rv_search_results);
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new AdapterSubject(resultList, R.layout.admin_item_subject_admin, (subject, v) -> {
            UserTopicListFragment fragment = new UserTopicListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("subject_id", subject.getSubject_id());
            bundle.putString("subject_name", subject.getNama());
            fragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.layout_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvResults.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        performSearch();

        return view;
    }

    private void performSearch() {
        if (query == null || query.isEmpty()) return;

        db.collection("subjects")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resultList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ModelSubject subject = doc.toObject(ModelSubject.class);
                        subject.setSubject_id(doc.getId());
                        
                        if (subject.getNama() != null && subject.getNama().toLowerCase().contains(query.toLowerCase())) {
                            resultList.add(subject);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
