package com.example.neuramusic.auth;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.SupabaseSignupResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {

    private static final String TAG = "TokenAuthenticator";

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Log.d(TAG, "Intercepted 401, attempting to refresh token...");

        String refreshToken = AuthSession.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            Log.e(TAG, "Refresh token is null or empty.");
            return null;
        }

        // Preparar el cuerpo para el refresh
        Map<String, String> headers = new HashMap<>();
        Map<String, String> body = new HashMap<>();
        body.put("refresh_token", refreshToken);

        SupabaseService service = RetrofitClient.getAuthClient().create(SupabaseService.class);
        Call<SupabaseSignupResponse> call = service.refreshAccessToken(headers, body);

        try {
            retrofit2.Response<SupabaseSignupResponse> tokenResponse = call.execute();
            if (tokenResponse.isSuccessful() && tokenResponse.body() != null) {
                SupabaseSignupResponse newSession = tokenResponse.body();

                // Actualizar el AuthSession
                AuthSession.setAccessToken(newSession.access_token);
                AuthSession.setRefreshToken(newSession.refresh_token);
                AuthSession.uid = newSession.user.id;

                Log.d(TAG, "Token actualizado correctamente");

                // Reintentar la petición con el nuevo token
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newSession.access_token)
                        .build();
            } else {
                Log.e(TAG, "No se pudo renovar el token. Código: " + tokenResponse.code());
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al renovar el token: " + e.getMessage());
            return null;
        }
    }
}
