package com.belajar.myapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.firebase.Timestamp;

/**
 * Model untuk Mata Pelajaran (Subject).
 * Kelas ini berfungsi sebagai TABEL di database lokal (Room).
 */
@Entity(tableName = "subjects")
public class ModelSubject {
    
    @PrimaryKey
    @NonNull
    private String subject_id = ""; // Default empty string agar tidak null
    
    private String nama;
    private String icon_name;
    private String color_hex;
    private String jurusan;
    
    @Ignore // Room tidak mendukung Object dinamis secara default
    private Object total_moduls;
    
    private long order;
    private long access_count;
    
    @Ignore // Room butuh Converter untuk Timestamp
    private Timestamp created_at;

    public ModelSubject() {}
    
    @NonNull
    public String getSubject_id() { return subject_id; }
    public void setSubject_id(@NonNull String subject_id) { this.subject_id = subject_id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getIcon_name() { return icon_name; }
    public void setIcon_name(String icon_name) { this.icon_name = icon_name; }
    public String getColor_hex() { return color_hex; }
    public void setColor_hex(String color_hex) { this.color_hex = color_hex; }
    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getTotal_moduls() {
        return String.valueOf(total_moduls != null ? total_moduls : 0);
    }
    public void setTotal_moduls(Object total_moduls) { this.total_moduls = total_moduls; }
    public long getOrder() { return order; }
    public void setOrder(long order) { this.order = order; }
    public long getAccess_count() { return access_count; }
    public void setAccess_count(long access_count) { this.access_count = access_count; }
    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
