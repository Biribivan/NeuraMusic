package com.example.neuramusic.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    public static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imx4b3hoZG1paHlkam90c2dncGNvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDgzNzk3MjYsImV4cCI6MjA2Mzk1NTcyNn0.Cg4fm9x0NqlkSxtMTvMMFZJ-MNDoN1-u4ymKr7NdzR0";
    private static final String BASE_URL = "https://lxoxhdmihydjotsggpco.supabase.co/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Crear interceptor de logging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
                Log.d(TAG, "HTTP: " + message));
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configurar cliente OkHttp con el interceptor
            OkHttpClient client = new OkHttpClient.Builder()
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
        }
        return retrofit;
    }
}
