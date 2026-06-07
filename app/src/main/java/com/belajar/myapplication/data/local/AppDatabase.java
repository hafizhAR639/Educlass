package com.belajar.myapplication.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.belajar.myapplication.data.local.dao.SubjectDao;
import com.belajar.myapplication.data.models.ModelSubject;

/**
 * Kelas Database Utama menggunakan Room.
 * Kita mendefinisikan tabel apa saja yang ada di dalam database ini.
 */
@Database(entities = {ModelSubject.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Akses ke DAO Subject
    public abstract SubjectDao subjectDao();

    private static AppDatabase instance;

    /**
     * Pola Singleton: Memastikan hanya ada 1 koneksi database yang terbuka di aplikasi.
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "educlass_db")
                    .fallbackToDestructiveMigration() // Hapus & buat ulang jika versi naik (agar tidak crash saat development)
                    .allowMainThreadQueries() // KISS: Untuk mempermudah testing, kita izinkan di main thread
                    .build();
        }
        return instance;
    }
}
