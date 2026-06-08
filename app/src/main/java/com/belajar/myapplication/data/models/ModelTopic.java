package com.belajar.myapplication.data.models;

import com.google.firebase.firestore.PropertyName;

public class ModelTopic {
    private String topic_id;
    private String subject_id;
    private String judul;
    private String deskripsi;
    private String durasi;
    private boolean has_visual;
    private boolean has_audio;
    private boolean has_kinestetik;
    private Object views_count;
    private int progress;
    private boolean isPremium;
    private Object order;
    private java.util.List<String> learning_styles;

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

    public boolean isHas_visual() { return has_visual; }
    public void setHas_visual(boolean has_visual) { this.has_visual = has_visual; }
    public boolean isHas_audio() { return has_audio; }
    public void setHas_audio(boolean has_audio) { this.has_audio = has_audio; }
    public boolean isHas_kinestetik() { return has_kinestetik; }
    public void setHas_kinestetik(boolean has_kinestetik) { this.has_kinestetik = has_kinestetik; }

    public java.util.List<String> getLearning_styles() { return learning_styles; }
    public void setLearning_styles(java.util.List<String> learning_styles) { this.learning_styles = learning_styles; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public long getViews_count_long() {
        if (views_count instanceof Number) {
            return ((Number) views_count).longValue();
        } else if (views_count instanceof String) {
            try {
                return (long) Double.parseDouble((String) views_count);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public String getViews_count() {
        return String.valueOf(getViews_count_long());
    }
    public void setViews_count(Object views_count) { this.views_count = views_count; }

    @PropertyName("is_premium")
    public boolean isPremium() { return isPremium; }
    @PropertyName("is_premium")
    public void setPremium(boolean premium) { isPremium = premium; }

    public long getOrder() {
        if (order instanceof Number) {
            return ((Number) order).longValue();
        } else if (order instanceof String) {
            try {
                return Long.parseLong((String) order);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    public void setOrder(Object order) { this.order = order; }
}
