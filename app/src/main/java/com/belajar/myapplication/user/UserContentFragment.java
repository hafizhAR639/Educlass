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

public class UserContentFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    
    private TextView tvTopicTitle, tvTimer, tvTimerMode, tvSessions, tvTypeLabel, tvStep, tvExplanation, tvVideoSpeed, tvReadingTitle;
    private TextView tvVideoCurrentTime, tvVideoTotalTime, tvAudioCurrentTime, tvAudioTotalTime;
    private ImageView ivTypeIcon, ivVideoPlayPause, ivVideoLandscape, ivVideoVolume, btnSkipBack, btnSkipForward;
    private ImageView btnAudioPlayPause, btnAudioSkipBack, btnAudioSkipForward;
    private MaterialCardView cardTypeIcon, btnVideoSpeed;
    private YouTubePlayerView youTubePlayerView;
    private VideoView nativeVideoView;
    private View cardPomodoro, layoutVideo, cardReading, cardPodcast, cardQuiz;
    private MaterialButton btnTimerControl, btnNext, btnPrev;
    private ProgressBar pbContent;
    private SeekBar videoSeekBar, audioSeekBar;
    private ImageView ivPodcastCover;
    private TextView tvPodcastTitle, tvPodcastSubtitle;

    private String topicId, topicJudul;
    private String currentMediaUrl = null;
    private boolean isMediaPreparing = false;
    private int currentStep = 1;
    private int totalSteps = 2;

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
                resetTimer(); 
                startTimer(); 
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
            if (!isVideoPlaying) {
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
                    if (nativeMediaPlayer.isPlaying()) {
                        currentPos = nativeMediaPlayer.getCurrentPosition() / 1000;
                        totalPos = nativeMediaPlayer.getDuration() / 1000;
                    }
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
        if (activeYouTubePlayer != null && youTubePlayerView.getVisibility() == View.VISIBLE) {
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
                    seekHandler.post(updateSeekBar);
                }
            } catch (Exception ignored) {}
        }
    }

    private void stopMedia() {
        isVideoPlaying = false;
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
        View contentArea = getView() != null ? getView().findViewById(R.id.content_area) : null; 
        
        if (isLandscape) {
            if (header != null) header.setVisibility(View.GONE);
            if (cardPomodoro != null) cardPomodoro.setVisibility(View.GONE);
            if (contentInfo != null) contentInfo.setVisibility(View.GONE);
            if (footerNav != null) footerNav.setVisibility(View.GONE);
            if (cardReading != null) cardReading.setVisibility(View.GONE);
            if (tvExplanation != null) tvExplanation.setVisibility(View.GONE);
            
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
        layoutVideo = v.findViewById(R.id.layout_video_wrapper);
        cardReading = v.findViewById(R.id.card_reading_content);
        cardPodcast = v.findViewById(R.id.card_podcast_player);
        cardQuiz = v.findViewById(R.id.card_quiz_content);
        
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
        if (activeYouTubePlayer != null && youTubePlayerView.getVisibility() == View.VISIBLE) {
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
        stopMedia(); // Stop media before navigating
        currentStep += delta;
        if (currentStep < 1) currentStep = 1;
        if (currentStep > totalSteps) currentStep = totalSteps;
        updateUIForStep();
    }

    private boolean isVideoActive = false;
    private boolean isAudioActive = false;
    private boolean isReadingActive = false;

    private void updateUIForStep() {
        if (layoutVideo != null) layoutVideo.setVisibility(View.GONE);
        if (btnVideoSpeed != null) btnVideoSpeed.setVisibility(View.GONE);
        if (cardPodcast != null) cardPodcast.setVisibility(View.GONE);
        if (cardReading != null) cardReading.setVisibility(View.GONE);
        if (cardQuiz != null) cardQuiz.setVisibility(View.GONE);

        pbContent.setProgress((currentStep * 100) / totalSteps);
        tvStep.setText("Konten " + currentStep + " dari " + totalSteps);
        
        if (getView() != null) {
            TextView tvInfo = getView().findViewById(R.id.tv_content_info);
            if (tvInfo != null) {
                tvInfo.setText(totalSteps + " Konten • 45 menit");
            }
        }
        
        String style = (tvTopicTitle.getTag() != null) ? (String) tvTopicTitle.getTag() : "Visual";

        if (currentStep == 1) {
            btnPrev.setVisibility(View.GONE);
            btnNext.setText("Selanjutnya →");

            if (style.equalsIgnoreCase("Audio")) {
                tvTypeLabel.setText("Podcast Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_lock_silent_mode_off); 
                ivVideoLandscape.setVisibility(View.GONE);
                
                if (isVideoActive) {
                    layoutVideo.setVisibility(View.VISIBLE);
                } else {
                    cardPodcast.setVisibility(View.VISIBLE);
                }
                
                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#4F46E5")));
            } else if (style.equalsIgnoreCase("Kinestetik")) {
                tvTypeLabel.setText("Materi Tutorial");
                ivTypeIcon.setImageResource(android.R.drawable.ic_media_play);
                
                if (isAudioActive) {
                    cardPodcast.setVisibility(View.VISIBLE);
                    layoutVideo.setVisibility(View.GONE);
                } else {
                    layoutVideo.setVisibility(View.VISIBLE);
                    if (btnVideoSpeed != null) btnVideoSpeed.setVisibility(View.VISIBLE);
                }

                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FFF7ED"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#EA580C")));
            } else { // Visual
                tvTypeLabel.setText("Video Pembelajaran");
                ivTypeIcon.setImageResource(android.R.drawable.ic_menu_slideshow);
                
                layoutVideo.setVisibility(View.VISIBLE);
                if (btnVideoSpeed != null) btnVideoSpeed.setVisibility(View.VISIBLE);

                cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FEE4E2"));
                ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#D92D20")));
            }
        } else if (currentStep == 2) {
            tvTypeLabel.setText("Materi Bacaan");
            if (tvReadingTitle != null && topicJudul != null) tvReadingTitle.setText("MATERI PEMBELAJARAN: " + topicJudul.toUpperCase());
            cardReading.setVisibility(View.VISIBLE);
            
            btnPrev.setVisibility(View.VISIBLE);
            btnPrev.setEnabled(true);
            
            if (style.equalsIgnoreCase("Kinestetik")) {
                btnNext.setText("Selanjutnya →");
            } else {
                btnNext.setText("Selesai");
            }
            
            cardTypeIcon.setCardBackgroundColor(Color.parseColor("#E0F2FE"));
            ivTypeIcon.setImageResource(android.R.drawable.ic_menu_agenda);
            ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#0284C7")));
        } else if (currentStep == 3 && style.equalsIgnoreCase("Kinestetik")) {
            tvTypeLabel.setText("Evaluasi & Praktik");
            cardQuiz.setVisibility(View.VISIBLE);
            
            btnPrev.setVisibility(View.VISIBLE);
            btnPrev.setEnabled(true);
            btnNext.setText("Selesai");
            
            cardTypeIcon.setCardBackgroundColor(Color.parseColor("#FFF1F2"));
            ivTypeIcon.setImageResource(android.R.drawable.star_on);
            ImageViewCompat.setImageTintList(ivTypeIcon, ColorStateList.valueOf(Color.parseColor("#D92D20")));
            
            // Inject summary into quiz card if available
            View rootView = getView();
            if (rootView != null) {
                ViewGroup layoutSummary = rootView.findViewById(R.id.layout_quiz_summary);
                if (layoutSummary != null && layoutSummary.getChildCount() == 0) {
                    TextView tvSummary = new TextView(getContext());
                    tvSummary.setText("• Video: Teknik praktik " + topicJudul + "\n• Teori: Konsep dasar dan prosedur kerja");
                    tvSummary.setTextColor(Color.parseColor("#475467"));
                    tvSummary.setTextSize(14);
                    tvSummary.setLineSpacing(0, 1.4f);
                    layoutSummary.addView(tvSummary);
                    
                    View padding = new View(getContext());
                    padding.setLayoutParams(new android.widget.LinearLayout.LayoutParams(1, (int)(16 * getResources().getDisplayMetrics().density)));
                    layoutSummary.addView(padding);
                }
            }
        }
    }

    private void completeModule() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        Toast.makeText(getContext(), "Selamat! Modul Selesai.", Toast.LENGTH_LONG).show();
        
        db.collection("users").document(uid)
                .collection("completed_topics").document(topicId)
                .set(new java.util.HashMap<String, Object>() {{
                    put("completed_at", com.google.firebase.Timestamp.now());
                    put("subject_id", (getArguments() != null ? getArguments().getString("subject_id") : ""));
                }});

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long dbMinutes = doc.getLong("weekly_study_minutes");
                Long dbStreak = doc.getLong("study_streak");
                
                long currentMinutes = (dbMinutes != null) ? dbMinutes : 0;
                long currentStreak = (dbStreak != null) ? dbStreak : 0;
                
                db.collection("users").document(uid).update(
                        "weekly_study_minutes", currentMinutes + 45,
                        "study_streak", currentStreak + 1,
                        "last_topic_progress", 100
                );
            }
            getParentFragmentManager().popBackStack();
        });
    }

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
        
        if (userListener != null) userListener.remove();
        
        userListener = db.collection("users").document(uid).addSnapshotListener((doc, error) -> {
            if (error != null || doc == null || !doc.exists()) return;
            
            String style = doc.getString("gaya_belajar");
            if (style == null) style = "Visual";
            
            if (style.equalsIgnoreCase("Kinestetik")) {
                totalSteps = 3;
            } else {
                totalSteps = 2;
            }
            
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
            
            tvTopicTitle.setTag(style); 
            loadActualContent(tId, style);
        });
    }

    private String convertToDirectUrl(String url) {
        if (url == null) return null;
        String trimmedUrl = url.trim();
        if (trimmedUrl.contains("drive.google.com")) {
            if (trimmedUrl.contains("/file/d/")) {
                int start = trimmedUrl.indexOf("/file/d/") + 8;
                int end = trimmedUrl.indexOf("/", start);
                if (end == -1) end = trimmedUrl.indexOf("?", start);
                if (end == -1) end = trimmedUrl.length();
                String id = trimmedUrl.substring(start, end);
                return "https://drive.google.com/uc?id=" + id + "&export=download";
            }
        }
        return trimmedUrl;
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

            isVideoActive = false;
            isAudioActive = false;
            isReadingActive = false;

            if (style.equalsIgnoreCase("Visual") && content.getVisual() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getVisual().getText_content());
                String yUrl = content.getVisual().getYoutube_url();
                String vUrl = content.getVisual().getVideo_url();
                
                currentMediaUrl = (yUrl != null && !yUrl.trim().isEmpty()) ? yUrl : vUrl;
                if (currentMediaUrl != null && !currentMediaUrl.trim().isEmpty()) {
                    isVideoActive = true;
                    isAudioActive = false;
                } else {
                    showEmptyMedia("Video");
                }
            } else if (style.equalsIgnoreCase("Audio") && content.getAudio() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getAudio().getDescription());
                String yUrl = content.getAudio().getYoutube_url();
                String aUrl = content.getAudio().getAudio_url();
                
                currentMediaUrl = (yUrl != null && !yUrl.trim().isEmpty()) ? yUrl : aUrl;
                if (currentMediaUrl != null && !currentMediaUrl.trim().isEmpty()) {
                    // Cek apakah ini video youtube atau file audio murni
                    String videoId = extractId(currentMediaUrl);
                    if (!videoId.isEmpty()) {
                        isVideoActive = true;
                        isAudioActive = false;
                    } else {
                        isAudioActive = true;
                        isVideoActive = false;
                    }
                    
                    if (tvPodcastTitle != null) tvPodcastTitle.setText("Podcast: " + (topicJudul != null ? topicJudul : "Materi"));
                    if (tvPodcastSubtitle != null) tvPodcastSubtitle.setText("Podcast Pembelajaran");
                } else {
                    showEmptyMedia("Audio");
                }
            } else if (style.equalsIgnoreCase("Kinestetik") && content.getKinestetik() != null) {
                if (tvExplanation != null) tvExplanation.setText(content.getKinestetik().getDescription());
                String yUrl = content.getKinestetik().getYoutube_url();
                String fUrl = content.getKinestetik().getFile_url();
                String kUrl = content.getKinestetik().getKin_url();
                
                currentMediaUrl = null;
                if (yUrl != null && !yUrl.trim().isEmpty()) currentMediaUrl = yUrl.trim();
                else if (kUrl != null && !kUrl.trim().isEmpty()) currentMediaUrl = kUrl.trim();
                else if (fUrl != null && !fUrl.trim().isEmpty()) currentMediaUrl = fUrl.trim();

                if (currentMediaUrl != null && !currentMediaUrl.isEmpty()) {
                    String lowUrl = currentMediaUrl.toLowerCase();
                    // Fix: Check if it's a YouTube URL first for Kinestetik
                    String videoId = extractId(currentMediaUrl);
                    
                    if (!videoId.isEmpty()) {
                        isVideoActive = true;
                        isAudioActive = false;
                    } else if (lowUrl.contains(".mp3") || lowUrl.contains(".wav") || lowUrl.contains(".m4a") || lowUrl.contains("audio")) {
                        // Only treat as audio if it's explicitly an audio file extension
                        isAudioActive = true;
                        isVideoActive = false;
                        
                        if (tvPodcastTitle != null) tvPodcastTitle.setText("Panduan Praktik: " + (topicJudul != null ? topicJudul : "Materi"));
                        if (tvPodcastSubtitle != null) tvPodcastSubtitle.setText("Panduan Audio Kinestetik");
                    } else {
                        // Default to video for everything else (mp4, webm, or unknown files)
                        isVideoActive = true;
                        isAudioActive = false;
                    }
                } else {
                    showEmptyMedia("Materi Kinestetik");
                }
            }

            updateUIForStep();
            updateLastAccessed();
            
            // Trigger playback AFTER UI is updated for step 1
            if (currentStep == 1 && currentMediaUrl != null) {
                playMedia(currentMediaUrl);
            }
        });
    }

    private void showEmptyMedia(String type) {
        Toast.makeText(getContext(), type + " belum tersedia.", Toast.LENGTH_SHORT).show();
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

    private void playMedia(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        // Prevent restarting the same media if it's already playing or preparing
        if (url.equals(currentMediaUrl) && (isVideoPlaying || isMediaPreparing)) {
            return;
        }

        currentMediaUrl = url;
        isMediaPreparing = true;

        final String finalUrl = convertToDirectUrl(url);
        final String videoId = extractId(finalUrl);
        
        if (!videoId.isEmpty()) {
            isVideoActive = true;
            isAudioActive = false;
            playYouTube(videoId);
        } else if (isAudioActive) {
            playAudio(finalUrl);
        } else {
            playVideoFile(finalUrl);
        }
    }

    private void playYouTube(String videoId) {
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
                    isMediaPreparing = false;
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
            activeYouTubePlayer.loadVideo(videoId, 0);
        }
    }

    private void playAudio(String url) {
        youTubePlayerView.setVisibility(View.GONE);
        nativeVideoView.setVisibility(View.GONE);
        cardPodcast.setVisibility(View.VISIBLE);
        
        try {
            if (nativeMediaPlayer != null) {
                try {
                    nativeMediaPlayer.stop();
                    nativeMediaPlayer.reset();
                } catch (Exception ignored) {}
                nativeMediaPlayer.release();
                nativeMediaPlayer = null;
            }
            nativeMediaPlayer = new MediaPlayer();
            
            nativeMediaPlayer.setVolume(1.0f, 1.0f);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                nativeMediaPlayer.setAudioAttributes(new android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            } else {
                nativeMediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            }

            // Fix: For network URLs, some versions of Android prefer setDataSource with headers,
            // or explicitly using the String version. We use the String version for maximum compatibility
            // but wrapped in a try-catch to ensure we can try alternative methods if needed.
            if (url.startsWith("http")) {
                nativeMediaPlayer.setDataSource(url);
            } else if (getContext() != null) {
                nativeMediaPlayer.setDataSource(getContext(), Uri.parse(url));
            } else {
                nativeMediaPlayer.setDataSource(url);
            }
            
            nativeMediaPlayer.setOnPreparedListener(mp -> {
                if (!isAdded()) return;
                isMediaPreparing = false;
                int durationSec = mp.getDuration() / 1000;
                if (audioSeekBar != null) {
                    audioSeekBar.setMax(durationSec);
                }
                mp.start();
                isVideoPlaying = true;
                if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                seekHandler.removeCallbacks(updateSeekBar);
                seekHandler.post(updateSeekBar);
            });

            nativeMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                android.util.Log.e("AudioPlayer", "Error: " + what + ", " + extra);
                isMediaPreparing = false;
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memutar audio: " + extra, Toast.LENGTH_SHORT).show();
                }
                isVideoPlaying = false;
                seekHandler.removeCallbacks(updateSeekBar);
                if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                return true;
            });

            nativeMediaPlayer.setOnCompletionListener(mp -> {
                if (!isAdded()) return;
                isVideoPlaying = false;
                if (btnAudioPlayPause != null) btnAudioPlayPause.setImageResource(android.R.drawable.ic_media_play);
                if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                if (audioSeekBar != null) audioSeekBar.setProgress(0);
                if (tvAudioCurrentTime != null) tvAudioCurrentTime.setText("00:00");
                seekHandler.removeCallbacks(updateSeekBar);
                try { mp.seekTo(0); } catch (Exception ignored) {}
            });

            nativeMediaPlayer.prepareAsync();
            
        } catch (Exception e) {
            android.util.Log.e("AudioPlayer", "Exception: " + e.getMessage());
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Kesalahan inisialisasi audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playVideoFile(String url) {
        youTubePlayerView.setVisibility(View.GONE);
        nativeVideoView.setVisibility(View.VISIBLE);
        
        try {
            // Fix: Use setVideoURI with empty headers to bypass some ContentResolver issues
            // that occur when passing only a Uri on certain Android versions/devices.
            nativeVideoView.setVideoURI(Uri.parse(url), new java.util.HashMap<>());
            
            nativeVideoView.setOnPreparedListener(mp -> {
                if (!isAdded()) return;
                isMediaPreparing = false;
                int durationSec = nativeVideoView.getDuration() / 1000;
                if (videoSeekBar != null) videoSeekBar.setMax(durationSec);
                
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
                android.util.Log.e("VideoPlayer", "Error: " + what + ", " + extra);
                isMediaPreparing = false;
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Gagal memutar video. Cek koneksi Anda.", Toast.LENGTH_SHORT).show();
                }
                isVideoPlaying = false;
                seekHandler.removeCallbacks(updateSeekBar);
                if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                return true;
            });
            
            nativeVideoView.setOnCompletionListener(mp -> {
                if (!isAdded()) return;
                isVideoPlaying = false;
                if (ivVideoPlayPause != null) ivVideoPlayPause.setImageResource(android.R.drawable.ic_media_play);
                if (videoSeekBar != null) videoSeekBar.setProgress(0);
                if (tvVideoCurrentTime != null) tvVideoCurrentTime.setText("00:00");
                seekHandler.removeCallbacks(updateSeekBar);
            });
            
        } catch (Exception e) {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Format link video tidak valid", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String extractId(String url) {
        if (url == null || url.trim().isEmpty()) return "";
        url = url.trim();
        if (url.length() == 11 && !url.contains("/") && !url.contains("=")) {
            return url;
        }
        String pattern = "(?i)(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|\\S*?[?&]v=)|youtu\\.be\\/|youtube\\.com\\/shorts\\/)([a-zA-Z0-9_-]{11})";
        java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        try {
            if (url.contains("v=")) {
                int start = url.indexOf("v=") + 2;
                int end = url.indexOf("&", start);
                if (end == -1) end = url.length();
                String id = url.substring(start, end);
                if (id.length() >= 11) return id.substring(0, 11);
            } else if (url.contains("youtu.be/")) {
                int start = url.indexOf("youtu.be/") + 9;
                int end = url.indexOf("?", start);
                if (end == -1) end = url.length();
                String id = url.substring(start, end);
                if (id.length() >= 11) return id.substring(0, 11);
            } else if (url.contains("embed/")) {
                int start = url.indexOf("embed/") + 6;
                int end = url.indexOf("?", start);
                if (end == -1) end = url.length();
                String id = url.substring(start, end);
                if (id.length() >= 11) return id.substring(0, 11);
            }
        } catch (Exception ignored) {}
        return "";
    }

    private void cyclePlaybackRate() {
        int currentIndex = 1; 
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
    }

    @Override
    public void onStart() {
        super.onStart();
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
        seekHandler.removeCallbacks(updateSeekBar);
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
            nativeMediaPlayer.release();
            nativeMediaPlayer = null;
        }
    }
}
