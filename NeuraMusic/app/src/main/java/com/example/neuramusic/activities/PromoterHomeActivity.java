package com.example.neuramusic.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neuramusic.R;

public class PromoterHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promoter_home);

        TextView welcomeText = findViewById(R.id.tvWelcomePromoter);
        String userId = getIntent().getStringExtra("user_id");
        welcomeText.setText("¡Bienvenido/a promotor!\nUID: " + userId);
    }
}
