package com.example.neuramusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.neuramusic.R;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.FeedPost;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.PostViewHolder> {

    private Context context;
    private List<FeedPost> postList;
    private String token;
    private SupabaseService supabaseService;




    public UserPostAdapter(Context context, List<FeedPost> postList, String token) {
        this.context = context;
        this.postList = postList;
        this.token = token;
        this.supabaseService = RetrofitClient.getClient().create(SupabaseService.class);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_post, parent, false);

        return new PostViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        FeedPost post = postList.get(position);

        // Nombre de usuario
        holder.tvUsername.setText("@" + post.user.username);

        // Contenido del post (texto o caption)
        holder.tvCaption.setText(post.caption != null ? post.caption : "");
        holder.tvContent.setText(post.content != null ? post.content : "");


        // Imagen multimedia si es post con media
        if (post.isMedia && post.mediaUrls != null && !post.mediaUrls.isEmpty()) {
            Glide.with(context)
                    .load(post.mediaUrls.get(0))
                    .into(holder.ivMedia);
            holder.ivMedia.setVisibility(View.VISIBLE);
        } else {
            holder.ivMedia.setVisibility(View.GONE);
        }

        // Imagen de perfil del usuario
        if (post.user.profileImageUrl != null && !post.user.profileImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(post.user.profileImageUrl)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_user).circleCrop())
                    .into(holder.ivProfileImage);
        } else {
            holder.ivProfileImage.setImageResource(R.drawable.ic_user);
        }

        // Menú contextual de opciones (3 puntos)
       // holder.btnOptions.setOnClickListener(v -> showPopupMenu(v, post, position));

        // Botón de eliminar
        holder.btnDeletePost.setOnClickListener(v -> deletePost(post, position));
    }


    /*private void showPopupMenu(View view, FeedPost post, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.post_menu);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_post) {
                deletePost(post, position);
                return true;
            }
            return false;
        });

        popup.show();
    }*/

    private void deletePost(FeedPost post, int position) {
        Map<String, String> query = new HashMap<>();
        query.put("id", "eq." + post.id);

        Call<ResponseBody> call;

        if (post.isMedia) {
            call = supabaseService.deleteMediaPost(query, RetrofitClient.API_KEY, "Bearer " + token);
        } else {
            call = supabaseService.deleteTextPost(query, RetrofitClient.API_KEY, "Bearer " + token);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    postList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Post eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvCaption;
        TextView tvContent;
        ImageView ivMedia, ivProfileImage;
        ImageButton
                btnDeletePost;
        //btnOptions,
        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage); // <- CORREGIDO
           //btnOptions = itemView.findViewById(R.id.btnOptions);
            btnDeletePost = itemView.findViewById(R.id.btnDeletePost);
        }
    }

}
