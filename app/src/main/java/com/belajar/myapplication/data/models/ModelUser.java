package com.belajar.myapplication.data.models;

public class ModelUser {
    private String uid;
    private String nama;
    private String email;
    private String role;

    public ModelUser() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
