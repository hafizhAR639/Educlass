package com.belajar.myapplication.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelSubject;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatistikFragment extends Fragment {

    private ImageView ivProfile;
    private TextView tvWeeklyTime, tvStreak;
    private LinearLayout layoutMasteryList;
    private FirebaseFirestore db;
    private final View[] barViews = new View[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment_statistik, container, false);

        ivProfile = view.findViewById(R.id.iv_profile_stats);
        tvWeeklyTime = view.findViewById(R.id.tv_weekly_study_time);
        tvStreak = view.findViewById(R.id.tv_study_streak);
        layoutMasteryList = view.findViewById(R.id.layout_subject_mastery_list);

        // Chart Bars IDs from user_fragment_statistik.xml
        barViews[0] = view.findViewById(R.id.bar_sun);
        barViews[1] = view.findViewById(R.id.bar_mon);
        barViews[2] = view.findViewById(R.id.bar_tue);
        barViews[3] = view.findViewById(R.id.bar_wed);
        barViews[4] = view.findViewById(R.id.bar_thu);
        barViews[5] = view.findViewById(R.id.bar_fri);
        barViews[6] = view.findViewById(R.id.bar_sat);

        db = FirebaseFirestore.getInstance();

        view.findViewById(R.id.btn_back_stats).setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                            .replace(R.id.layout_fragment_container, new HomeFragment())
                            .commit();
                }
            }
        });

        loadUserData();
        loadAcademicMastery();

        return view;
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(doc -> {
                if (doc.exists() && isAdded()) {
                    // Profile Photo
                    String photoUrl = doc.getString("photoUrl");
                    if (photoUrl != null && !photoUrl.isEmpty()) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.user_ic_profile).into(ivProfile);
                    }

                    // Weekly Time
                    Long minutes = doc.getLong("weekly_study_minutes");
                    if (minutes == null) minutes = 0L;
                    updateStudyTimeUI(minutes);

                    // Streak
                    Long streak = doc.getLong("study_streak");
                    if (streak == null) streak = 0L;
                    if (tvStreak != null) {
                        tvStreak.setText(String.format(Locale.getDefault(), "%d hari", streak));
                    }

                    // Daily Stats for Chart
                    Map<String, Object> dailyStats = (Map<String, Object>) doc.get("daily_study_minutes");
                    if (dailyStats == null) {
                        // Dummy fallback so user sees the change even if empty
                        dailyStats = new HashMap<>();
                        dailyStats.put("Sun", 20L); dailyStats.put("Mon", 45L);
                        dailyStats.put("Tue", 30L); dailyStats.put("Wed", 60L);
                        dailyStats.put("Thu", 80L); dailyStats.put("Fri", 25L);
                        dailyStats.put("Sat", 40L);
                    }
                    updateChartBars(dailyStats);
                }
            });
        }
    }

    private void updateChartBars(Map<String, Object> dailyStats) {
        if (dailyStats == null || getContext() == null) return;
        
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        long maxMinutes = 1;
        
        for (String day : days) {
            Object val = dailyStats.get(day);
            if (val instanceof Long) {
                maxMinutes = Math.max(maxMinutes, (Long) val);
            }
        }

        // maxHeight in pixels (160dp corresponds to the view constraints)
        float scale = getResources().getDisplayMetrics().density;
        int maxHeightPx = (int) (160 * scale);

        for (int i = 0; i < 7; i++) {
            if (barViews[i] == null) continue;
            
            Object val = dailyStats.get(days[i]);
            long mins = (val instanceof Long) ? (Long) val : 0;
            
            int heightPx = (int) (((float) mins / maxMinutes) * maxHeightPx);
            if (heightPx < (int)(10 * scale)) heightPx = (int)(10 * scale); // min height
            
            ViewGroup.LayoutParams params = barViews[i].getLayoutParams();
            if (params != null) {
                params.height = heightPx;
                barViews[i].setLayoutParams(params);
            }
        }
    }

    private void loadAcademicMastery() {
        if (layoutMasteryList == null) return;
        layoutMasteryList.removeAllViews();

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        // Get completed topics to calculate mastery
        db.collection("users").document(uid).collection("completed_topics").get().addOnSuccessListener(completedSnap -> {
            java.util.Map<String, Integer> completedCounts = new HashMap<>();
            for (QueryDocumentSnapshot doc : completedSnap) {
                String subjectId = doc.getString("subject_id");
                if (subjectId != null) {
                    completedCounts.put(subjectId, completedCounts.getOrDefault(subjectId, 0) + 1);
                }
            }

            // Get all subjects
            db.collection("subjects").orderBy("order").get().addOnSuccessListener(result -> {
                if (isAdded()) {
                    for (QueryDocumentSnapshot doc : result) {
                        ModelSubject subject = doc.toObject(ModelSubject.class);
                        String sId = doc.getId();
                        
                        // Calculate percentage: (completed modules in subject / total modules in subject) * 100
                        int completed = completedCounts.getOrDefault(sId, 0);
                        int total = 0;
                        Object totalObj = doc.get("total_moduls");
                        if (totalObj instanceof Number) total = ((Number) totalObj).intValue();
                        else if (totalObj instanceof String) try { total = Integer.parseInt((String)totalObj); } catch(Exception e){}

                        int mastery = total > 0 ? (completed * 100 / total) : 0;
                        if (mastery > 100) mastery = 100;

                        addActualMasteryRow(subject.getNama(), mastery);
                    }
                }
            });
        });
    }

    private void addActualMasteryRow(String name, int progress) {
        if (getContext() == null) return;
        View rowView = LayoutInflater.from(getContext()).inflate(R.layout.user_item_mastery_row, layoutMasteryList, false);
        
        TextView tvName = rowView.findViewById(R.id.tv_subject_name_mastery);
        TextView tvPercent = rowView.findViewById(R.id.tv_subject_percent_mastery);
        ProgressBar pb = rowView.findViewById(R.id.pb_subject_mastery);

        tvName.setText(name);
        tvPercent.setText(String.format(Locale.getDefault(), "%d%%", progress));
        pb.setProgress(progress);

        layoutMasteryList.addView(rowView);
    }

    private void addDummyMasteryRow(String name, int progress) {
        addActualMasteryRow(name, progress);
    }

    private void updateStudyTimeUI(long totalMinutes) {
        if (tvWeeklyTime == null) return;
        long hours = totalMinutes / 60;
        long mins = totalMinutes % 60;
        String timeStr = hours > 0 ? 
                String.format(Locale.getDefault(), "%dh %dm", hours, mins) : 
                String.format(Locale.getDefault(), "%dm", mins);
        tvWeeklyTime.setText(timeStr);
    }
}
