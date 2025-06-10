package com.example.neuramusic.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.activities.PlaylistDetailActivity;
import com.example.neuramusic.adapters.LibraryAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.Playlist;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment {

    private RecyclerView rvPlaylists;
    private LibraryAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton btnAdd;

    private final List<Playlist> allPlaylists = new ArrayList<>();
    private final List<Playlist> visiblePlaylists = new ArrayList<>();
    private final Map<String, List<Playlist>> childrenMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_library, container, false);

        rvPlaylists = view.findViewById(R.id.rv_library);
        progressBar = view.findViewById(R.id.progress_bar);
        btnAdd = view.findViewById(R.id.fab_add_playlist);

        adapter = new LibraryAdapter(
                requireContext(),
                visiblePlaylists,
                this::toggleFolderExpansion,
                this::openEditDialog,
                playlist -> {
                    // ✅ Lanza actividad de detalle al hacer click en una playlist
                    Intent intent = new Intent(requireContext(), PlaylistDetailActivity.class);
                    intent.putExtra("playlist_id", playlist.getId());
                    intent.putExtra("playlist_title", playlist.getTitle());
                    startActivity(intent);
                }
        );


        rvPlaylists.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvPlaylists.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            AddFolderDialogFragment dialog = new AddFolderDialogFragment(
                    RetrofitClient.getCurrentUid(),
                    this::loadAndBuildHierarchy
            );
            dialog.show(getParentFragmentManager(), "addFolder");
        });

        loadAndBuildHierarchy();
        return view;
    }

    private void loadAndBuildHierarchy() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);

        Map<String, String> filters = new HashMap<>();
        filters.put("uid", "eq." + RetrofitClient.getCurrentUid());

        api.getPlaylists(filters, RetrofitClient.API_KEY, "Bearer " + RetrofitClient.getAccessToken())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Playlist>> call, @NonNull Response<List<Playlist>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            allPlaylists.clear();
                            allPlaylists.addAll(response.body());
                            buildHierarchy();
                        } else {
                            Toast.makeText(getContext(), "Error al cargar biblioteca", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Playlist>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Fallo de red", Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void buildHierarchy() {
        childrenMap.clear();
        List<Playlist> roots = new ArrayList<>();

        for (Playlist p : allPlaylists) {
            p.setExpanded(false); // reset
            if (p.getParentId() == null) {
                roots.add(p);
            } else {
                childrenMap.computeIfAbsent(p.getParentId(), k -> new ArrayList<>()).add(p);
            }
        }

        visiblePlaylists.clear();
        for (Playlist root : roots) {
            addVisibleRecursive(root, 0);
        }

        adapter.notifyDataSetChanged();
    }

    private void addVisibleRecursive(Playlist playlist, int depth) {
        playlist.setDepth(depth);
        visiblePlaylists.add(playlist);
        if (playlist.isExpanded()) {
            List<Playlist> children = childrenMap.get(playlist.getId());
            if (children != null) {
                for (Playlist child : children) {
                    addVisibleRecursive(child, depth + 1);
                }
            }
        }
    }

    private void toggleFolderExpansion(int position) {
        Playlist clicked = visiblePlaylists.get(position);
        if (!"folder".equals(clicked.getType())) return;

        if (clicked.isExpanded()) {
            collapseFolder(position);
            clicked.setExpanded(false);
        } else {
            clicked.setExpanded(true);
            expandFolder(position);
        }
    }

    private void expandFolder(int position) {
        Playlist folder = visiblePlaylists.get(position);
        List<Playlist> children = childrenMap.get(folder.getId());
        if (children == null) return;

        int insertPosition = position + 1;
        for (Playlist child : children) {
            child.setDepth(folder.getDepth() + 1);
            visiblePlaylists.add(insertPosition, child);
            insertPosition++;
        }

        adapter.notifyItemRangeInserted(position + 1, children.size());
    }

    private void collapseFolder(int position) {
        Playlist folder = visiblePlaylists.get(position);
        int depth = folder.getDepth();

        int removeCount = 0;
        for (int i = position + 1; i < visiblePlaylists.size(); i++) {
            if (visiblePlaylists.get(i).getDepth() <= depth) break;
            removeCount++;
        }

        for (int i = 0; i < removeCount; i++) {
            visiblePlaylists.remove(position + 1);
        }

        adapter.notifyItemRangeRemoved(position + 1, removeCount);
    }

    private void openEditDialog(Playlist folder) {
        EditFolderDialogFragment dialog = new EditFolderDialogFragment(
                RetrofitClient.getCurrentUid(),
                this::loadAndBuildHierarchy,
                folder // ← Esta es la Playlist seleccionada para edición
        );

        dialog.show(getParentFragmentManager(), "editFolder");
    }
}
