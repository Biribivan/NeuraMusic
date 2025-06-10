package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class CalendarNote {

    private String id;
    private String title;
    private String content;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("note_date")
    private String noteDate;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUserId() {
        return userId;
    }

    public String getNoteDate() {
        return noteDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNoteDate(String noteDate) {
        this.noteDate = noteDate;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
