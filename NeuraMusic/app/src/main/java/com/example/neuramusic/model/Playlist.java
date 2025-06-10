package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Playlist {

    private String id;
    private String uid;
    private String title;

    // Ahora `tags` es una lista, porque Supabase espera un array (text[])
    private List<String> tags;

    private String type;

    @SerializedName("parent_id")
    private String parentId;

    @SerializedName("created_at")
    private String createdAt;

    // --- Campos auxiliares (no persistentes en Supabase) ---

    @SerializedName("isExpanded") // Esto es clave si está en Supabase como camelCase
    private boolean isExpanded = false;

    private int depth = 0;

    // Getters
    public String getId() { return id; }
    public String getUid() { return uid; }
    public String getTitle() { return title; }
    public List<String> getTags() { return tags; }
    public String getType() { return type; }
    public String getParentId() { return parentId; }
    public String getCreatedAt() { return createdAt; }
    public boolean isExpanded() { return isExpanded; }
    public int getDepth() { return depth; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUid(String uid) { this.uid = uid; }
    public void setTitle(String title) { this.title = title; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setType(String type) { this.type = type; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setExpanded(boolean expanded) { this.isExpanded = expanded; }
    public void setDepth(int depth) { this.depth = depth; }
}
