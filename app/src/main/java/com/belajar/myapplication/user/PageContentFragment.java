package com.belajar.myapplication.user;

import android.os.Bundle;
import android.util.Log;
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
    private TextView tvLabelGayaBelajar, tvJudulTopik, tvPenjelasan;
    private String topicId, topicJudul;
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

        tvLabelGayaBelajar = view.findViewById(R.id.rsa0mkdkytdc); // Label "Visual", "Audio", dll
        tvJudulTopik = view.findViewById(R.id.r8p2j2jpedem);
        tvPenjelasan = view.findViewById(R.id.r29po0f85m63);
        youTubePlayerView = view.findViewById(R.id.youtube_player_view);
        
        if (youTubePlayerView != null) {
            getLifecycle().addObserver(youTubePlayerView);
        }

        View btnBack = view.findViewById(R.id.rs9gqiz7qaib);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            topicId = bundle.getString("topic_id");
            topicJudul = bundle.getString("topic_judul");
            
            if (topicJudul != null) tvJudulTopik.setText(topicJudul);
            if (topicId != null) {
                getUserLearningStyleAndLoadContent();
            }
        }
    }

    private void getUserLearningStyleAndLoadContent() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        // Step 1: Ambil gaya belajar user dari koleksi "users"
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            String style = documentSnapshot.getString("gaya_belajar");
            if (style == null) style = "Visual"; // Default jika tidak ada
            
            tvLabelGayaBelajar.setText(style);
            loadContentBasedOnStyle(topicId, style);
        }).addOnFailureListener(e -> {
            loadContentBasedOnStyle(topicId, "Visual"); // Fallback
        });
    }

    private void loadContentBasedOnStyle(String tId, String style) {
        // Step 2: Ambil konten dari koleksi "content"
        db.collection("content")
            .whereEqualTo("topic_id", tId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        
                        // Pilih map berdasarkan gaya belajar (visual, audio, atau kinestetik)
                        String styleKey = style.toLowerCase();
                        if (styleKey.equals("kinestetik")) styleKey = "kinestetik"; // Memastikan nama key
                        
                        Map<String, Object> contentMap = (Map<String, Object>) document.get(styleKey);
                        
                        if (contentMap != null) {
                            // Tampilkan Teks
                            String text = (String) contentMap.get("text_content");
                            if (text != null) {
                                tvPenjelasan.setText(text.replace("\\n", "\n"));
                            }
                            
                            // Tampilkan Video (jika ada field video_url atau audio_url atau kin_url)
                            String videoUrl = (String) contentMap.get("video_url");
                            if (videoUrl == null) videoUrl = (String) contentMap.get("audio_url");
                            if (videoUrl == null) videoUrl = (String) contentMap.get("kin_url");

                            TextView tvLabelVideo = getView().findViewById(R.id.rmntw2d4hb59);
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
                } else {
                    tvPenjelasan.setText("Konten untuk gaya belajar " + style + " belum tersedia.");
                    if (youTubePlayerView != null) youTubePlayerView.setVisibility(View.GONE);
                }
            });
    }

    private void setupYouTubePlayer(String videoId) {
        final String cleanVideoId = extractVideoId(videoId);
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(cleanVideoId, 0);
            }
        });
    }

    private String extractVideoId(String url) {
        if (url == null) return "";
        if (url.length() == 11) return url;
        if (url.contains("v=")) {
            int start = url.indexOf("v=") + 2;
            return url.substring(start, Math.min(start + 11, url.length()));
        }
        if (url.contains("be/")) {
            int start = url.indexOf("be/") + 3;
            return url.substring(start, Math.min(start + 11, url.length()));
        }
        return url;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
    }
}
