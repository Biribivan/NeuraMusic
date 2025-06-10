package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.neuramusic.R;
import com.example.neuramusic.model.FeedPost;
import com.example.neuramusic.model.UserResponse;

import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private final List<FeedPost> posts;

    public FeedAdapter(List<FeedPost> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed_post, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        FeedPost post = posts.get(position);
        UserResponse user = post.user;

        // Username
        String username = (user != null && user.username != null) ? user.username : "Usuario desconocido";
        holder.tvUsername.setText(username);

        // Avatar
        if (user != null && user.profileImageUrl != null && !user.profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.profileImageUrl)
                    .placeholder(R.drawable.ic_user)
                    .error(R.drawable.ic_user)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        // Caption (pie de foto)
        if (post.caption != null && !post.caption.trim().isEmpty()) {
            holder.tvCaption.setText(post.caption.trim());
            holder.tvCaption.setVisibility(View.VISIBLE);
        } else {
            holder.tvCaption.setVisibility(View.GONE);
        }

        // Content
        holder.tvContent.setText(post.content != null ? post.content : "");

        // Media (solo si es post multimedia y hay al menos 1 media URL)
        if (post.isMedia && post.mediaUrls != null && !post.mediaUrls.isEmpty()) {
            holder.ivMedia.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.mediaUrls.get(0))
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_media)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.ivMedia);
        } else {
            holder.ivMedia.setImageResource(0);
            holder.ivMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvCaption;
        TextView tvContent;
        ImageView ivMedia;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivMedia = itemView.findViewById(R.id.ivMedia);
        }
    }
}
