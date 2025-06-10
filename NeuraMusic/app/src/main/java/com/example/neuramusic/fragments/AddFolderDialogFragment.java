package com.example.neuramusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.PlaylistCreationAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.Playlist;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddFolderDialogFragment extends DialogFragment {

    private EditText etFolderName;
    private String selectedColor = "#1E88E5";
    private RecyclerView rvPlaylists;
    private PlaylistCreationAdapter creationAdapter;
    private List<Playlist> newPlaylists = new ArrayList<>();
    private ImageButton btnAddPlaylist;
    private Button btnSave, btnCancel;

    private final String userId;
    private final Runnable onCompleteCallback; // ✅ NUEVO CALLBACK

    private SupabaseService api;

    // ✅ Constructor actualizado
    public AddFolderDialogFragment(String uid, Runnable onCompleteCallback) {
        this.userId = uid;
        this.onCompleteCallback = onCompleteCallback;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_folder, container, false);

        etFolderName = view.findViewById(R.id.et_folder_name);
        rvPlaylists = view.findViewById(R.id.rv_new_playlists);
        btnAddPlaylist = view.findViewById(R.id.btn_add_playlist_inside);
        btnSave = view.findViewById(R.id.btn_save_folder);
        btnCancel = view.findViewById(R.id.btn_cancel);

        api = RetrofitClient.getClient().create(SupabaseService.class);

        creationAdapter = new PlaylistCreationAdapter(newPlaylists, selectedColor, position -> {
            newPlaylists.remove(position);
            creationAdapter.notifyItemRemoved(position);
        });

        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaylists.setAdapter(creationAdapter);

        btnAddPlaylist.setOnClickListener(v -> {
            Playlist p = new Playlist();
            p.setUid(userId);
            p.setTitle("Nueva Playlist");
            p.setTags(Collections.singletonList(selectedColor));
            newPlaylists.add(p);
            creationAdapter.notifyItemInserted(newPlaylists.size() - 1);
        });

        btnSave.setOnClickListener(v -> saveFolderWithPlaylists());
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveFolderWithPlaylists() {
        String folderName = etFolderName.getText().toString().trim();
        if (folderName.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un nombre para la carpeta", Toast.LENGTH_SHORT).show();
            return;
        }

        Playlist folder = new Playlist();
        folder.setUid(userId);
        folder.setTitle(folderName);
        folder.setType("folder");
        folder.setTags(Collections.singletonList(selectedColor));

        api.createPlaylist(Collections.singletonList(folder), RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<List<Playlist>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Playlist>> call, @NonNull Response<List<Playlist>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String folderId = response.body().get(0).getId();
                            createNestedPlaylists(folderId);
                        } else {
                            Toast.makeText(getContext(), "Error al guardar la carpeta", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Playlist>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "Fallo al crear carpeta", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createNestedPlaylists(String folderId) {
        for (Playlist p : newPlaylists) {
            p.setParentId(folderId);
        }

        api.createPlaylist(newPlaylists, RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<List<Playlist>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Playlist>> call, @NonNull Response<List<Playlist>> response) {
                        dismiss();
                        if (onCompleteCallback != null) onCompleteCallback.run(); // ✅ Ejecutar callback al cerrar
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Playlist>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                    }
                });
    }
}
