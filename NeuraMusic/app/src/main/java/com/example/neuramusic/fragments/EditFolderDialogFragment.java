package com.example.neuramusic.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditFolderDialogFragment extends DialogFragment {

    private EditText etFolderName;
    private RecyclerView rvPlaylists;
    private PlaylistCreationAdapter creationAdapter;
    private List<Playlist> editablePlaylists = new ArrayList<>();
    private ImageButton btnAddPlaylist;
    private Button btnSave, btnCancel;

    private final String userId;
    private final Runnable onCompleteCallback;
    private final Playlist folder;

    private SupabaseService api;

    public EditFolderDialogFragment(String uid, Runnable onCompleteCallback, Playlist folder) {
        this.userId = uid;
        this.onCompleteCallback = onCompleteCallback;
        this.folder = folder;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.92);
            window.setAttributes(params);
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

        creationAdapter = new PlaylistCreationAdapter(editablePlaylists, "#1E88E5", this::onRequestDeletePlaylist);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaylists.setAdapter(creationAdapter);

        etFolderName.setText(folder.getTitle());

        btnAddPlaylist.setOnClickListener(v -> {
            Playlist p = new Playlist();
            p.setUid(userId);
            p.setParentId(folder.getId());
            p.setTitle("Nueva Playlist");
            p.setTags(Collections.singletonList("#1E88E5"));
            editablePlaylists.add(p);
            creationAdapter.notifyItemInserted(editablePlaylists.size() - 1);
        });

        btnSave.setOnClickListener(v -> updateFolderAndSavePlaylists());
        btnCancel.setOnClickListener(v -> dismiss());

        loadExistingChildren();

        return view;
    }

    private void loadExistingChildren() {
        Map<String, String> filters = new HashMap<>();
        filters.put("uid", "eq." + userId);
        filters.put("parent_id", "eq." + folder.getId());

        api.getPlaylists(filters, RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<List<Playlist>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Playlist>> call, @NonNull Response<List<Playlist>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            editablePlaylists.clear();
                            editablePlaylists.addAll(response.body());
                            creationAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Playlist>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error al cargar playlists", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void updateFolderAndSavePlaylists() {
        String newTitle = etFolderName.getText().toString().trim();
        if (newTitle.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un nombre para la carpeta", Toast.LENGTH_SHORT).show();
            return;
        }

        folder.setTitle(newTitle);

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", folder.getTitle());
        updates.put("parent_id", folder.getParentId());

        api.updatePlaylist(
                "eq." + folder.getId(),
                RetrofitClient.API_KEY,
                "Bearer " + RetrofitClient.getAccessToken(),
                updates
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    dismiss();
                    if (onCompleteCallback != null) onCompleteCallback.run();
                } else {
                    Toast.makeText(getContext(), "Error al actualizar carpeta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Fallo de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChildPlaylists() {
        for (Playlist p : editablePlaylists) {
            p.setParentId(folder.getId());
        }

        api.createPlaylist(editablePlaylists, RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<List<Playlist>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Playlist>> call, @NonNull Response<List<Playlist>> response) {
                        if (onCompleteCallback != null) onCompleteCallback.run();
                        dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Playlist>> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(getContext(), "Fallo al guardar playlists", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onRequestDeletePlaylist(int position) {
        Playlist p = editablePlaylists.get(position);
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("¿Eliminar playlist?")
                .setMessage("¿Seguro que quieres eliminar \"" + p.getTitle() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    editablePlaylists.remove(position);
                    creationAdapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
