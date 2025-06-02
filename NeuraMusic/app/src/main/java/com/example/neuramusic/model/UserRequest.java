package com.example.neuramusic.model;

public class UserRequest {

    private String id; // UUID del usuario
    private String email;
    private String username;
    private String role;
    private String bio;
    private String dob;
    private String instagram;
    private String soundcloud;
    private String spotify;
    private String youtube;
    private boolean isBlocked;
    private boolean isApproved;
    private String imageUrl;
    private String profile_image_url;

    public UserRequest(String id, String email, String username, String role,
                       String bio, String dob, String instagram, String soundcloud,
                       String spotify, String youtube, boolean isBlocked, boolean isApproved, String imageUrl) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
        this.bio = bio;
        this.dob = dob;
        this.instagram = instagram;
        this.soundcloud = soundcloud;
        this.spotify = spotify;
        this.youtube = youtube;
        this.isBlocked = isBlocked;
        this.isApproved = isApproved;
        this.imageUrl = imageUrl;
        this.profile_image_url = profile_image_url;


    }

    // GETTERS
    public String getProfile_image_url() {
        return profile_image_url;
    }
    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
    }
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getBio() {
        return bio;
    }

    public String getDob() {
        return dob;
    }

    public String getInstagram() {
        return instagram;
    }

    public String getSoundcloud() {
        return soundcloud;
    }

    public String getSpotify() {
        return spotify;
    }

    public String getYoutube() {
        return youtube;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isApproved() {
        return isApproved;
    }

    // SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public void setSoundcloud(String soundcloud) {
        this.soundcloud = soundcloud;
    }

    public void setSpotify(String spotify) {
        this.spotify = spotify;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }
}
