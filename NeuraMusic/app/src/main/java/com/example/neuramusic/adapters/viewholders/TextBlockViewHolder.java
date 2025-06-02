package com.example.neuramusic.adapters.viewholders;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.model.ProfileBlock;

public class TextBlockViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "TextBlockViewHolder";
    private final TextView textView;

    public TextBlockViewHolder(View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.tvContent);
    }

    public void bind(ProfileBlock block) {
        if (block.content != null && !block.content.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(block.content);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
