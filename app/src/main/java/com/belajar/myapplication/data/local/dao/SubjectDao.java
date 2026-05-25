package com.belajar.myapplication.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.belajar.myapplication.data.models.ModelSubject;
import java.util.List;

/**
 * Data Access Object (DAO) untuk tabel subjects.
 * Berfungsi sebagai "Remot" untuk melakukan query ke SQLite.
 */
@Dao
public interface SubjectDao {

    // Mengambil semua data mata pelajaran, diurutkan berdasarkan field 'order'
    @Query("SELECT * FROM subjects ORDER BY `order` ASC")
    List<ModelSubject> getAllSubjects();

    // Menyimpan banyak data sekaligus ke database lokal.
    // OnConflictStrategy.REPLACE: Jika ada ID yang sama, data lama akan ditimpa (Sync).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubjects(List<ModelSubject> subjects);

    // Menghapus seluruh isi tabel subjects (biasanya dipanggil saat refresh total)
    @Query("DELETE FROM subjects")
    void deleteAll();
}
