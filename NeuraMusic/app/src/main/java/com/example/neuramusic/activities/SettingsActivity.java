package com.example.neuramusic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;

public class SettingsActivity extends AppCompatActivity {

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        // Eliminar datos guardados
        SharedPreferences prefs = getSharedPreferences("NeuraPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Ir al login
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Evita volver atrás
        startActivity(intent);
    }
}
