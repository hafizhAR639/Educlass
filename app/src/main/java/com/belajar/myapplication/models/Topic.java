package com.belajar.myapplication.models;

public class Topic {
    private String topic_id;
    private String subject_id;
    private String judul;
    private long views_count;

    public Topic() {}

    public String getTopic_id() { return topic_id; }
    public void setTopic_id(String topic_id) { this.topic_id = topic_id; }

    public String getSubject_id() { return subject_id; }
    public void setSubject_id(String subject_id) { this.subject_id = subject_id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public long getViews_count() { return views_count; }
    public void setViews_count(long views_count) { this.views_count = views_count; }
}
