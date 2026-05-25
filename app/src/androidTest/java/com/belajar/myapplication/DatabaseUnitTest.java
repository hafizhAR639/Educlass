package com.belajar.myapplication;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.belajar.myapplication.data.local.AppDatabase;
import com.belajar.myapplication.data.local.dao.SubjectDao;
import com.belajar.myapplication.data.models.ModelSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit Test untuk menguji fungsionalitas Database (Room).
 * Menguji alur: Insert, Update, dan View (Select).
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseUnitTest {

    private AppDatabase db;
    private SubjectDao subjectDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Menggunakan in-memory database agar data tidak permanen dan bersih setiap tes
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        subjectDao = db.subjectDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void testFullDataCycle() {
        // 1. DATA INSERT (FORM INSERT)
        ModelSubject subject = new ModelSubject();
        subject.setSubject_id("TEST_001");
        subject.setNama("Matematika");
        subject.setOrder(1);

        subjectDao.insertSubjects(Collections.singletonList(subject));

        // 2. DATA VIEW AFTER INSERT (VERIFIKASI INSERT)
        List<ModelSubject> listAfterInsert = subjectDao.getAllSubjects();
        assertEquals(1, listAfterInsert.size());
        assertEquals("Matematika", listAfterInsert.get(0).getNama());

        // 3. DATA UPDATE (FORM UPDATE)
        // Kita timpa data ID yang sama dengan nama baru
        subject.setNama("Matematika Lanjut");
        subjectDao.insertSubjects(Collections.singletonList(subject));

        // 4. DATA VIEW AFTER UPDATE (VERIFIKASI UPDATE)
        List<ModelSubject> listAfterUpdate = subjectDao.getAllSubjects();
        assertEquals(1, listAfterUpdate.size()); // Size tetap 1 karena ID sama (Replace)
        assertEquals("Matematika Lanjut", listAfterUpdate.get(0).getNama());
    }
}
