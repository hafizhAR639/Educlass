package com.belajar.myapplication.data.models;

import com.google.firebase.Timestamp;

public class ModelActivity {
    private String description;
    private Timestamp timestamp;
    private String type; // e.g., "add", "delete", "edit"

    public ModelActivity() {}

    public ModelActivity(String description, String type) {
        this.description = description;
        this.type = type;
        this.timestamp = Timestamp.now();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
