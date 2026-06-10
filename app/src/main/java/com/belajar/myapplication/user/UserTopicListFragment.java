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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.shared.AdapterTopic;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserTopicListFragment extends Fragment {

    private String subjectId, subjectName;
    private RecyclerView rvTopics;
    private AdapterTopic adapter;
    private final List<ModelTopic> topicList = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView tvCurrentStyle, btnChangeStyle, tvModuleCount, tvHeaderTitle;
    private ImageView ivStyleIcon, ivHeaderBg;
    private final String[] learningStyles = {"Visual", "Audio", "Kinestetik"};
    private ListenerRegistration topicsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_materi, container, false);
        
        if (getArguments() != null) {
            subjectId = getArguments().getString("subject_id");
            subjectName = getArguments().getString("subject_name");
        }

        tvHeaderTitle = view.findViewById(R.id.tv_materi_header_title);
        ivHeaderBg = view.findViewById(R.id.iv_materi_header_bg);
        if (tvHeaderTitle != null && subjectName != null) tvHeaderTitle.setText(subjectName);
        
        updateHeaderImage(subjectName);

        view.findViewById(R.id.btn_back_materi).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        db = FirebaseFirestore.getInstance();
        
        tvCurrentStyle = view.findViewById(R.id.tv_current_learning_style);
        btnChangeStyle = view.findViewById(R.id.btn_change_style);
        ivStyleIcon = view.findViewById(R.id.iv_learning_style_icon);
        tvModuleCount = view.findViewById(R.id.tv_module_count_badge);

        loadLearningStyle();
        if (btnChangeStyle != null) {
            btnChangeStyle.setOnClickListener(v -> showStyleSelectionDialog());
        }

        rvTopics = view.findViewById(R.id.rv_topics);
        rvTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        
        checkPremiumAndSetupAdapter();

        return view;
    }

    private void updateHeaderImage(String name) {
        if (ivHeaderBg == null || name == null) return;
        ivHeaderBg.setImageResource(R.drawable.user_img_header_math);
    }

    private void checkPremiumAndSetupAdapter() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                boolean isPremium = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isPremium"));
                setupAdapter(isPremium);
                startTopicsListener();
            }).addOnFailureListener(e -> {
                // Jika gagal (misal SecurityException), asumsikan bukan premium tapi tetap jalankan
                setupAdapter(false);
                startTopicsListener();
            });
        } else {
            setupAdapter(false);
            startTopicsListener();
        }
    }

    private void setupAdapter(boolean isPremium) {
        adapter = new AdapterTopic(topicList, false, isPremium, new AdapterTopic.OnTopicClickListener() {
            @Override
            public void onTopicClick(ModelTopic topic, boolean isLocked) {
                if (isLocked) {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.layout_fragment_container, new UserPremiumFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    showPomodoroDialog(topic);
                }
            }
        });
        rvTopics.setAdapter(adapter);
    }

    private void showPomodoroDialog(ModelTopic topic) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_pomodoro, null);
        EditText etFocus = dialogView.findViewById(R.id.et_focus_time);
        EditText etBreak = dialogView.findViewById(R.id.et_break_time);
        
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialogView.findViewById(R.id.btn_reject).setOnClickListener(v -> {
            dialog.dismiss();
            navigateToContent(topic, false, 0, 0);
        });

        dialogView.findViewById(R.id.btn_accept).setOnClickListener(v -> {
            String focusStr = etFocus.getText().toString();
            String breakStr = etBreak.getText().toString();
            int focus = focusStr.isEmpty() ? 25 : Integer.parseInt(focusStr);
            int breakT = breakStr.isEmpty() ? 5 : Integer.parseInt(breakStr);
            dialog.dismiss();
            navigateToContent(topic, true, focus, breakT);
        });

        dialog.show();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = android.view.Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void navigateToContent(ModelTopic topic, boolean usePomodoro, int focus, int breakT) {
        UserContentFragment fragment = new UserContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("topic_id", topic.getTopic_id());
        bundle.putString("topic_judul", topic.getJudul());
        bundle.putString("subject_id", subjectId);
        bundle.putBoolean("use_pomodoro", usePomodoro);
        bundle.putInt("focus_time", focus);
        bundle.putInt("break_time", breakT);
        fragment.setArguments(bundle);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void startTopicsListener() {
        if (subjectId == null) return;
        
        topicsListener = db.collection("topics")
                .whereEqualTo("subject_id", subjectId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        processTopics(value);
                    }
                });
    }

    private void processTopics(com.google.firebase.firestore.QuerySnapshot result) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Use the current style from UI for faster response
        String userStyle = tvCurrentStyle != null ? tvCurrentStyle.getText().toString() : "Visual";
        final String finalStyle = userStyle;

        db.collection("users").document(uid).collection("completed_topics").get().addOnSuccessListener(completedSnap -> {
            java.util.Set<String> completedIds = new java.util.HashSet<>();
            for (QueryDocumentSnapshot doc : completedSnap) {
                completedIds.add(doc.getId());
            }

            topicList.clear();
            for (QueryDocumentSnapshot document : result) {
                ModelTopic topic = document.toObject(ModelTopic.class);
                topic.setTopic_id(document.getId());

                // Filtering Logic: Tampilkan jika cocok dengan gaya belajar atau jika tidak ada batasan gaya
                boolean matches = false;
                java.util.List<String> styles = topic.getLearning_styles();
                String lowStyle = finalStyle.toLowerCase();

                if (styles != null && !styles.isEmpty()) {
                    if (styles.contains(lowStyle)) matches = true;
                } else {
                    // Cek boolean flags sebagai fallback
                    if (lowStyle.contains("visual") && topic.isHas_visual()) matches = true;
                    else if (lowStyle.contains("audio") && topic.isHas_audio()) matches = true;
                    else if (lowStyle.contains("kinestetik") && topic.isHas_kinestetik()) matches = true;
                    
                    // Jika tidak ada info gaya sama sekali, tampilkan saja
                    if (!topic.isHas_visual() && !topic.isHas_audio() && !topic.isHas_kinestetik() && (styles == null || styles.isEmpty())) {
                        matches = true;
                    }
                }

                if (matches) {
                    if (completedIds.contains(topic.getTopic_id())) {
                        topic.setProgress(100);
                    } else {
                        topic.setProgress(0);
                    }
                    topicList.add(topic);
                }
            }

            // Jika setelah difilter kosong, tampilkan semua saja daripada user bingung
            if (topicList.isEmpty() && !result.isEmpty()) {
                for (QueryDocumentSnapshot document : result) {
                    ModelTopic topic = document.toObject(ModelTopic.class);
                    topic.setTopic_id(document.getId());
                    if (completedIds.contains(topic.getTopic_id())) topic.setProgress(100);
                    topicList.add(topic);
                }
            }
            topicList.sort((a, b) -> Long.compare(a.getOrder(), b.getOrder()));
            if (adapter != null) adapter.notifyDataSetChanged();
            updateModuleCountText(completedIds.size());
        });
    }

    private void loadLearningStyle() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && isAdded()) {
                String style = doc.getString("gaya_belajar");
                if (style == null) style = "Visual";
                updateStyleUI(style);
            }
        });
    }

    private void updateStyleUI(String style) {
        if (tvCurrentStyle != null) tvCurrentStyle.setText(style);
        if (ivStyleIcon != null && subjectName != null) {
            String lowName = subjectName.toLowerCase();
            int resId = R.drawable.shared_ic_math;
            if (lowName.contains("kimia") || lowName.contains("chem")) resId = R.drawable.shared_ic_chem;
            else if (lowName.contains("biologi") || lowName.contains("bio")) resId = R.drawable.shared_ic_bio;
            else if (lowName.contains("fisika") || lowName.contains("phys")) resId = R.drawable.shared_ic_phys;
            ivStyleIcon.setImageResource(resId);
        }
    }

    private void updateModuleCountText(int completedCount) {
        if (tvModuleCount != null) {
            int total = topicList.size();
            
            int progressPercent = 0;
            if (total > 0) {
                // Pastikan progress tidak melebihi 100%
                progressPercent = Math.min(100, (completedCount * 100 / total));
            }
            
            tvModuleCount.setText(String.format(Locale.getDefault(), 
                "%d Modul • %d%% Selesai", 
                total, progressPercent));
        }
    }

    private void showStyleSelectionDialog() {
        String current = tvCurrentStyle != null ? tvCurrentStyle.getText().toString() : "Visual";
        int checkedItem = 0;
        for (int i = 0; i < learningStyles.length; i++) {
            if (learningStyles[i].equalsIgnoreCase(current)) {
                checkedItem = i;
                break;
            }
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Pilih Gaya Belajar")
                .setSingleChoiceItems(learningStyles, checkedItem, (dialog, which) -> {
                    updateLearningStyle(learningStyles[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateLearningStyle(String newStyle) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        db.collection("users").document(uid).update("gaya_belajar", newStyle)
                .addOnSuccessListener(aVoid -> {
                    updateStyleUI(newStyle);
                    // Refresh topics when style changes
                    if (topicsListener != null) {
                        topicsListener.remove();
                        startTopicsListener();
                    }
                    Toast.makeText(getContext(), "Gaya belajar diubah ke " + newStyle, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (topicsListener != null) topicsListener.remove();
    }
}
