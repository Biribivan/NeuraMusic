package com.example.neuramusic.model;

public class NoteItem {
    private String title;
    private String content;

    public NoteItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
}
