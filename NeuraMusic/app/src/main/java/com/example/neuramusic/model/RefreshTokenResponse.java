package com.example.neuramusic.model;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenResponse {

    @SerializedName("access_token")
    public String accessToken;

    @SerializedName("refresh_token")
    public String refreshToken;

    @SerializedName("token_type")
    public String tokenType;

    @SerializedName("expires_in")
    public int expiresIn;
}
