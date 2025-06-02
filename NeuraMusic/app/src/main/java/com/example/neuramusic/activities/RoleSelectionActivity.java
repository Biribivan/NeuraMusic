package com.example.neuramusic.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;
import com.example.neuramusic.activities.LoginActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NeuraPrefs";
    private static final String KEY_ROLE = "user_role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        Button btnArtist = findViewById(R.id.btnArtist);
        Button btnPromoter = findViewById(R.id.btnPromoter);

        btnArtist.setOnClickListener(v -> selectRoleAndContinue("artista"));
        btnPromoter.setOnClickListener(v -> selectRoleAndContinue("promotor"));
    }

    private void selectRoleAndContinue(String role) {
        // Guardar temporalmente el rol
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ROLE, role).apply();

        // Ir al Login
        Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
