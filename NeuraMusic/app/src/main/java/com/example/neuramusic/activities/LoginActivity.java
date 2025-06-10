package com.example.neuramusic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.AuthRequest;
import com.example.neuramusic.model.SupabaseSignupResponse;
import com.example.neuramusic.model.UserResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ImageButton btnTogglePassword;
    private TextView tvRegisterLink;
    private boolean isPasswordVisible = false;

    private SupabaseService supabaseService;
    private static final String SUPABASE_API_KEY = RetrofitClient.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        RetrofitClient.init(getApplicationContext());

        SupabaseService authService = RetrofitClient.getSupabaseAuthService();
        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> attemptLogin(authService));
        tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        checkSessionAndAutoLogin();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin(SupabaseService authService) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email no válido");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Contraseña demasiado corta");
            return;
        }

        if (isAdminLogin(email, password)) {
            startActivity(new Intent(this, AdminHomeActivity.class));
            finish();
            return;
        }

        AuthRequest authRequest = new AuthRequest(email, password);

        authService.login(authRequest, SUPABASE_API_KEY).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        SupabaseSignupResponse loginResp = SupabaseSignupResponse.fromJson(json);

                        if (loginResp == null || loginResp.user == null || loginResp.user.id == null) {
                            showToast("Respuesta inválida del servidor");
                            return;
                        }

                        String uid = loginResp.user.id;
                        String token = loginResp.access_token;

                        RetrofitClient.setUid(uid);
                        RetrofitClient.setAccessToken(token);

                        fetchUserData(uid, token);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing login JSON", e);
                        showToast("Error procesando la respuesta");
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("Invalid login credentials")) {
                            showToast("Credenciales inválidas");
                        } else if (errorBody.contains("Email not confirmed")) {
                            showToast("Confirma tu correo primero");
                        } else {
                            showToast("Error al iniciar sesión");
                        }
                    } catch (Exception e) {
                        showToast("Error inesperado");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Login error", t);
                showToast("Error de red al iniciar sesión");
            }
        });
    }

    private void fetchUserData(String uid, String accessToken) {
        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + uid);

        supabaseService.getUserById(query, SUPABASE_API_KEY, "Bearer " + accessToken)
                .enqueue(new Callback<List<UserResponse>>() {
                    @Override
                    public void onResponse(Call<List<UserResponse>> call, Response<List<UserResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            UserResponse user = response.body().get(0);

                            if (user.isBlocked) {
                                showToast("Cuenta bloqueada");
                                return;
                            }

                            if ("artista".equals(user.role)) {
                                launchActivity(ArtistHomeActivity.class, uid, accessToken, user.role);
                            } else if ("promotor".equals(user.role)) {
                                if (!user.isApproved) {
                                    showToast("Cuenta de promotor no aprobada");
                                    return;
                                }
                                launchActivity(PromoterHomeActivity.class, uid, accessToken, user.role);
                            } else {
                                showToast("Rol inválido. Contacta con soporte.");
                            }

                        } else {
                            showToast("Usuario no encontrado o respuesta inválida");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserResponse>> call, Throwable t) {
                        Log.e(TAG, "Error al obtener usuario", t);
                        showToast("Fallo de red al obtener usuario");
                    }
                });
    }

    private void launchActivity(Class<?> activityClass, String uid, String token, String role) {
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("user_id", uid)
                .putString("access_token", token)
                .putString("user_role", role)
                .apply();

        Intent intent = new Intent(this, activityClass);
        intent.putExtra("user_id", uid);
        startActivity(intent);
        finish();
    }

    private boolean isAdminLogin(String email, String password) {
        return email.equals("admin@neuramusic.com") && password.equals("admin123");
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void checkSessionAndAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("user_id", null);
        String token = prefs.getString("access_token", null);
        String role = prefs.getString("user_role", null);

        if (uid != null && token != null && role != null) {
            RetrofitClient.setUid(uid);
            RetrofitClient.setAccessToken(token);

            Class<?> target;

            switch (role) {
                case "artista":
                    target = ArtistHomeActivity.class;
                    break;
                case "promotor":
                    target = PromoterHomeActivity.class;
                    break;
                case "admin":
                    target = AdminHomeActivity.class;
                    break;
                default:
                    target = null;
                    break;
            }

            if (target != null) {
                Intent intent = new Intent(this, target);
                intent.putExtra("user_id", uid);
                startActivity(intent);
                finish();
            } else {
                showToast("Rol inválido, haz login manual.");
            }
        }
    }
}
