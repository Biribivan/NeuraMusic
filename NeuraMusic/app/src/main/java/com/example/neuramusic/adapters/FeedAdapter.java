package com.example.neuramusic.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.neuramusic.R;
import com.example.neuramusic.model.FeedPost;

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

        holder.tvUsername.setText(post.user != null ? post.user.username : "");
        if (post.user != null && post.user.profileImageUrl != null && !post.user.profileImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.user.profileImageUrl)
                    .placeholder(R.drawable.ic_user)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_user);
        }

        holder.tvContent.setText(post.content != null ? post.content : "");

        if (post.isMedia && post.mediaUrls != null && !post.mediaUrls.isEmpty()) {
            holder.ivMedia.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.mediaUrls.get(0))
                    .into(holder.ivMedia);
        } else {
            holder.ivMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class FeedViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvContent;
        ImageView ivMedia;

        FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvContent);
            ivMedia = itemView.findViewById(R.id.ivMedia);
        }
    }
}
