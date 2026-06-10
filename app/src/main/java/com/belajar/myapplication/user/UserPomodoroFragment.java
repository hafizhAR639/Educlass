package com.belajar.myapplication.user;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.belajar.myapplication.R;

public class UserPomodoroFragment extends Fragment {

    private TextView tvTimer;
    private Button btnStartPause, btnReset;
    private CountDownTimer countDownTimer;
    private boolean timerRunning;
    private long timeLeftInMillis = 1500000; // 25 menit default

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Karena layout belum ada, kita bisa gunakan layout placeholder atau buat sederhana di sini
        return inflater.inflate(R.layout.user_fragment_home, container, false); // Placeholder layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Logika timer akan ditambahkan setelah layout XML dibuat
    }
}
