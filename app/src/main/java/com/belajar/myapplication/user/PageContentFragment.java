package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.Map;

public class PageContentFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private TextView tvLabelGayaBelajar, tvPenjelasan;
    private String topicId;
    private YouTubePlayerView youTubePlayerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_fragment_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvLabelGayaBelajar = view.findViewById(R.id.tv_learning_style);
        TextView tvJudulTopik = view.findViewById(R.id.tv_topic_title);
        tvPenjelasan = view.findViewById(R.id.tv_explanation);
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        
        if (youTubePlayerView != null) {
            getLifecycle().addObserver(youTubePlayerView);
        }

        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            topicId = bundle.getString("topic_id");
            String topicJudul = bundle.getString("topic_judul");
            
            if (tvJudulTopik != null && topicJudul != null) tvJudulTopik.setText(topicJudul);
            
            // Mengambil gaya belajar dulu, baru load content
            getUserLearningStyleAndLoadContent(topicJudul);
        }
    }

    private void getUserLearningStyleAndLoadContent(String topicJudul) {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            String styleFromDb = documentSnapshot.getString("gaya_belajar");
            final String finalStyle = (styleFromDb != null) ? styleFromDb : "Visual";
            
            if (tvLabelGayaBelajar != null) tvLabelGayaBelajar.setText(finalStyle);
            loadContentBasedOnStyle(topicId, topicJudul, finalStyle);
        }).addOnFailureListener(e -> loadContentBasedOnStyle(topicId, topicJudul, "Visual"));
    }

    @SuppressWarnings("unchecked")
    private void loadContentBasedOnStyle(String tId, String tJudul, String style) {
        // Coba cari berdasarkan Document ID dulu
        db.collection("content")
            .whereEqualTo("topic_id", tId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    processContentResults(task.getResult(), style);
                } else {
                    // Jika tidak ketemu dengan ID, coba cari berdasarkan Judul (sesuai struktur Firestore user)
                    db.collection("content")
                        .whereEqualTo("topic_id", tJudul)
                        .get()
                        .addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult() != null && !task2.getResult().isEmpty()) {
                                processContentResults(task2.getResult(), style);
                            } else {
                                showNotFoundMessage(style);
                            }
                        });
                }
            });
    }

    @SuppressWarnings("unchecked")
    private void processContentResults(com.google.firebase.firestore.QuerySnapshot result, String style) {
        for (QueryDocumentSnapshot document : result) {
            // Mapping berdasarkan struktur yang diberikan user
            String styleKey = style.toLowerCase();
            // Handle typo di Firestore user "kinetetik"
            if (styleKey.equals("kinestetik") || styleKey.equals("kin")) {
                styleKey = "kinetetik"; 
            }
            
            Map<String, Object> contentMap = (Map<String, Object>) document.get(styleKey);
            if (contentMap == null) {
                // Fallback case-sensitive
                contentMap = (Map<String, Object>) document.get(style);
            }

            if (contentMap != null) {
                displayContent(contentMap);
            } else {
                showNotFoundMessage(style);
            }
        }
    }

    private void showNotFoundMessage(String style) {
        String msg = "Konten untuk gaya belajar " + style + " belum tersedia.";
        if (tvPenjelasan != null) tvPenjelasan.setText(msg);
        if (youTubePlayerView != null) youTubePlayerView.setVisibility(View.GONE);
    }

    private void displayContent(Map<String, Object> contentMap) {
        String text = (String) contentMap.get("text_content");
        if (tvPenjelasan != null && text != null) {
            tvPenjelasan.setText(text.replace("\\n", "\n"));
        }
        
        String videoUrl = (String) contentMap.get("video_url");
        // Gunakan key sesuai Firestore user: "audio_url", "kin_url"
        if (videoUrl == null) videoUrl = (String) contentMap.get("audio_url");
        if (videoUrl == null) videoUrl = (String) contentMap.get("kin_url");

        View view = getView();
        if (view != null) {
            TextView tvLabelVideo = view.findViewById(R.id.tv_video_label);
            if (videoUrl == null || videoUrl.isEmpty()) {
                if (tvLabelVideo != null) tvLabelVideo.setVisibility(View.GONE);
                if (youTubePlayerView != null) youTubePlayerView.setVisibility(View.GONE);
            } else {
                if (tvLabelVideo != null) tvLabelVideo.setVisibility(View.VISIBLE);
                if (youTubePlayerView != null) {
                    youTubePlayerView.setVisibility(View.VISIBLE);
                    setupYouTubePlayer(videoUrl);
                }
            }
        }
    }

    private void setupYouTubePlayer(String videoId) {
        final String cleanVideoId = extractVideoId(videoId);
        if (youTubePlayerView != null) {
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(cleanVideoId, 0);
                }
            });
        }
    }

    private String extractVideoId(String url) {
        if (url == null || url.isEmpty()) return "";
        if (url.length() == 11) return url;
        if (url.contains("v=")) {
            int start = url.indexOf("v=") + 2;
            int end = Math.min(start + 11, url.length());
            return url.substring(start, end);
        }
        if (url.contains("be/")) {
            int start = url.indexOf("be/") + 3;
            int end = Math.min(start + 11, url.length());
            return url.substring(start, end);
        }
        return url;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (youTubePlayerView != null) {
            getLifecycle().removeObserver(youTubePlayerView);
        }
    }
}
