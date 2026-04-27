package com.example.planexia.ui.tasks;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        holder.tvTaskTitle.setText(task.getTitle());
        if (task.isDone()) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#1A1A2E"));
        }

        // --- Sous-titre : Module • ressource ---
        String subtitle = TextUtils.isEmpty(task.getModuleName()) ? "" : task.getModuleName();
        if (!TextUtils.isEmpty(task.getResourceText())) {
            subtitle += (subtitle.isEmpty() ? "" : " • ") + task.getResourceText();
        }
        holder.tvTaskSubtitle.setText(subtitle);

        // --- Barre colorée latérale ---
        String moduleColor = task.getModuleColor();
        try {
            holder.viewColorBar.setBackgroundColor(Color.parseColor(moduleColor));
        } catch (Exception e) {
            holder.viewColorBar.setBackgroundColor(Color.parseColor("#7B1FA2"));
        }

        // --- Icône chrono (grisée si fait, rouge si en retard) ---
        if (task.isDone()) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#BBBBBB"));
        } else if (isLate) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#F44336"));
        } else {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#CCCCCC"));
        }

        // --- Clic sur la carte = cocher/décocher ---
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !task.isDone();
            task.setDone(newState);
            notifyItemChanged(holder.getAdapterPosition());
            if (listener != null) listener.onChecked(task.getId(), newState);
        });

        // --- En-tête de date : masqué dans cette vue ---
        holder.layoutDateHeader.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View layoutDateHeader;
        View viewColorBar;
        TextView tvTaskTitle;
        TextView tvTaskSubtitle;
        ImageView ivChronoBadge;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDateHeader = itemView.findViewById(R.id.layoutDateHeader);
            viewColorBar     = itemView.findViewById(R.id.viewColorBar);
            tvTaskTitle      = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskSubtitle   = itemView.findViewById(R.id.tvTaskSubtitle);
            ivChronoBadge    = itemView.findViewById(R.id.ivChronoBadge);
        }
    }
}
