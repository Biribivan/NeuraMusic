// CalendarFragment.java

package com.example.neuramusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.neuramusic.R;
import com.example.neuramusic.adapters.CalendarItemAdapter;
import com.example.neuramusic.api.RetrofitClient;
import com.example.neuramusic.api.SupabaseService;
import com.example.neuramusic.model.CalendarItem;

import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private TextView tvSelectedDate;
    private RecyclerView recyclerView;
    private ImageButton btnAdd;

    private CalendarItemAdapter adapter;
    private List<CalendarItem> itemList = new ArrayList<>();

    private String selectedDate = "";
    private final String apiKey = RetrofitClient.API_KEY;
    private String authToken;
    private String userId;

    public CalendarFragment(String authToken, String userId) {
        this.authToken = authToken;
        this.userId = userId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                "calendar_item_result", this,
                (requestKey, bundle) -> {
                    if (bundle.getBoolean("calendar_item_added", false)) {
                        cargarItemsDelDia();
                    }
                }
        );


        calendarView = view.findViewById(R.id.calendar_view);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        recyclerView = view.findViewById(R.id.recycler_day_items);
        btnAdd = view.findViewById(R.id.btn_add_calendar_item);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalendarItemAdapter(getContext(), itemList, this::actualizarEstadoTarea);
        recyclerView.setAdapter(adapter);

        long dateMillis = calendarView.getDate();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(dateMillis));
        tvSelectedDate.setText("Eventos de " + selectedDate);
        cargarItemsDelDia();

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            tvSelectedDate.setText("Eventos de " + selectedDate);
            cargarItemsDelDia();
        });

        // Escuchar si el diálogo agregó un ítem
        getParentFragmentManager().setFragmentResultListener("calendar_item_result", this, (key, bundle) -> {
            boolean added = bundle.getBoolean("calendar_item_added", false);
            if (added) {
                cargarItemsDelDia();
            }
        });

        btnAdd.setOnClickListener(v -> {
            AddCalendarItemDialogFragment dialog = new AddCalendarItemDialogFragment(
                    apiKey, authToken, userId, this::cargarItemsDelDia
            );
            dialog.show(getParentFragmentManager(), "AddCalendarItemDialog");
        });

    }

    private void cargarItemsDelDia() {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Map<String, String> query = new HashMap<>();
        query.put("user_id", "eq." + userId);
        query.put("event_date", "eq." + selectedDate);

        Call<List<CalendarItem>> call = api.getCalendarItems(query, apiKey, "Bearer " + authToken);
        call.enqueue(new Callback<List<CalendarItem>>() {
            @Override
            public void onResponse(Call<List<CalendarItem>> call, Response<List<CalendarItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList.clear();
                    itemList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CalendarItem>> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarEstadoTarea(CalendarItem item, boolean isChecked) {
        SupabaseService api = RetrofitClient.getClient().create(SupabaseService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("is_completed", isChecked);

        Call<ResponseBody> call = api.updateCalendarItem(item.getId(), apiKey, "Bearer " + authToken, updates);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getContext(), "Error al actualizar tarea", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
