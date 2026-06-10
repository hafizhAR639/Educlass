package com.belajar.myapplication.admin;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.belajar.myapplication.R;
import com.belajar.myapplication.data.models.ModelNotification;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminNotificationActivity extends AppCompatActivity {

    private RecyclerView rvToday, rvYesterday;
    private List<ModelNotification> listToday = new ArrayList<>();
    private List<ModelNotification> listYesterday = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity_notifications);

        findViewById(R.id.btn_back_notif).setOnClickListener(v -> finish());

        rvToday = findViewById(R.id.rv_notif_today);
        rvYesterday = findViewById(R.id.rv_notif_yesterday);

        rvToday.setLayoutManager(new LinearLayoutManager(this));
        rvYesterday.setLayoutManager(new LinearLayoutManager(this));

        loadMockData();

        rvToday.setAdapter(new AdminAdapterNotification(listToday));
        rvYesterday.setAdapter(new AdminAdapterNotification(listYesterday));
    }

    private void loadMockData() {
        // Today
        ModelNotification n1 = new ModelNotification("User Baru Bergabung", "Zulfa Afifah mendaftar sebagai siswa baru", "user", new Timestamp(new Date()));
        n1.setRead(false);
        listToday.add(n1);

        ModelNotification n2 = new ModelNotification("Modul Selesai", "Materi Aljabar diselesaikan oleh 12 siswa", "modul", new Timestamp(new Date(System.currentTimeMillis() - 15 * 60000)));
        n2.setRead(false);
        listToday.add(n2);

        ModelNotification n3 = new ModelNotification("Progress Meningkat", "Rata-rata progress naik 15% minggu ini", "progress", new Timestamp(new Date(System.currentTimeMillis() - 3600000)));
        n3.setRead(true);
        listToday.add(n3);

        // Yesterday
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        ModelNotification n4 = new ModelNotification("Modul Baru Ditambahkan", "Modul Fisika: Gerak Lurus berhasil ditambahkan", "modul_new", new Timestamp(cal.getTime()));
        n4.setRead(true);
        listYesterday.add(n4);

        ModelNotification n5 = new ModelNotification("10 User Aktif", "10 siswa menyelesaikan materi hari ini", "user_active", new Timestamp(new Date(cal.getTimeInMillis() - 3600000)));
        n5.setRead(true);
        listYesterday.add(n5);
    }
}
