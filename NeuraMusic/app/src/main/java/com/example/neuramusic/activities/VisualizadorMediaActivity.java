package com.example.neuramusic.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.neuramusic.R;

public class VisualizadorMediaActivity extends AppCompatActivity {

    private VideoView videoView;
    private ImageView imageView;
    private ImageButton btnClose;
    private ImageButton btnPip;
    private MediaController mediaController;

    public static void launch(AppCompatActivity activity, Uri mediaUri, boolean isVideo) {
        Intent intent = new Intent(activity, VisualizadorMediaActivity.class);
        intent.putExtra("mediaUrl", mediaUri.toString());
        intent.putExtra("mediaType", isVideo ? "video" : "image");
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizador_media);

        // Inicializar vistas
        videoView = findViewById(R.id.videoView);
        imageView = findViewById(R.id.imageView);
        btnClose = findViewById(R.id.btnClose);
        btnPip = findViewById(R.id.btnPip);

        // Configurar controles de video
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        // Configurar botones
        btnClose.setOnClickListener(v -> finish());
        btnPip.setOnClickListener(v -> entrarModoPip());

        // Obtener y reproducir el medio
        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        String mediaType = getIntent().getStringExtra("mediaType");

        if (mediaUrl != null) {
            if ("video".equals(mediaType)) {
                mostrarVideo(mediaUrl);
            } else if ("image".equals(mediaType)) {
                mostrarImagen(mediaUrl);
            }
        }
    }

    private void mostrarVideo(String url) {
        videoView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        btnPip.setVisibility(View.VISIBLE);

        videoView.setVideoPath(url);
        videoView.setOnPreparedListener(MediaPlayer::start);
        videoView.setOnCompletionListener(mp -> videoView.start()); // Reproducir en bucle
    }

    private void mostrarImagen(String url) {
        videoView.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        btnPip.setVisibility(View.GONE);
        // Cargar imagen con Glide
        Glide.with(this).load(url).into(imageView);
    }

    private void entrarModoPip() {
        // La funcionalidad PiP se implementará más adelante
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null && !videoView.isPlaying()) {
            videoView.start();
        }
    }
} 