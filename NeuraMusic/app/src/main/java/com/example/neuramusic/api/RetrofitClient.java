package com.example.neuramusic.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.neuramusic.auth.AuthSession;
import com.example.neuramusic.auth.TokenAuthenticator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4b3hoZG1paHlkam90c2dncGNvIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc0ODM3OTcyNiwiZXhwIjoyMDYzOTU1NzI2fQ.eWRZRHeiIuAA7p6wC6kYULBSXj4y-M9KMaPsOPiRJt8";


    private static final String BASE_URL = "https://lxoxhdmihydjotsggpco.supabase.co/";
    private static Retrofit retrofit = null;
    private static SharedPreferences prefs;

    public static void init(Context context) {
        prefs = context.getSharedPreferences("NeuraPrefs", Context.MODE_PRIVATE);

        AuthSession.setSession(
                getAccessToken(),
                getRefreshToken(),
                getCurrentUid()
        );
    }
    public static Retrofit getAuthClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d(TAG, "HTTP: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("apikey", API_KEY)
                            .header("Content-Type", "application/json")
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }


    public static Retrofit getClient() {
        if (retrofit != null) return retrofit;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                Log.d(TAG, "HTTP: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .authenticator(new TokenAuthenticator())  // 🔁 Manejo automático de 401
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("apikey", API_KEY);

                    String bearer = AuthSession.getBearerToken();
                    if (bearer != null) {
                        requestBuilder.header("Authorization", bearer);
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit;
    }

    public static SupabaseService getSupabaseAuthService() {
        return getClient().create(SupabaseService.class);
    }

    public static String getAccessToken() {
        return prefs != null ? prefs.getString("access_token", null) : null;
    }

    public static void setAccessToken(String token) {
        if (prefs != null) prefs.edit().putString("access_token", token).apply();
    }

    public static String getRefreshToken() {
        return prefs != null ? prefs.getString("refresh_token", null) : null;
    }

    public static void setRefreshToken(String token) {
        if (prefs != null) prefs.edit().putString("refresh_token", token).apply();
    }

    public static String getCurrentUid() {
        return prefs != null ? prefs.getString("user_id", null) : null;
    }

    public static void setUid(String uid) {
        if (prefs != null) prefs.edit().putString("user_id", uid).apply();
    }
}
