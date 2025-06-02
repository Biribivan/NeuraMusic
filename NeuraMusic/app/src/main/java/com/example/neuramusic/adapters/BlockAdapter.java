package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.neuramusic.R;
import com.example.neuramusic.model.ProfileBlock;

import java.util.List;

public class BlockAdapter extends RecyclerView.Adapter<BlockAdapter.BlockViewHolder> {

    private List<ProfileBlock> blocks;

    public BlockAdapter(List<ProfileBlock> blocks) {
        this.blocks = blocks;
    }

    @NonNull
    @Override
    public BlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_block, parent, false);
        return new BlockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockViewHolder holder, int position) {
        ProfileBlock block = blocks.get(position);
        
        // Configurar el tamaño del item según el tipo de contenido
        StaggeredGridLayoutManager.LayoutParams layoutParams =
                (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
        
        switch (block.type) {
            case "reel":
            case "video":
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.setFullSpan(true); // Ocupará todo el ancho
                break;
            case "image":
                layoutParams.height = block.height > 0 ? block.height : ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.setFullSpan(false);
                break;
            case "text":
            case "tweet":
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layoutParams.setFullSpan(block.content.length() > 100); // Si es texto largo, ocupa todo el ancho
                break;
        }
        
        holder.itemView.setLayoutParams(layoutParams);

        // Cargar contenido según el tipo
        if (block.type.equals("image") || block.type.equals("video") || block.type.equals("reel")) {
            holder.imageView.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.GONE);
            
            Glide.with(holder.itemView.getContext())
                    .load(block.mediaUrl)
                    .into(holder.imageView);
                    
            if (block.type.equals("video") || block.type.equals("reel")) {
                holder.playIcon.setVisibility(View.VISIBLE);
            } else {
                holder.playIcon.setVisibility(View.GONE);
            }
        } else {
            holder.imageView.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.playIcon.setVisibility(View.GONE);
            holder.textView.setText(block.content);
        }
    }

    @Override
    public int getItemCount() {
        return blocks.size();
    }

    static class BlockViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView playIcon;
        TextView textView;

        BlockViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivContent);
            playIcon = itemView.findViewById(R.id.ivPlayIcon);
            textView = itemView.findViewById(R.id.tvContent);
        }
    }
}
