package com.example.planexia;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter pour afficher les tâches du planning, groupées par date.
 * Chaque item peut afficher ou non un en-tête de date.
 */
public class PlanningTaskAdapter extends RecyclerView.Adapter<PlanningTaskAdapter.TaskViewHolder> {

    // Couleurs des barres latérales (cycle selon les modules)
    private static final String[] BAR_COLORS = {
            "#5B2EE8", // violet
            "#4CAF7D", // vert
            "#F5A623", // orange
            "#E84B8A", // rose
            "#2196F3"  // bleu
    };

    // Modèle interne d'une tâche à afficher dans le planning
    public static class PlanningTask {
        public String title;       // Titre de la tâche
        public String moduleName;  // Nom du module associé
        public int durationMin;    // Durée estimée en minutes (0 si inconnue)
        public String dateLabel;   // Ex: "Mercredi 03/12/2025" — null si même groupe que précédent
        public boolean isToday;    // True si la date correspond à aujourd'hui
        public boolean isDone;     // True si la tâche est déjà cochée

        public PlanningTask(String title, String moduleName, int durationMin,
                            String dateLabel, boolean isToday, boolean isDone) {
            this.title = title;
            this.moduleName = moduleName;
            this.durationMin = durationMin;
            this.dateLabel = dateLabel;
            this.isToday = isToday;
            this.isDone = isDone;
        }
    }

    // -------------------------------------------------------------------------

    private List<PlanningTask> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(PlanningTask task, int position);
    }

    public PlanningTaskAdapter(List<PlanningTask> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    // -------------------------------------------------------------------------

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        PlanningTask task = tasks.get(position);

        // --- En-tête de date ---
        if (task.dateLabel != null) {
            holder.layoutDateHeader.setVisibility(View.VISIBLE);
            holder.tvDateHeader.setText(task.dateLabel);

            // Badge "Aujourd'hui" uniquement si c'est le jour courant
            if (task.isToday) {
                holder.tvBadgeAujourdhui.setVisibility(View.VISIBLE);
            } else {
                holder.tvBadgeAujourdhui.setVisibility(View.GONE);
            }
        } else {
            holder.layoutDateHeader.setVisibility(View.GONE);
        }

        // --- Titre de la tâche ---
        holder.tvTaskTitle.setText(task.title);

        // --- Sous-titre : Module • durée ---
        String subtitle = task.moduleName;
        if (task.durationMin > 0) {
            subtitle += " • " + task.durationMin + " min";
        }
        holder.tvTaskSubtitle.setText(subtitle);

        // --- Couleur de la barre latérale (cycle selon position) ---
        String color = BAR_COLORS[position % BAR_COLORS.length];
        holder.viewColorBar.setBackgroundColor(Color.parseColor(color));

        // --- Icône chrono (grisée si tâche faite, normale sinon) ---
        int tint = task.isDone ? Color.parseColor("#BBBBBB") : Color.parseColor("#CCCCCC");
        holder.ivChronoBadge.setColorFilter(tint);

        // --- Click sur la carte ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    // --- Mise à jour de la liste ---
    public void updateTasks(List<PlanningTask> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    // -------------------------------------------------------------------------

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutDateHeader;
        TextView tvBadgeAujourdhui;
        TextView tvDateHeader;
        View viewColorBar;
        TextView tvTaskTitle;
        TextView tvTaskSubtitle;
        ImageView ivChronoBadge;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDateHeader  = itemView.findViewById(R.id.layoutDateHeader);
            tvBadgeAujourdhui = itemView.findViewById(R.id.tvBadgeAujourdhui);
            tvDateHeader      = itemView.findViewById(R.id.tvDateHeader);
            viewColorBar      = itemView.findViewById(R.id.viewColorBar);
            tvTaskTitle       = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskSubtitle    = itemView.findViewById(R.id.tvTaskSubtitle);
            ivChronoBadge     = itemView.findViewById(R.id.ivChronoBadge);
        }
    }
}