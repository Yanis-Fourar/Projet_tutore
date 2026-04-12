package com.example.planexia.ui.tasks;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Task;

import java.time.LocalDate;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskCheckedListener {
        void onChecked(long taskId, boolean isDone);
    }

    private final List<Task> taskList;
    private final OnTaskCheckedListener listener;
    private final String today;

    public TaskAdapter(List<Task> taskList, OnTaskCheckedListener listener) {
        this.taskList = taskList;
        this.listener = listener;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.today = LocalDate.now().toString();
        } else {
            this.today = "";
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        String dueDate = task.getDueDate();

        boolean isLate = !task.isDone()
                && !TextUtils.isEmpty(dueDate)
                && !today.isEmpty()
                && dueDate.compareTo(today) < 0;

        // --- Titre ---
        holder.tvTitle.setText(task.getTitle());
        if (task.isDone()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#1A1A1A"));
        }

        // --- Cercle checkbox ---
        if (task.isDone()) {
            holder.viewCircle.setBackgroundResource(R.drawable.circle_check_green);
        } else if (isLate) {
            holder.viewCircle.setBackgroundResource(R.drawable.circle_outline_red);
        } else {
            holder.viewCircle.setBackgroundResource(R.drawable.circle_outline_purple);
        }

        // Clic sur le cercle = cocher/décocher
        holder.viewCircle.setOnClickListener(v -> {
            boolean newState = !task.isDone();
            task.setDone(newState);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onChecked(task.getId(), newState);
        });

        // --- Pastille couleur du module ---
        String moduleColor = task.getModuleColor();
        try {
            holder.viewModuleColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor(moduleColor))
            );
        } catch (Exception e) {
            holder.viewModuleColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor("#7B1FA2"))
            );
        }

        // --- Nom du module ---
        String moduleName = task.getModuleName();
        if (!TextUtils.isEmpty(moduleName)) {
            holder.tvModule.setVisibility(View.VISIBLE);
            holder.viewModuleColor.setVisibility(View.VISIBLE);
            holder.tvModule.setText(moduleName);
            holder.tvModule.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#888888"));
        } else {
            holder.tvModule.setVisibility(View.GONE);
            holder.viewModuleColor.setVisibility(View.GONE);
        }

        // --- Date ---
        if (!TextUtils.isEmpty(dueDate)) {
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvDateSep.setVisibility(View.VISIBLE);
            holder.tvDate.setText(formatDate(dueDate));
            holder.tvDate.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#888888"));
        } else {
            holder.tvDate.setVisibility(View.GONE);
            holder.tvDateSep.setVisibility(View.GONE);
        }

        // --- Ressource ---
        if (!TextUtils.isEmpty(task.getResourceText())) {
            holder.tvResource.setVisibility(View.VISIBLE);
            holder.tvResourceSep.setVisibility(View.VISIBLE);
            holder.tvResource.setText(task.getResourceText());
        } else {
            holder.tvResource.setVisibility(View.GONE);
            holder.tvResourceSep.setVisibility(View.GONE);
        }

        // --- Bordure rouge si en retard ---
        if (isLate) {
            holder.itemView.setBackgroundResource(R.drawable.card_border_red);
        } else {
            holder.itemView.setBackgroundResource(0);
        }
    }

    private String formatDate(String date) {
        try {
            String[] parts = date.split("-");
            int month = Integer.parseInt(parts[1]);
            int day   = Integer.parseInt(parts[2]);
            String[] months = {"jan.", "fév.", "mar.", "avr.", "mai", "juin",
                    "juil.", "août", "sep.", "oct.", "nov.", "déc."};
            return day + " " + months[month - 1];
        } catch (Exception e) {
            return date;
        }
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView viewCircle, tvTitle, tvModule, tvDate, tvDateSep, tvResource, tvResourceSep;
        View viewModuleColor;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCircle    = itemView.findViewById(R.id.viewTaskCircle);
            tvTitle       = itemView.findViewById(R.id.tvTaskTitle);
            tvModule      = itemView.findViewById(R.id.tvTaskModule);
            tvDate        = itemView.findViewById(R.id.tvTaskDate);
            tvDateSep     = itemView.findViewById(R.id.tvTaskDateSep);
            tvResource    = itemView.findViewById(R.id.tvTaskResource);
            tvResourceSep = itemView.findViewById(R.id.tvResourceSep);
            viewModuleColor = itemView.findViewById(R.id.viewModuleColor);
        }
    }
}