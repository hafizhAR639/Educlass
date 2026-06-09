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
import com.belajar.myapplication.data.models.ModelTopic;
import com.belajar.myapplication.data.models.ModelContent;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
    private TextView tvVideoCurrentTime, tvVideoTotalTime, tvAudioCurrentTime, tvAudioTotalTime;
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
    private ListenerRegistration userListener, contentListener;
    private final float[] speeds = {0.5f, 1.0f, 1.5f, 2.0f};
    
    // Timer Variables
    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private long timeLeftInMillis;
    private int focusTimeMinutes = 25;
    private int breakTimeMinutes = 5;
    private boolean isFocusMode = true;
    private int sessionsCompleted = 0;
    
    // Break Popup
    private androidx.appcompat.app.AlertDialog breakDialog;
    private TextView currentBreakTimerTextView;
    private boolean wasVideoPlayingBeforeBreak = false;

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
                    } else if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE) {
                        nativeVideoView.seekTo(progress * 1000);
                    } else if (nativeMediaPlayer != null) {
                        nativeMediaPlayer.seekTo(progress * 1000);
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
            if (!isVideoPlaying || !isAdded() || getContext() == null) {
                return;
            }

            int currentPos = 0;
            int totalPos = 0;

            try {
                if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE) {
                    if (nativeVideoView.isPlaying()) {
                        currentPos = nativeVideoView.getCurrentPosition() / 1000;
                        totalPos = nativeVideoView.getDuration() / 1000;
                    }
                } else if (nativeMediaPlayer != null) {
                    try {
                        if (nativeMediaPlayer.isPlaying()) {
                            currentPos = nativeMediaPlayer.getCurrentPosition() / 1000;
                            totalPos = nativeMediaPlayer.getDuration() / 1000;
                        }
                    } catch (IllegalStateException ignored) {}
                }
            } catch (Exception ignored) {}

            if (currentPos >= 0) {
                if (videoSeekBar != null) videoSeekBar.setProgress(currentPos);
                if (audioSeekBar != null) audioSeekBar.setProgress(currentPos);
                
                String timeStr = formatTime(currentPos);
                if (tvVideoCurrentTime != null) tvVideoCurrentTime.setText(timeStr);
                if (tvAudioCurrentTime != null) tvAudioCurrentTime.setText(timeStr);
                
                if (totalPos > 0) {
                    if (tvVideoTotalTime != null) tvVideoTotalTime.setText(formatTime(totalPos));
                    if (tvAudioTotalTime != null) tvAudioTotalTime.setText(formatTime(totalPos));
                    if (videoSeekBar != null) videoSeekBar.setMax(totalPos);
                    if (audioSeekBar != null) audioSeekBar.setMax(totalPos);
                }
            }
            
            if (isVideoPlaying) {
                seekHandler.postDelayed(this, 1000);
            }
        }
    };

    private String formatTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }

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
        } else if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE) {
            if (nativeVideoView.isPlaying()) {
                nativeVideoView.pause();
                isVideoPlaying = false;
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                nativeVideoView.start();
                isVideoPlaying = true;
                ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                
                seekHandler.removeCallbacks(updateSeekBar);
                seekHandler.post(updateSeekBar);
            }
        } else if (nativeMediaPlayer != null) {
            try {
                if (nativeMediaPlayer.isPlaying()) {
                    nativeMediaPlayer.pause();
                    isVideoPlaying = false;
                    ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                    btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    nativeMediaPlayer.start();
                    isVideoPlaying = true;
                    ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                    
                    seekHandler.removeCallbacks(updateSeekBar);
                    seekHandler.post(updateSeekBar);
                }
            } catch (IllegalStateException e) {
                Toast.makeText(getContext(), "Audio belum siap diputar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopMedia() {
        seekHandler.removeCallbacks(updateSeekBar);
        if (activeYouTubePlayer != null) {
            activeYouTubePlayer.pause();
            activeYouTubePlayer.seekTo(0);
        } else if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE) {
            nativeVideoView.pause();
            nativeVideoView.seekTo(0);
        } else if (nativeMediaPlayer != null) {
            try {
                nativeMediaPlayer.pause();
                nativeMediaPlayer.seekTo(0);
            } catch (Exception ignored) {}
        }
        isVideoPlaying = false;
        ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
        btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
        if (videoSeekBar != null) videoSeekBar.setProgress(0);
        if (audioSeekBar != null) audioSeekBar.setProgress(0);
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

        tvVideoCurrentTime = v.findViewById(R.id.tv_video_current_time);
        tvVideoTotalTime = v.findViewById(R.id.tv_video_total_time);
        tvAudioCurrentTime = v.findViewById(R.id.tv_audio_current_time);
        tvAudioTotalTime = v.findViewById(R.id.tv_audio_total_time);
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
        } else if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE) {
            int currentPos = nativeVideoView.getCurrentPosition();
            int newPos = currentPos + (seconds * 1000);
            if (newPos < 0) newPos = 0;
            nativeVideoView.seekTo(newPos);
        } else if (nativeMediaPlayer != null) {
            int currentPos = nativeMediaPlayer.getCurrentPosition();
            int newPos = currentPos + (seconds * 1000);
            if (newPos < 0) newPos = 0;
            nativeMediaPlayer.seekTo(newPos);
        }
    }

    private void navigateStep(int delta) {
        currentStep += delta;
        if (currentStep < 1) currentStep = 1;
        if (currentStep > totalSteps) currentStep = totalSteps;
        updateUIForStep();
    }

    private boolean isVideoActive = false;
    private boolean isAudioActive = false;
    private boolean isReadingActive = false;

    private void updateUIForStep() {
        pbContent.setProgress(currentStep * 50);
        tvStep.setText("Konten " + currentStep + " dari " + totalSteps);
        
        String style = (tvTopicTitle.getTag() != null) ? (String) tvTopicTitle.getTag() : "Visual";

        if (currentStep == 1) {
            // Reset common visibility first
            layoutVideo.setVisibility(View.GONE);
            cardPodcast.setVisibility(View.GONE);
            cardReading.setVisibility(View.GONE);
            btnPrev.setVisibility(View.GONE);
            btnNext.setText("Selanjutnya →");

            if (style.equalsIgnoreCase("Audio")) {
                tvTypeLabel.setText("Podcast Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off); 
                tvVideoSpeed.setVisibility(View.GONE);
                btnVideoSpeed.setVisibility(View.GONE);
                ivVideoLandscape.setVisibility(View.GONE);
                
                if (isAudioActive) cardPodcast.setVisibility(View.VISIBLE);
                else if (isVideoActive) layoutVideo.setVisibility(View.VISIBLE);
                
                // Audio style specific (Purple/Indigo)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#4F46E5")));
            } else if (style.equalsIgnoreCase("Kinestetik")) {
                tvTypeLabel.setText("Video Tutorial Praktik");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                tvVideoSpeed.setVisibility(View.VISIBLE);
                btnVideoSpeed.setVisibility(View.VISIBLE);
                
                if (isVideoActive) layoutVideo.setVisibility(View.VISIBLE);
                else if (isAudioActive) cardPodcast.setVisibility(View.VISIBLE);

                // Kinestetik style specific (Orange/Amber)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FFF7ED"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#EA580C")));
            } else {
                tvTypeLabel.setText("Video Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                tvVideoSpeed.setVisibility(View.VISIBLE);
                btnVideoSpeed.setVisibility(View.VISIBLE);
                
                if (isVideoActive) layoutVideo.setVisibility(View.VISIBLE);

                // Visual style specific (Red/Rose)
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FEE4E2"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#D92D20")));
            }
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
                if (currentBreakTimerTextView != null && breakDialog != null && breakDialog.isShowing()) {
                    updatePopupCountdownText(currentBreakTimerTextView);
                }
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                btnTimerControl.setText("Mulai");
                btnTimerControl.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_media_play));
                
                if (isFocusMode) {
                    sessionsCompleted++;
                    tvSessions.setText("🍅 " + sessionsCompleted + " Selesai");
                    
                    // Pause video when focus ends
                    wasVideoPlayingBeforeBreak = isVideoPlaying;
                    if (isVideoPlaying) toggleVideoPlayPause();
                    
                    startBreak();
                    showBreakPopup();
                } else {
                    if (breakDialog != null) breakDialog.dismiss();
                    currentBreakTimerTextView = null;
                    
                    startFocus();
                    startTimer(); // Auto resume focus
                    
                    // Resume video after break if it was playing
                    if (wasVideoPlayingBeforeBreak && !isVideoPlaying) toggleVideoPlayPause();
                    wasVideoPlayingBeforeBreak = false;
                }
            }
        }.start();

        timerRunning = true;
        btnTimerControl.setText("Jeda");
        btnTimerControl.setIcon(ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_media_pause));
    }

    private void showBreakPopup() {
        if (getActivity() == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.user_dialog_break_timer, null);
        currentBreakTimerTextView = dialogView.findViewById(R.id.tv_break_timer);
        MaterialButton btnSkip = dialogView.findViewById(R.id.btn_skip_break);

        breakDialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (breakDialog.getWindow() != null) {
            breakDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSkip.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            timerRunning = false;
            breakDialog.dismiss();
            currentBreakTimerTextView = null;
            startFocus();
            startTimer();
            if (wasVideoPlayingBeforeBreak && !isVideoPlaying) toggleVideoPlayPause();
            wasVideoPlayingBeforeBreak = false;
        });

        updatePopupCountdownText(currentBreakTimerTextView);
        breakDialog.show();
        startTimer(); // Auto play break timer
    }

    private void updatePopupCountdownText(TextView textView) {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        textView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
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
        
        // Use a listener to make it responsive to style changes
        if (userListener != null) userListener.remove();
        
        userListener = db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) return;
            
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

    private void loadActualContent(String tId, String style) {
        if (contentListener != null) contentListener.remove();
        
        contentListener = db.collection("content").document(tId).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) {
                db.collection("topics").document(tId).get().addOnSuccessListener(topicDoc -> {
                    if (topicDoc.exists()) {
                        String penjelasan = topicDoc.getString("deskripsi");
                        if (tvExplanation != null && penjelasan != null) {
                            tvExplanation.setText(penjelasan.replace("\\n", "\n"));
                        }
                    }
                });
                return;
            }

            ModelContent content = doc.toObject(ModelContent.class);
            if (content == null) return;

            // Reset active states
            isVideoActive = false;
            isAudioActive = false;
            isReadingActive = false;

            if (style.equalsIgnoreCase("Visual") && content.getVisual() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getVisual().getText_content());
                String yUrl = content.getVisual().getYoutube_url();
                String vUrl = content.getVisual().getVideo_url();
                
                if (yUrl != null && !yUrl.trim().isEmpty()) {
                    isVideoActive = true;
                    setupVideo(yUrl);
                } else if (vUrl != null && !vUrl.trim().isEmpty()) {
                    isVideoActive = true;
                    setupVideo(vUrl);
                } else showEmptyMedia("Video");
            } else if (style.equalsIgnoreCase("Audio") && content.getAudio() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getAudio().getDescription());
                String yUrl = content.getAudio().getYoutube_url();
                String aUrl = content.getAudio().getAudio_url();
                
                if (yUrl != null && !yUrl.trim().isEmpty()) {
                    isAudioActive = true;
                    setupAudio(yUrl);
                } else if (aUrl != null && !aUrl.trim().isEmpty()) {
                    isAudioActive = true;
                    setupAudio(aUrl);
                } else showEmptyMedia("Audio");
            } else if (style.equalsIgnoreCase("Kinestetik") && content.getKinestetik() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getKinestetik().getDescription());
                String yUrl = content.getKinestetik().getYoutube_url();
                String fUrl = content.getKinestetik().getFile_url();
                
                if (yUrl != null && !yUrl.trim().isEmpty()) {
                    isVideoActive = true;
                    setupVideo(yUrl);
                } else if (fUrl != null && !fUrl.trim().isEmpty()) {
                    // Deteksi apakah file_url ini video atau audio/dokumen
                    String lowUrl = fUrl.toLowerCase();
                    if (lowUrl.contains(".mp4") || lowUrl.contains(".mkv") || lowUrl.contains(".webm") || lowUrl.contains("video")) {
                        isVideoActive = true;
                        setupVideo(fUrl);
                    } else if (lowUrl.contains(".mp3") || lowUrl.contains(".wav") || lowUrl.contains(".m4a") || lowUrl.contains("audio")) {
                        isAudioActive = true;
                        setupAudio(fUrl);
                    } else {
                        setupKinestetik(fUrl, true);
                    }
                } else {
                    showEmptyMedia("Materi Kinestetik");
                }
            }

            updateUIForStep();
            updateLastAccessed();
        });
    }

    private void showEmptyMedia(String type) {
        Toast.makeText(getContext(), type + " belum tersedia.", Toast.LENGTH_SHORT).show();
    }

    private void setupKinestetik(String url, boolean isFile) {
        layoutVideo.setVisibility(View.GONE);
        cardPodcast.setVisibility(View.GONE);
        
        // Custom UI for Kinestetik if needed, or just a button in the explanation
        if (tvExplanation != null) {
            String current = tvExplanation.getText().toString();
            tvExplanation.setText(current + "\n\n[Klik tombol di bawah untuk membuka materi]");
        }
        
        btnNext.setText(isFile ? "Buka File" : "Buka Link");
        btnNext.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Gagal membuka materi", Toast.LENGTH_SHORT).show();
            }
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
        
        // Cek apakah ini YouTube (karena YouTube butuh layoutVideo)
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            layoutVideo.setVisibility(View.VISIBLE);
            cardPodcast.setVisibility(View.GONE);
            
            // Atur tinggi video agar pas
            ViewGroup.LayoutParams lp = layoutVideo.getLayoutParams();
            lp.height = (int) (220 * getResources().getDisplayMetrics().density);
            layoutVideo.setLayoutParams(lp);
        } else {
            // Podcast style (Audio asli)
            layoutVideo.setVisibility(View.GONE); 
            cardPodcast.setVisibility(View.VISIBLE);
        }
        
        if (tvPodcastTitle != null) tvPodcastTitle.setText("Podcast: " + (topicJudul != null ? topicJudul : "Materi"));
        if (tvPodcastSubtitle != null) tvPodcastSubtitle.setText("Podcast Pembelajaran");
        
        playMedia(url);
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
            layoutVideo.setVisibility(View.GONE);
        }

        playMedia(url);
    }

    private void playMedia(String url) {
        if (url == null || url.isEmpty()) {
            layoutVideo.setVisibility(View.GONE);
            cardPodcast.setVisibility(View.GONE);
            return;
        }

        final String videoId = extractId(url);
        boolean isYouTube = !videoId.isEmpty() || url.contains("youtube.com") || url.contains("youtu.be");

        // Stop any current playback
        stopMedia();
        if (nativeMediaPlayer != null) {
            try {
                nativeMediaPlayer.release();
            } catch (Exception ignored) {}
            nativeMediaPlayer = null;
        }

        if (isYouTube) {
            layoutVideo.setVisibility(View.VISIBLE); // Pastikan container video muncul
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
                        player.loadVideo(videoId, 0);
                    }

                    @Override
                    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                        youtubeCurrentTime = second;
                        int s = (int) second;
                        if (videoSeekBar != null) videoSeekBar.setProgress(s);
                        if (audioSeekBar != null) audioSeekBar.setProgress(s);
                        String timeStr = formatTime(s);
                        if (tvVideoCurrentTime != null) tvVideoCurrentTime.setText(timeStr);
                        if (tvAudioCurrentTime != null) tvAudioCurrentTime.setText(timeStr);
                    }

                    @Override
                    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                        int d = (int) duration;
                        if (videoSeekBar != null) videoSeekBar.setMax(d);
                        if (audioSeekBar != null) audioSeekBar.setMax(d);
                        String durationStr = formatTime(d);
                        if (tvVideoTotalTime != null) tvVideoTotalTime.setText(durationStr);
                        if (tvAudioTotalTime != null) tvAudioTotalTime.setText(durationStr);
                    }

                    @Override
                    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState state) {
                        if (state == com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING) {
                            isVideoPlaying = true;
                            if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                            if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        } else {
                            isVideoPlaying = false;
                            if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                            if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                }, options);
            } else if (activeYouTubePlayer != null) {
                activeYouTubePlayer.loadVideo(videoId, 0);
            }
        } else {
            if (cardPodcast.getVisibility() == View.VISIBLE) {
                // Play Audio using MediaPlayer directly for robustness
                youTubePlayerView.setVisibility(View.GONE);
                nativeVideoView.setVisibility(View.GONE);
                
                try {
                    nativeMediaPlayer = new MediaPlayer();
                    nativeMediaPlayer.setDataSource(url);
                    nativeMediaPlayer.setOnPreparedListener(mp -> {
                        int durationSec = mp.getDuration() / 1000;
                        if (audioSeekBar != null) audioSeekBar.setMax(durationSec);
                        mp.start();
                        isVideoPlaying = true;
                        if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        
                        seekHandler.removeCallbacks(updateSeekBar);
                        seekHandler.post(updateSeekBar);
                    });
                    nativeMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        String msg = "Gagal memutar audio";
                        if (extra == -1010) msg += ": Format tidak didukung";
                        else if (extra == -1004) msg += ": Kesalahan jaringan";
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    nativeMediaPlayer.setOnCompletionListener(mp -> {
                        isVideoPlaying = false;
                        if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        if (audioSeekBar != null) audioSeekBar.setProgress(0);
                        if (videoSeekBar != null) videoSeekBar.setProgress(0);
                        if (tvAudioCurrentTime != null) tvAudioCurrentTime.setText("00:00");
                        if (tvVideoCurrentTime != null) tvVideoCurrentTime.setText("00:00");
                        seekHandler.removeCallbacks(updateSeekBar);
                        mp.seekTo(0);
                    });
                    nativeMediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Format link audio tidak valid", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Play Video using VideoView
                youTubePlayerView.setVisibility(View.GONE);
                nativeVideoView.setVisibility(View.VISIBLE);
                
                try {
                    // Ensure the URL is clean and parsed as Uri
                    Uri videoUri = Uri.parse(url);
                    nativeVideoView.setVideoURI(videoUri);
                    
                    nativeVideoView.setOnPreparedListener(mp -> {
                        int durationSec = nativeVideoView.getDuration() / 1000;
                        if (videoSeekBar != null) videoSeekBar.setMax(durationSec);
                        
                        // Setup playback speed if supported
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            try {
                                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(currentPlaybackRate));
                            } catch (Exception ignored) {}
                        }
                        
                        nativeVideoView.start();
                        isVideoPlaying = true;
                        if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                        
                        seekHandler.removeCallbacks(updateSeekBar);
                        seekHandler.post(updateSeekBar);
                    });

                    nativeVideoView.setOnErrorListener((mp, what, extra) -> {
                        String errorMsg = "Gagal memutar video";
                        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) errorMsg += ": Server died";
                        else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) errorMsg += ": Format tidak didukung atau kendala jaringan";
                        
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        return true;
                    });
                    
                    nativeVideoView.setOnCompletionListener(mp -> {
                        isVideoPlaying = false;
                        if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                        if (videoSeekBar != null) videoSeekBar.setProgress(0);
                        if (tvVideoCurrentTime != null) tvVideoCurrentTime.setText("00:00");
                        seekHandler.removeCallbacks(updateSeekBar);
                    });
                    
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Format link video tidak valid", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String extractId(String url) {
        if (url == null || url.trim().isEmpty()) return "";
        
        url = url.trim();
        
        // Handle direct 11-char ID
        if (url.length() == 11 && !url.contains("/") && !url.contains("=")) {
            return url;
        }

        // Extremely robust regex for YouTube ID extraction
        String pattern = "(?i)(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/|youtube\\.com\\/shorts\\/)([a-zA-Z0-9_-]{11})";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        // Manual parsing fallbacks for common formats
        if (url.contains("v=")) {
            int start = url.indexOf("v=") + 2;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            String id = url.substring(start, end);
            return id.length() >= 11 ? id.substring(0, 11) : "";
        } else if (url.contains("youtu.be/")) {
            int start = url.indexOf("youtu.be/") + 9;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            String id = url.substring(start, end);
            return id.length() >= 11 ? id.substring(0, 11) : "";
        }

        return "";
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
    public void onStart() {
        super.onStart();
        // Resume media if it was playing
        if (nativeVideoView != null && nativeVideoView.getVisibility() == View.VISIBLE && isVideoPlaying) {
            nativeVideoView.start();
        } else if (nativeMediaPlayer != null && isVideoPlaying) {
            nativeMediaPlayer.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (nativeVideoView != null && nativeVideoView.isPlaying()) {
            nativeVideoView.pause();
        }
        if (nativeMediaPlayer != null && nativeMediaPlayer.isPlaying()) {
            nativeMediaPlayer.pause();
        }
        if (activeYouTubePlayer != null) {
            activeYouTubePlayer.pause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseTimer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        seekHandler.removeCallbacks(updateSeekBar);
        if (userListener != null) userListener.remove();
        if (contentListener != null) contentListener.remove();
        if (nativeVideoView != null) {
            nativeVideoView.stopPlayback();
        }
        if (nativeMediaPlayer != null) {
            try {
                nativeMediaPlayer.release();
            } catch (Exception ignored) {}
        }
        nativeMediaPlayer = null;
    }
}
