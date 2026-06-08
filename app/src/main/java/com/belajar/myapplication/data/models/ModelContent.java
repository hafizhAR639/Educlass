package com.belajar.myapplication.data.models;

import java.util.Map;

public class ModelContent {
    private String topic_id;
    private String title;
    private String description;
    private java.util.List<String> learning_styles;
    private VisualContent visual;
    private AudioContent audio;
    private KinestetikContent kinestetik;

    public ModelContent() {}

    public String getTopic_id() { return topic_id; }
    public void setTopic_id(String topic_id) { this.topic_id = topic_id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.List<String> getLearning_styles() { return learning_styles; }
    public void setLearning_styles(java.util.List<String> learning_styles) { this.learning_styles = learning_styles; }

    public VisualContent getVisual() { return visual; }
    public void setVisual(VisualContent visual) { this.visual = visual; }

    public AudioContent getAudio() { return audio; }
    public void setAudio(AudioContent audio) { this.audio = audio; }

    public KinestetikContent getKinestetik() { return kinestetik; }
    public void setKinestetik(KinestetikContent kinestetik) { this.kinestetik = kinestetik; }

    public static class VisualContent {
        private String text_content;
        private String video_url;
        private String youtube_url;

        public VisualContent() {}
        public String getText_content() { return text_content; }
        public void setText_content(String text_content) { this.text_content = text_content; }
        public String getVideo_url() { return video_url; }
        public void setVideo_url(String video_url) { this.video_url = video_url; }
        public String getYoutube_url() { return youtube_url; }
        public void setYoutube_url(String youtube_url) { this.youtube_url = youtube_url; }
    }

    public static class AudioContent {
        private String audio_url;
        private String description;
        private String youtube_url;

        public AudioContent() {}
        public String getAudio_url() { return audio_url; }
        public void setAudio_url(String audio_url) { this.audio_url = audio_url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getYoutube_url() { return youtube_url; }
        public void setYoutube_url(String youtube_url) { this.youtube_url = youtube_url; }
    }

    public static class KinestetikContent {
        private String kin_url;
        private String file_url;
        private String description;
        private String youtube_url;

        public KinestetikContent() {}
        public String getKin_url() { return kin_url; }
        public void setKin_url(String kin_url) { this.kin_url = kin_url; }
        public String getFile_url() { return file_url; }
        public void setFile_url(String file_url) { this.file_url = file_url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getYoutube_url() { return youtube_url; }
        public void setYoutube_url(String youtube_url) { this.youtube_url = youtube_url; }
    }
}
