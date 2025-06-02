package com.example.neuramusic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pantalla completa
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        TextView tvAppName = findViewById(R.id.tvAppName);

        // Animación fade-in
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        tvAppName.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, RoleSelectionActivity.class));
            finish();
        }, SPLASH_TIME_OUT);
    }
}
