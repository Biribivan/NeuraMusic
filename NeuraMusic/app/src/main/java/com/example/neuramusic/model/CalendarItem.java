package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class CalendarItem {

    private String id;
    private String title;
    private String description;
    private String type;
    private String icon;

    @SerializedName("event_date")
    private String eventDate; // Formato: "YYYY-MM-DD"

    @SerializedName("event_time")
    private String eventTime; // Formato: "HH:MM"

    @SerializedName("is_completed")
    private Boolean isCompleted;

    @SerializedName("is_important")
    private Boolean isImportant;

    @SerializedName("label_color")
    private String labelColor;

    private String link;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("repeat")
    private String repeat;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
    public String getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }
    public Boolean getIsCompleted() { return isCompleted; }
    public Boolean getIsImportant() { return isImportant; }
    public String getLabelColor() { return labelColor; }
    public String getLink() { return link; }
    public String getUserId() { return userId; }
    public String getRepeat() { return repeat; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    public void setIsImportant(Boolean isImportant) { this.isImportant = isImportant; }
    public void setLabelColor(String labelColor) { this.labelColor = labelColor; }
    public void setLink(String link) { this.link = link; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setRepeat(String repeat) { this.repeat = repeat; }
}
