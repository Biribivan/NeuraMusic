package com.example.neuramusic.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SupabaseSignupResponse {

    @SerializedName("access_token")
    public String access_token;

    @SerializedName("refresh_token")
    public String refresh_token;

    @SerializedName("token_type")
    public String token_type;

    @SerializedName("expires_in")
    public int expires_in;

    @SerializedName("user")
    public SupabaseUser user;

    // Método para parsear JSON fácilmente
    public static SupabaseSignupResponse fromJson(String json) {
        try {
            return new Gson().fromJson(json, SupabaseSignupResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class SupabaseUser {
        @SerializedName("id")
        public String id;

        @SerializedName("email")
        public String email;

        // Agrega otros campos si los necesitas
    }
}
