package com.belajar.myapplication.user;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.util.Locale;
import java.util.Map;

public class ContentFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private TextView tvTopicTitle, tvTimer, tvTimerMode, tvSessions, tvTypeLabel, tvStep, tvExplanation, tvVideoSpeed, tvReadingTitle;
    private ImageView ivTypeIcon, ivVideoPlayPause, ivVideoLandscape, ivVideoVolume, btnSkipBack, btnSkipForward;
    private ImageView btnAudioPlayPause, btnAudioSkipBack, btnAudioSkipForward;
    private MaterialCardView cardTypeIcon, btnVideoSpeed;
    private YouTubePlayerView youTubePlayerView;
    private VideoView nativeVideoView;
    private View cardPomodoro, layoutVideo, cardReading, cardPodcast;
    private MaterialButton btnTimerControl, btnNext, btnPrev;
    private ProgressBar pbContent;
    private SeekBar videoSeekBar, audioSeekBar;
    private ImageView ivPodcastCover;
    private TextView tvPodcastTitle, tvPodcastSubtitle;

    private String topicId, topicJudul;
    private int currentStep = 1;
    private final int totalSteps = 2;

    private float currentPlaybackRate = 1.0f;
    private float youtubeCurrentTime = 0f;
    private boolean isYouTubePlayerInitialized = false;
    private boolean isVideoPlaying = false;
    private YouTubePlayer activeYouTubePlayer;
    private MediaPlayer nativeMediaPlayer;
    private final float[] speeds = {0.5f, 1.0f, 1.5f, 2.0f};
    
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

            loadContent(topicId);
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
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                toggleLandscape();
            } else {
                getParentFragmentManager().popBackStack();
            }
        });
        
        if (btnVideoSpeed != null) {
            btnVideoSpeed.setOnClickListener(v -> cyclePlaybackRate());
        }

        btnSkipBack.setOnClickListener(v -> skipTime(-15));
        btnSkipForward.setOnClickListener(v -> skipTime(15));
        ivVideoPlayPause.setOnClickListener(v -> toggleVideoPlayPause());
        ivVideoLandscape.setOnClickListener(v -> toggleLandscape());

        btnAudioSkipBack.setOnClickListener(v -> skipTime(-15));
        btnAudioSkipForward.setOnClickListener(v -> skipTime(15));
        btnAudioPlayPause.setOnClickListener(v -> toggleVideoPlayPause());

        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (activeYouTubePlayer != null) {
                        activeYouTubePlayer.seekTo(progress);
                    } else if (nativeVideoView.getVisibility() == View.VISIBLE || cardPodcast.getVisibility() == View.VISIBLE) {
                        nativeVideoView.seekTo(progress * 1000);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };
        
        videoSeekBar.setOnSeekBarChangeListener(seekListener);
        audioSeekBar.setOnSeekBarChangeListener(seekListener);
    }

    private final android.os.Handler seekHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (nativeVideoView != null && nativeVideoView.isPlaying()) {
                int progress = nativeVideoView.getCurrentPosition() / 1000;
                if (videoSeekBar != null) videoSeekBar.setProgress(progress);
                if (audioSeekBar != null) audioSeekBar.setProgress(progress);
            }
            seekHandler.postDelayed(this, 1000);
        }
    };

    private void toggleVideoPlayPause() {
        if (activeYouTubePlayer != null) {
            if (isVideoPlaying) {
                activeYouTubePlayer.pause();
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                activeYouTubePlayer.play();
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
            isVideoPlaying = !isVideoPlaying;
        } else if (nativeVideoView != null) {
            if (nativeVideoView.isPlaying()) {
                nativeVideoView.pause();
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                nativeVideoView.start();
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    private void toggleLandscape() {
        if (getActivity() != null) {
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                hideSystemUI();
            } else {
                getActivity().setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                showSystemUI();
            }
        }
    }

    private void hideSystemUI() {
        if (getActivity() != null && getActivity().getWindow() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void showSystemUI() {
        if (getActivity() != null && getActivity().getWindow() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateUIForOrientation(newConfig.orientation);
    }

    private void updateUIForOrientation(int orientation) {
        boolean isLandscape = (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE);
        
        View header = getView() != null ? getView().findViewById(R.id.header_section) : null;
        View contentInfo = getView() != null ? getView().findViewById(R.id.content_info_section) : null;
        View footerNav = getView() != null ? getView().findViewById(R.id.footer_nav) : null;
        View contentArea = getView() != null ? getView().findViewById(R.id.content_area) : null; // Need to add this ID
        
        if (isLandscape) {
            if (header != null) header.setVisibility(View.GONE);
            if (cardPomodoro != null) cardPomodoro.setVisibility(View.GONE);
            if (contentInfo != null) contentInfo.setVisibility(View.GONE);
            if (footerNav != null) footerNav.setVisibility(View.GONE);
            if (cardReading != null) cardReading.setVisibility(View.GONE);
            if (tvExplanation != null) tvExplanation.setVisibility(View.GONE);
            
            // Remove padding for fullscreen
            if (contentArea instanceof android.widget.LinearLayout) {
                ((android.widget.LinearLayout) contentArea).setPadding(0, 0, 0, 0);
            }
            
            ViewGroup.LayoutParams lp = layoutVideo.getLayoutParams();
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutVideo.setLayoutParams(lp);
        } else {
            if (header != null) header.setVisibility(View.VISIBLE);
            if (cardPomodoro != null && getArguments() != null && getArguments().getBoolean("use_pomodoro")) 
                cardPomodoro.setVisibility(View.VISIBLE);
            if (contentInfo != null) contentInfo.setVisibility(View.VISIBLE);
            if (footerNav != null) footerNav.setVisibility(View.VISIBLE);
            if (tvExplanation != null) tvExplanation.setVisibility(View.VISIBLE);
            
            // Restore padding
            if (contentArea instanceof android.widget.LinearLayout) {
                float density = getResources().getDisplayMetrics().density;
                ((android.widget.LinearLayout) contentArea).setPadding((int)(24*density), (int)(32*density), (int)(24*density), 0);
            }
            
            updateUIForStep();
            
            ViewGroup.LayoutParams lp = layoutVideo.getLayoutParams();
            lp.height = (int) (220 * getResources().getDisplayMetrics().density);
            layoutVideo.setLayoutParams(lp);
        }
    }

    private void initViews(View v) {
        tvTopicTitle = v.findViewById(R.id.tv_topic_title);
        tvTimer = v.findViewById(R.id.tv_timer_countdown);
        tvTimerMode = v.findViewById(R.id.tv_timer_mode_desc);
        tvSessions = v.findViewById(R.id.tv_sessions_completed);
        tvTypeLabel = v.findViewById(R.id.tv_content_type_label);
        tvStep = v.findViewById(R.id.tv_content_step);
        tvExplanation = v.findViewById(R.id.tv_explanation);
        tvReadingTitle = v.findViewById(R.id.tv_reading_title);
        
        ivTypeIcon = v.findViewById(R.id.iv_content_type_icon);
        cardTypeIcon = v.findViewById(R.id.card_content_type_icon);
        
        cardPomodoro = v.findViewById(R.id.card_pomodoro);
        layoutVideo = v.findViewById(R.id.layout_video_container);
        cardReading = v.findViewById(R.id.card_reading_content);
        cardPodcast = v.findViewById(R.id.card_podcast_player);
        
        ivPodcastCover = v.findViewById(R.id.iv_podcast_cover);
        tvPodcastTitle = v.findViewById(R.id.tv_podcast_title);
        tvPodcastSubtitle = v.findViewById(R.id.tv_podcast_subtitle);
        
        btnTimerControl = v.findViewById(R.id.btn_timer_control);
        btnNext = v.findViewById(R.id.btn_next);
        btnPrev = v.findViewById(R.id.btn_prev);
        btnSkipBack = v.findViewById(R.id.btn_skip_back);
        btnSkipForward = v.findViewById(R.id.btn_skip_forward);
        ivVideoPlayPause = v.findViewById(R.id.btn_video_play_pause);
        ivVideoLandscape = v.findViewById(R.id.btn_video_landscape);
        ivVideoVolume = v.findViewById(R.id.iv_video_volume);
        videoSeekBar = v.findViewById(R.id.video_seekbar);
        
        btnAudioPlayPause = v.findViewById(R.id.btn_audio_play_pause);
        btnAudioSkipBack = v.findViewById(R.id.btn_audio_skip_back);
        btnAudioSkipForward = v.findViewById(R.id.btn_audio_skip_forward);
        audioSeekBar = v.findViewById(R.id.audio_seekbar);
        
        pbContent = v.findViewById(R.id.pb_content);
        youTubePlayerView = v.findViewById(R.id.youtube_player_view);
        nativeVideoView = v.findViewById(R.id.native_video_view);

        tvVideoSpeed = v.findViewById(R.id.tv_video_speed);
        btnVideoSpeed = v.findViewById(R.id.btn_video_speed);
    }

    private void setupLifecycle() {
        if (youTubePlayerView != null) getLifecycle().addObserver(youTubePlayerView);
    }

    private void loadContent(String tId) {
        getUserLearningStyleAndLoad(tId);
    }

    private void skipTime(int seconds) {
        if (activeYouTubePlayer != null) {
            float newTime = youtubeCurrentTime + seconds;
            if (newTime < 0) newTime = 0;
            activeYouTubePlayer.seekTo(newTime);
        } else if (nativeVideoView != null && nativeVideoView.isPlaying()) {
            int currentPos = nativeVideoView.getCurrentPosition();
            int newPos = currentPos + (seconds * 1000);
            if (newPos < 0) newPos = 0;
            nativeVideoView.seekTo(newPos);
        }
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
                ivTypeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off); 
                tvVideoSpeed.setVisibility(View.GONE);
                btnVideoSpeed.setVisibility(View.GONE);
                ivVideoLandscape.setVisibility(View.GONE);
                
                layoutVideo.setVisibility(View.GONE);
                cardPodcast.setVisibility(View.VISIBLE);
                
                // Audio style specific (Purple/Indigo)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#4F46E5")));
            } else if (style.equalsIgnoreCase("Kinestetik")) {
                tvTypeLabel.setText("Video Tutorial Praktik");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                tvVideoSpeed.setVisibility(View.VISIBLE);
                btnVideoSpeed.setVisibility(View.VISIBLE);
                
                layoutVideo.setVisibility(View.VISIBLE);
                cardPodcast.setVisibility(View.GONE);

                // Kinestetik style specific (Orange/Amber)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FFF7ED"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#EA580C")));
            } else {
                tvTypeLabel.setText("Video Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                tvVideoSpeed.setVisibility(View.VISIBLE);
                btnVideoSpeed.setVisibility(View.VISIBLE);
                
                layoutVideo.setVisibility(View.VISIBLE);
                cardPodcast.setVisibility(View.GONE);

                // Visual style specific (Red/Rose)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FEE4E2"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#D92D20")));
            }
            
            cardReading.setVisibility(View.GONE);
            btnPrev.setVisibility(View.GONE);
            btnNext.setText("Selanjutnya →");
        } else {
            // Konten 2: Bacaan
            tvTypeLabel.setText("Materi Bacaan");
            if (tvReadingTitle != null && topicJudul != null) tvReadingTitle.setText("MATERI PEMBELAJARAN: " + topicJudul.toUpperCase());
            layoutVideo.setVisibility(View.GONE);
            cardPodcast.setVisibility(View.GONE);
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
                    put("subject_id", (getArguments() != null ? getArguments().getString("subject_id") : ""));
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

    private void getUserLearningStyleAndLoad(String tId) {
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            String style = doc.getString("gaya_belajar");
            if (style == null) style = "Visual";
            
            if (getView() != null) {
                TextView tvStyle = getView().findViewById(R.id.tv_learning_style);
                TextView tvInfo = getView().findViewById(R.id.tv_content_info);
                if (tvStyle != null) tvStyle.setText(style);
                
                if (tvInfo != null) {
                    if (style.equalsIgnoreCase("Audio")) tvInfo.setText("Materi Podcast & Audio");
                    else if (style.equalsIgnoreCase("Kinestetik")) tvInfo.setText("Materi Tutorial & Praktik");
                    else tvInfo.setText("Materi Video & Visual");
                }
            }
            
            tvTopicTitle.setTag(style); // Simpan gaya belajar untuk update UI
            loadActualContent(tId, style);
        });
    }

    @SuppressWarnings("unchecked")
    private void loadActualContent(String tId, String style) {
        db.collection("content").whereEqualTo("topic_id", tId).get().addOnSuccessListener(result -> {
            boolean contentFound = false;
            for (QueryDocumentSnapshot doc : result) {
                // strict mapping based on admin's upload
                String styleKey = style.toLowerCase();
                if (styleKey.equals("kinestetik")) {
                    if (doc.get("kinestetik") == null && doc.get("kinetetik") != null) styleKey = "kinetetik";
                }
                
                Map<String, Object> styleMap = (Map<String, Object>) doc.get(styleKey);
                
                if (styleMap != null) {
                    String video = (String) styleMap.get("video_url");
                    String audio = (String) styleMap.get("audio_url");
                    String text = (String) styleMap.get("text_content");
                    
                    // Only proceed if at least one media or text is present for this specific style
                    if ((video != null && !video.isEmpty()) || (audio != null && !audio.isEmpty()) || (text != null && !text.isEmpty())) {
                        contentFound = true;
                        if (tvExplanation != null && text != null) tvExplanation.setText(text.replace("\\n", "\n"));
                        
                        if (style.equalsIgnoreCase("Audio")) {
                            setupAudio(audio != null && !audio.isEmpty() ? audio : video);
                        } else {
                            setupVideo(video);
                        }
                        break; 
                    }
                }
            }
            
            if (!contentFound) {
                // Fallback content to ensure material is ALWAYS visible
                String mockText;
                String mockUrl;
                
                if (style.equalsIgnoreCase("Audio")) {
                    mockText = "🎧 **Sesi Podcast Pembelajaran**\n\n" +
                            "Selamat datang di sesi podcast untuk materi: " + (topicJudul != null ? topicJudul : "Topik Ini") + ".\n\n" +
                            "Dalam sesi ini, kita akan membahas konsep secara mendalam melalui audio. " +
                            "Gunakan tombol skip 15 detik jika Anda ingin mengulang bagian tertentu.\n\n" +
                            "**Daftar Bahasan:**\n" +
                            "1. Pengenalan Konsep Utama\n" +
                            "2. Pembahasan Detail & Contoh\n" +
                            "3. Kesimpulan & Poin Penting\n\n" +
                            "Tips: Fokuslah pada suara dan cobalah memvisualisasikan apa yang dijelaskan.";
                    mockUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
                    setupAudio(mockUrl);
                } else if (style.equalsIgnoreCase("Kinestetik")) {
                    mockText = "🛠️ **Panduan Praktik Mandiri**\n\n" +
                            "Materi: " + (topicJudul != null ? topicJudul : "Topik Ini") + " (Versi Praktik)\n\n" +
                            "Silakan ikuti tutorial video di atas sambil mempraktikkannya langsung. " +
                            "Metode belajar ini dirancang agar Anda lebih cepat memahami dengan cara mencoba.\n\n" +
                            "**Langkah Kegiatan:**\n" +
                            "1. Siapkan peralatan atau aplikasi yang diperlukan.\n" +
                            "2. Amati demonstrasi langkah demi langkah di video.\n" +
                            "3. Coba lakukan hal yang sama secara mandiri.\n" +
                            "4. Catat jika ada kendala atau temuan menarik.";
                    mockUrl = "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_1mb.mp4";
                    setupVideo(mockUrl);
                } else {
                    mockText = "📺 **Video Pembelajaran Visual**\n\n" +
                            "Selamat datang di materi video untuk: " + (topicJudul != null ? topicJudul : "Topik Ini") + ".\n\n" +
                            "Saksikan penjelasan visual di atas untuk memahami ilustrasi dan diagram penting dalam materi ini.\n\n" +
                            "**Poin Visual:**\n" +
                            "• Diagram alur proses utama\n" +
                            "• Ilustrasi grafis perbandingan data\n" +
                            "• Rangkuman poin-poin kunci di akhir video";
                    mockUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"; // Rick Roll as stable placeholder
                    setupVideo(mockUrl);
                }

                if (tvExplanation != null) tvExplanation.setText(mockText);
            }

            updateUIForStep();
            updateLastAccessed();
        });
    }

    private void updateLastAccessed() {
        String uid = mAuth.getUid();
        if (uid == null || topicId == null) return;

        db.collection("users").document(uid).update(
                "last_topic_id", topicId,
                "last_topic_title", topicJudul,
                "last_topic_progress", (currentStep * 100) / totalSteps
        );
    }

    private void setupAudio(String url) {
        if (url == null || url.isEmpty()) return;
        
        // Podcast style (Audio)
        layoutVideo.setVisibility(View.INVISIBLE); 
        ViewGroup.LayoutParams lp = layoutVideo.getLayoutParams();
        lp.height = 1; 
        layoutVideo.setLayoutParams(lp);
        
        cardPodcast.setVisibility(View.VISIBLE);
        if (tvPodcastTitle != null) tvPodcastTitle.setText("Podcast: " + (topicJudul != null ? topicJudul : "Materi"));
        if (tvPodcastSubtitle != null) tvPodcastSubtitle.setText("Podcast Pembelajaran");
        
        setupVideo(url);
    }

    private void setupVideo(String url) {
        if (url == null || url.isEmpty()) {
            layoutVideo.setVisibility(View.GONE);
            return;
        }

        String style = (tvTopicTitle.getTag() != null) ? (String) tvTopicTitle.getTag() : "Visual";
        if (!style.equalsIgnoreCase("Audio")) {
            layoutVideo.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = layoutVideo.getLayoutParams();
            lp.height = (int) (220 * getResources().getDisplayMetrics().density);
            layoutVideo.setLayoutParams(lp);
        } else {
            layoutVideo.setVisibility(View.INVISIBLE);
        }

        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            youTubePlayerView.setVisibility(View.VISIBLE);
            nativeVideoView.setVisibility(View.GONE);

            if (!isYouTubePlayerInitialized) {
                IFramePlayerOptions options = new IFramePlayerOptions.Builder()
                        .controls(0)
                        .build();

                youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer player) {
                        activeYouTubePlayer = player;
                        isYouTubePlayerInitialized = true;
                        
                        // Disable default library UI properly
                        try {
                            youTubePlayerView.setCustomPlayerUi(new View(getContext()));
                        } catch (Exception ignored) {}

                        player.cueVideo(extractId(url), 0);
                        player.setPlaybackRate(com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_1);
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                        youtubeCurrentTime = second;
                        if (videoSeekBar != null) videoSeekBar.setProgress((int) second);
                        if (audioSeekBar != null) audioSeekBar.setProgress((int) second);
                    }

                    @Override
                    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                        if (videoSeekBar != null) videoSeekBar.setMax((int) duration);
                        if (audioSeekBar != null) audioSeekBar.setMax((int) duration);
                    }

                    @Override
                    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState state) {
                        if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING) {
                            isVideoPlaying = true;
                            ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                            btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        } else {
                            isVideoPlaying = false;
                            ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                            btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                }, options);
            } else if (activeYouTubePlayer != null) {
                activeYouTubePlayer.cueVideo(extractId(url), 0);
            }
        } else {
            youTubePlayerView.setVisibility(View.GONE);
            nativeVideoView.setVisibility(View.VISIBLE);
            nativeVideoView.setVideoURI(Uri.parse(url));
            
            nativeVideoView.setOnPreparedListener(mp -> {
                nativeMediaPlayer = mp;
                int durationSec = nativeVideoView.getDuration() / 1000;
                if (videoSeekBar != null) videoSeekBar.setMax(durationSec);
                if (audioSeekBar != null) audioSeekBar.setMax(durationSec);
                seekHandler.post(updateSeekBar);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    try {
                        mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(currentPlaybackRate));
                    } catch (Exception ignored) {}
                }
            });

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

    private void cyclePlaybackRate() {
        int currentIndex = 1; // Default 1.0x
        for (int i = 0; i < speeds.length; i++) {
            if (speeds[i] == currentPlaybackRate) {
                currentIndex = i;
                break;
            }
        }
        
        int nextIndex = (currentIndex + 1) % speeds.length;
        currentPlaybackRate = speeds[nextIndex];
        
        if (tvVideoSpeed != null) {
            tvVideoSpeed.setText(String.format(Locale.getDefault(), "%.1fx", currentPlaybackRate));
        }

        if (activeYouTubePlayer != null) {
            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate rate;
            if (currentPlaybackRate == 0.5f) rate = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_0_5;
            else if (currentPlaybackRate == 1.5f) rate = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_1_5;
            else if (currentPlaybackRate == 2.0f) rate = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_2;
            else rate = com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlaybackRate.RATE_1;
            
            activeYouTubePlayer.setPlaybackRate(rate);
        }

        if (nativeMediaPlayer != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                nativeMediaPlayer.setPlaybackParams(nativeMediaPlayer.getPlaybackParams().setSpeed(currentPlaybackRate));
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        seekHandler.removeCallbacks(updateSeekBar);
    }
}
