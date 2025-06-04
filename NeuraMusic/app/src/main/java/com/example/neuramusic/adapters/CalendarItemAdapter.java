package com.example.neuramusic.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.neuramusic.R;
import com.example.neuramusic.model.CalendarItem; // ← cambiado a 'model'

import java.util.List;

public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemAdapter.ViewHolder> {

    private final List<CalendarItem> itemList;
    private final Context context;

    public interface OnItemCheckedChangeListener {
        void onChecked(CalendarItem item, boolean isChecked);
    }

    private final OnItemCheckedChangeListener checkedListener;

    public CalendarItemAdapter(Context context, List<CalendarItem> itemList, OnItemCheckedChangeListener checkedListener) {
        this.context = context;
        this.itemList = itemList;
        this.checkedListener = checkedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CalendarItem item = itemList.get(position);

        holder.tvTitle.setText(item.getTitle());

        String timePart = item.getEventTime() != null ? " • " + item.getEventTime().toString() : "";
        holder.tvTypeTime.setText(item.getType() + timePart);

        holder.tvIcon.setText(item.getIcon() != null ? item.getIcon() : "•");

        int colorRes = context.getResources().getIdentifier(
                item.getLabelColor(), "color", context.getPackageName());
        holder.viewLabelColor.setBackgroundColor(
                colorRes != 0 ? context.getColor(colorRes) : context.getColor(R.color.accent_blue)
        );

        if ("tarea".equalsIgnoreCase(item.getType())) {
            holder.checkboxCompleted.setVisibility(View.VISIBLE);
            holder.checkboxCompleted.setChecked(item.getIsCompleted());
            holder.checkboxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                checkedListener.onChecked(item, isChecked);
            });
        } else {
            holder.checkboxCompleted.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewLabelColor;
        TextView tvIcon, tvTitle, tvTypeTime;
        CheckBox checkboxCompleted;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewLabelColor = itemView.findViewById(R.id.view_label_color);
            tvIcon = itemView.findViewById(R.id.tv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTypeTime = itemView.findViewById(R.id.tv_type_time);
            checkboxCompleted = itemView.findViewById(R.id.checkbox_completed);
        }
    }
}
