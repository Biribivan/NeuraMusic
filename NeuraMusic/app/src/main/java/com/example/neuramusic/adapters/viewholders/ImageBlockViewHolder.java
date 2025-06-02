package com.example.neuramusic.adapters.viewholders;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neuramusic.model.ProfileBlock;
import com.example.neuramusic.R;

public class ImageBlockViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ImageBlockViewHolder";
    private final ImageView imageView;
    private final ImageView playIcon;
    private final TextView textView;

    public ImageBlockViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.ivContent);
        playIcon = itemView.findViewById(R.id.ivPlayIcon);
        textView = itemView.findViewById(R.id.tvContent);
    }

    public void bind(ProfileBlock block) {
        if (block.mediaUrl != null && !block.mediaUrl.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            
            Glide.with(itemView.getContext())
                    .load(block.mediaUrl)
                    .into(imageView);
                    
            playIcon.setVisibility(block.type.equals("video") || block.type.equals("reel") ? 
                View.VISIBLE : View.GONE);
        } else {
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            playIcon.setVisibility(View.GONE);
            textView.setText("Sin imagen disponible");
        }
    }
}
