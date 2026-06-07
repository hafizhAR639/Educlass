package com.belajar.myapplication.data.models;

import com.google.firebase.Timestamp;

public class ModelNotification {
    private String title;
    private String description;
    private Timestamp timestamp;
    private String type; // e.g., "user", "modul", "progress"
    private boolean isRead;

    public ModelNotification() {}

    public ModelNotification(String title, String description, String type, Timestamp timestamp) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
