package com.belajar.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DatabaseHelper mengurus semua urusan SQLite:
 * - Buat database & tabel
 * - Insert user baru (register)
 * - Cek user login
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Nama file database yang tersimpan di internal storage HP
    private static final String DATABASE_NAME    = "educlass.db";
    private static final int    DATABASE_VERSION = 1;
    // Nama tabel & kolom
    private static final String TABLE_USERS  = "users";
    private static final String COL_ID       = "id";
    private static final String COL_FULLNAME = "full_name";
    private static final String COL_EMAIL    = "email";
    private static final String COL_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Dipanggil sekali saat database pertama kali dibuat
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FULLNAME + " TEXT NOT NULL, "
                + COL_EMAIL    + " TEXT NOT NULL UNIQUE, "
                + COL_PASSWORD + " TEXT NOT NULL"
                + ")";
        db.execSQL(createTable);
    }

    // Dipanggil kalau DATABASE_VERSION dinaikkan (untuk migrasi)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // =====================================================================
    //  REGISTER: simpan user baru
    //  Return true  → berhasil
    //  Return false → email sudah terdaftar
    // =====================================================================
    public boolean registerUser(String fullName, String email, String password) {
        // Cek dulu apakah email sudah ada
        if (isEmailExist(email)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULLNAME, fullName);
        values.put(COL_EMAIL,    email);
        values.put(COL_PASSWORD, password); // simpan langsung (produksi: pakai hash)

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result != -1; // -1 berarti insert gagal
    }

    // =====================================================================
    //  LOGIN: cek email + password cocok atau tidak
    //  Return true  → cocok
    //  Return false → tidak cocok
    // =====================================================================
    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,                          // ambil semua kolom
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password}, // parameter query
                null, null, null
        );

        boolean found = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return found;
    }

    // =====================================================================
    //  Cek apakah email sudah terdaftar
    // =====================================================================
    private boolean isEmailExist(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );

        boolean exist = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exist;
    }
    public void checkAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);

        if (cursor.moveToFirst()) {
            do {
                // Cetak data ke Logcat
                String info = "ID: " + cursor.getInt(0) +
                        " | Name: " + cursor.getString(1) +
                        " | Email: " + cursor.getString(2);
                android.util.Log.d("DB_DEBUG", info);
            } while (cursor.moveToNext());
        } else {
            android.util.Log.d("DB_DEBUG", "Database masih kosong.");
        }
        cursor.close();
    }
}