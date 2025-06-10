package com.example.neuramusic.activities;



import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.TextPost;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTextActivity extends AppCompatActivity {
    private static final String TAG = "AddTextActivity";
    private static final int MAX_CHARS = 280;
    
    private EditText etContent;
    private TextView tvCharCount;
    private MaterialButton btnPublish;
    private ImageButton btnClose;
    private SupabaseService supabaseService;
    private SharedPreferences prefs;
    private int colorNormal;
    private int colorWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);
        
        initViews();
        setupColors();
        setupListeners();
        
        prefs = getSharedPreferences("NeuraPrefs", MODE_PRIVATE);
        supabaseService = RetrofitClient.getClient().create(SupabaseService.class);
    }

    private void initViews() {
        etContent = findViewById(R.id.etContent);
        tvCharCount = findViewById(R.id.tvCharCount);
        btnPublish = findViewById(R.id.btnPublish);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupColors() {
        colorNormal = ContextCompat.getColor(this, R.color.text_secondary);
        colorWarning = ContextCompat.getColor(this, R.color.accent_blue);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        
        btnPublish.setOnClickListener(v -> {
            btnPublish.setEnabled(false);
            animateButton(false);
            publishText();
        });
        
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                updateCharCount(length);
                boolean isEnabled = length > 0 && length <= MAX_CHARS;
                if (btnPublish.isEnabled() != isEnabled) {
                    btnPublish.setEnabled(isEnabled);
                    animateButton(isEnabled);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateCharCount(int length) {
        tvCharCount.setText(length + "/" + MAX_CHARS);
        
        // Animar el color del contador cuando se acerca al límite
        float percentage = (float) length / MAX_CHARS;
        if (percentage > 0.8f) {
            animateTextColor(tvCharCount, colorNormal, colorWarning);
        } else if (percentage <= 0.8f) {
            animateTextColor(tvCharCount, colorWarning, colorNormal);
        }
    }

    private void animateButton(boolean enabled) {
        btnPublish.animate()
                .alpha(enabled ? 1f : 0.6f)
                .scaleX(enabled ? 1f : 0.95f)
                .scaleY(enabled ? 1f : 0.95f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateTextColor(TextView textView, int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> 
            textView.setTextColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private void publishText() {
        String content = etContent.getText().toString().trim();
        String userId = prefs.getString("user_id", null);
        String token = prefs.getString("access_token", null);

        if (userId == null || token == null) {
            Toast.makeText(this, "Sesión expirada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextPost post = new TextPost(userId, content);

        Map<String, String> headers = new HashMap<>();
        headers.put("apikey", RetrofitClient.API_KEY);
        headers.put("Authorization", "Bearer " + token);
        headers.put("Content-Type", "application/json");
        headers.put("Prefer", "return=minimal");

        supabaseService.createTextPost(headers, post)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddTextActivity.this, "Publicado con éxito", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            btnPublish.setEnabled(true);
                            animateButton(true);
                            Toast.makeText(AddTextActivity.this, "Error al publicar", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        btnPublish.setEnabled(true);
                        animateButton(true);
                        Toast.makeText(AddTextActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

} 