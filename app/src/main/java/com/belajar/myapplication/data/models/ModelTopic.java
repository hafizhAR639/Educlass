package com.belajar.myapplication.data.models;

public class ModelTopic {
    private String topic_id;
    private String subject_id;
    private String judul;
    private String deskripsi;
    private String durasi;
    private Object views_count;
    private int progress;

    public ModelTopic() {}
    public String getTopic_id() { return topic_id; }
    public void setTopic_id(String topic_id) { this.topic_id = topic_id; }
    public String getSubject_id() { return subject_id; }
    public void setSubject_id(String subject_id) { this.subject_id = subject_id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getDurasi() { return durasi; }
    public void setDurasi(String durasi) { this.durasi = durasi; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getViews_count() {
        return String.valueOf(views_count != null ? views_count : 0);
    }
    public void setViews_count(Object views_count) { this.views_count = views_count; }
}
