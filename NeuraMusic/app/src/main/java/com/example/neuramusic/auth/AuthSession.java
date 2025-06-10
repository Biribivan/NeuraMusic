package com.example.neuramusic.auth;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.SupabaseSignupResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthSession {

    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4b3hoZG1paHlkam90c2dncGNvIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODM3OTcyNiwiZXhwIjoyMDYzOTU1NzI2fQ.eWRZRHeiIuAA7p6wC6kYULBSXj4y-M9KMaPsOPiRJt8";
    public static String accessToken = null;
    public static String refreshToken = null;
    public static String uid = null;

    public static void setSession(String newAccessToken, String newRefreshToken, String newUid) {
        accessToken = newAccessToken;
        refreshToken = newRefreshToken;
        uid = newUid;
    }

    public static void clear() {
        accessToken = null;
        refreshToken = null;
        uid = null;
    }

    public static boolean isAuthenticated() {
        return accessToken != null && uid != null;
    }

    public static String getBearerToken() {
        return accessToken != null ? "Bearer " + accessToken : null;
    }

    // Nunca expira
    public static boolean isTokenExpired() {
        return false;
    }

    public static boolean refreshSessionIfNeeded() {
        return true;  // Siempre da por válida la sesión
    }

    public static void refreshSession(Runnable onReady) {
        onReady.run();  // Ejecuta directamente sin refrescar
    }

    // Métodos de refresco ya no se usan pero se mantienen para compatibilidad si decides reactivarlos
    public static void refreshToken(Callback<Boolean> callback) {
        Log.d(TAG, "⏩ Token permanente: no se refresca");
        callback.onResponse(null, Response.success(true));
    }

    public static boolean refreshTokenSync() {
        Log.d(TAG, "⏩ Token permanente: no se refresca (sync)");
        return true;
    }
}
