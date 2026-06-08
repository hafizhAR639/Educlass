package com.belajar.myapplication.user;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.Locale;
import java.util.Map;

public class ContentFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private TextView tvTopicTitle, tvTimer, tvTimerMode, tvSessions, tvTypeLabel, tvStep, tvExplanation;
    private ImageView ivTypeIcon;
    private MaterialCardView cardTypeIcon;
    private YouTubePlayerView youTubePlayerView;
    private VideoView nativeVideoView;
    private View cardPomodoro, layoutVideo, cardReading;
    private MaterialButton btnTimerControl, btnNext, btnPrev;
    private ProgressBar pbContent;

    private String topicId, topicJudul;
    private int currentStep = 1;
    private final int totalSteps = 2;
    
    // Timer Variables
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private long timeLeftInMillis;
    private int focusTimeMinutes = 25;
    private int breakTimeMinutes = 5;
    private boolean isFocusMode = true;
    private int sessionsCompleted = 0;

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

        initViews(view);
        setupLifecycle();

        Bundle bundle = getArguments();
        if (bundle != null) {
            topicId = bundle.getString("topic_id");
            topicJudul = bundle.getString("topic_judul");
            tvTopicTitle.setText(topicJudul);
            
            boolean usePomodoro = bundle.getBoolean("use_pomodoro", false);
            if (usePomodoro) {
                focusTimeMinutes = bundle.getInt("focus_time", 25);
                breakTimeMinutes = bundle.getInt("break_time", 5);
                
                cardPomodoro.setVisibility(View.VISIBLE);
                resetTimer(); // Memastikan menit yang diubah di popup terbawa ke sini
                startTimer(); // Otomatis jalan (Auto-play)
            }

            loadContent(topicJudul);
        }

        btnNext.setOnClickListener(v -> {
            if (currentStep < totalSteps) {
                navigateStep(1);
            } else {
                completeModule();
            }
        });
        
        btnPrev.setOnClickListener(v -> navigateStep(-1));
        btnTimerControl.setOnClickListener(v -> toggleTimer());
        view.findViewById(R.id.btn_timer_reset).setOnClickListener(v -> resetTimer());
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void initViews(View v) {
        tvTopicTitle = v.findViewById(R.id.tv_topic_title);
        tvTimer = v.findViewById(R.id.tv_timer_countdown);
        tvTimerMode = v.findViewById(R.id.tv_timer_mode_desc);
        tvSessions = v.findViewById(R.id.tv_sessions_completed);
        tvTypeLabel = v.findViewById(R.id.tv_content_type_label);
        tvStep = v.findViewById(R.id.tv_content_step);
        tvExplanation = v.findViewById(R.id.tv_explanation);
        
        ivTypeIcon = v.findViewById(R.id.iv_content_type_icon);
        cardTypeIcon = v.findViewById(R.id.card_content_type_icon);
        
        cardPomodoro = v.findViewById(R.id.card_pomodoro);
        layoutVideo = v.findViewById(R.id.layout_video_container);
        cardReading = v.findViewById(R.id.card_reading_content);
        
        btnTimerControl = v.findViewById(R.id.btn_timer_control);
        btnNext = v.findViewById(R.id.btn_next);
        btnPrev = v.findViewById(R.id.btn_prev);
        
        pbContent = v.findViewById(R.id.pb_content);
        youTubePlayerView = v.findViewById(R.id.youtube_player_view);
        nativeVideoView = v.findViewById(R.id.native_video_view);
    }

    private void setupLifecycle() {
        if (youTubePlayerView != null) getLifecycle().addObserver(youTubePlayerView);
    }

    private void loadContent(String topicJudul) {
        getUserLearningStyleAndLoad(topicJudul);
    }

    private void navigateStep(int delta) {
        currentStep += delta;
        if (currentStep < 1) currentStep = 1;
        if (currentStep > totalSteps) currentStep = totalSteps;
        updateUIForStep();
    }

    private void updateUIForStep() {
        pbContent.setProgress(currentStep * 50);
        tvStep.setText("Konten " + currentStep + " dari " + totalSteps);
        
        String style = (tvTopicTitle.getTag() != null) ? (String) tvTopicTitle.getTag() : "Visual";

        if (currentStep == 1) {
            // Konten 1: Media (Dinamis berdasarkan gaya belajar)
            if (style.equalsIgnoreCase("Audio")) {
                tvTypeLabel.setText("Podcast Pembelajaran");
                ivTypeIcon.setImageResource(R.drawable.shared_ic_bell); 
            } else if (style.equalsIgnoreCase("Kinestetik")) {
                tvTypeLabel.setText("Video Tutorial Praktik");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
            } else {
                tvTypeLabel.setText("Video Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
            }
            
            layoutVideo.setVisibility(View.VISIBLE);
            cardReading.setVisibility(View.GONE);
            btnPrev.setVisibility(View.GONE);
            btnNext.setText("Selanjutnya →");
            
            cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FEE4E2"));
            ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#D92D20")));
            
        } else {
            // Konten 2: Bacaan
            tvTypeLabel.setText("Materi Bacaan");
            layoutVideo.setVisibility(View.GONE);
            cardReading.setVisibility(View.VISIBLE);
            
            // Tampilkan tombol sebelumnya
            btnPrev.setVisibility(View.VISIBLE);
            btnPrev.setEnabled(true);
            btnNext.setText("Selesai");
            
            // Warna Status (Biru)
            cardTypeIcon.setCardBackgroundColor(Color.parseColor("#E0F2FE"));
            ivTypeIcon.setImageResource(android.R.drawable.ic_menu_agenda);
            ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#0284C7")));
        }
    }

    private void completeModule() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        Toast.makeText(getContext(), "Selamat! Modul Selesai.", Toast.LENGTH_LONG).show();
        
        // Simpan status selesai untuk topik ini di koleksi terpisah agar permanen
        db.collection("users").document(uid)
                .collection("completed_topics").document(topicId)
                .set(new java.util.HashMap<String, Object>() {{
                    put("completed_at", com.google.firebase.Timestamp.now());
                }});

        // Update user stats
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long dbMinutes = doc.getLong("weekly_study_minutes");
                Long dbStreak = doc.getLong("study_streak");
                
                long currentMinutes = (dbMinutes != null) ? dbMinutes : 0;
                long currentStreak = (dbStreak != null) ? dbStreak : 0;
                
                // Tambah 45 menit (durasi rata-rata modul)
                db.collection("users").document(uid).update(
                        "weekly_study_minutes", currentMinutes + 45,
                        "study_streak", currentStreak + 1,
                        "last_topic_progress", 100
                );
            }
            getParentFragmentManager().popBackStack();
        });
    }

    // Pomodoro Logic
    private void toggleTimer() {
        if (timerRunning) pauseTimer();
        else startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btnTimerControl.setText("Mulai");
                btnTimerControl.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_media_play));
                
                if (isFocusMode) {
                    sessionsCompleted++;
                    tvSessions.setText("🍅 " + sessionsCompleted + " Selesai");
                    Toast.makeText(getContext(), "Waktunya Istirahat!", Toast.LENGTH_SHORT).show();
                    startBreak();
                } else {
                    Toast.makeText(getContext(), "Sesi Istirahat Selesai! Mulai Fokus?", Toast.LENGTH_SHORT).show();
                    startFocus();
                }
            }
        }.start();

        timerRunning = true;
        btnTimerControl.setText("Jeda");
        btnTimerControl.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_media_pause));
    }

    private void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerRunning = false;
        btnTimerControl.setText("Mulai");
        btnTimerControl.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_media_play));
    }

    private void resetTimer() {
        pauseTimer();
        startFocus();
    }

    private void startFocus() {
        isFocusMode = true;
        timeLeftInMillis = focusTimeMinutes * 60000L;
        tvTimerMode.setText(focusTimeMinutes + " menit sesi fokus");
        updateCountdownText();
    }

    private void startBreak() {
        isFocusMode = false;
        timeLeftInMillis = breakTimeMinutes * 60000L;
        tvTimerMode.setText(breakTimeMinutes + " menit sesi istirahat");
        updateCountdownText();
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void getUserLearningStyleAndLoad(String tJudul) {
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            String style = doc.getString("gaya_belajar");
            if (style == null) style = "Visual";
            tvTopicTitle.setTag(style); // Simpan gaya belajar untuk update UI
            loadActualContent(tJudul, style);
        });
    }

    @SuppressWarnings("unchecked")
    private void loadActualContent(String tJudul, String style) {
        db.collection("content").whereEqualTo("topic_id", tJudul).get().addOnSuccessListener(result -> {
            for (QueryDocumentSnapshot doc : result) {
                Map<String, Object> styleMap = (Map<String, Object>) doc.get(style.toLowerCase());
                if (styleMap == null && style.equalsIgnoreCase("Kinestetik")) {
                    styleMap = (Map<String, Object>) doc.get("kinetetik");
                }
                
                if (styleMap != null) {
                    String text = (String) styleMap.get("text_content");
                    String video = (String) styleMap.get("video_url");
                    if (tvExplanation != null && text != null) tvExplanation.setText(text.replace("\\n", "\n"));
                    if (video != null) setupVideo(video);
                }
            }
            updateUIForStep();
        });
    }

    private void setupVideo(String url) {
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            nativeVideoView.setVisibility(View.GONE);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer player) {
                    player.cueVideo(extractId(url), 0);
                }
            });
        } else {
            youTubePlayerView.setVisibility(View.GONE);
            nativeVideoView.setVisibility(View.VISIBLE);
            nativeVideoView.setVideoURI(Uri.parse(url));
            MediaController mc = new MediaController(getContext());
            mc.setAnchorView(nativeVideoView);
            nativeVideoView.setMediaController(mc);
        }
    }

    private String extractId(String url) {
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
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
