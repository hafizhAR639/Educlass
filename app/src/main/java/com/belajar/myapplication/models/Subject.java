package com.belajar.myapplication.models;

import com.google.firebase.Timestamp;

public class Subject {
    private String subject_id;
    private String nama;
    private String icon_name;
    private String color_hex;
    private long total_moduls;
    private long order;
    private Timestamp created_at;

    public Subject() {}

    public String getSubject_id() { return subject_id; }
    public void setSubject_id(String subject_id) { this.subject_id = subject_id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getIcon_name() { return icon_name; }
    public void setIcon_name(String icon_name) { this.icon_name = icon_name; }

    public String getColor_hex() { return color_hex; }
    public void setColor_hex(String color_hex) { this.color_hex = color_hex; }

    public long getTotal_moduls() { return total_moduls; }
    public void setTotal_moduls(long total_moduls) { this.total_moduls = total_moduls; }

    public long getOrder() { return order; }
    public void setOrder(long order) { this.order = order; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
