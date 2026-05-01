package com.example.planexia.ui.tasks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.ChronoActivity;
import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Task;
import com.example.planexia.ui.PremiumDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskCheckedListener {
        void onChecked(long taskId, boolean isDone);
    }

    public interface OnTaskEditListener {
        void onEdit(Task task);
    }

    public interface OnTaskDeleteListener {
        void onDelete(long taskId);
    }

    private final List<Task> taskList;
    private final OnTaskCheckedListener checkedListener;
    private final OnTaskEditListener editListener;
    private final OnTaskDeleteListener deleteListener;
    private final String today;

    public TaskAdapter(List<Task> taskList,
                       OnTaskCheckedListener checkedListener,
                       OnTaskEditListener editListener,
                       OnTaskDeleteListener deleteListener) {
        this.taskList        = taskList;
        this.checkedListener = checkedListener;
        this.editListener    = editListener;
        this.deleteListener  = deleteListener;
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

        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        // Titre
        holder.tvTaskTitle.setText(task.getTitle());
        if (task.isDone()) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#1A1A2E"));
        }

        // Bordure card
        if (holder.cardTask != null) {
            if (isLate && !task.isDone()) {
                holder.cardTask.setCardBackgroundColor(Color.parseColor("#FFF5F5"));
                holder.cardTask.setStrokeColor(Color.parseColor("#F44336"));
                holder.cardTask.setStrokeWidth(4);
            } else {
                holder.cardTask.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                holder.cardTask.setStrokeWidth(0);
            }
        }

        // Sous-titre
        // Sous-titre : Module • Date (ex: "Mathématiques • 3 déc.")
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(task.getModuleName())) {
            sb.append(task.getModuleName());
        }
        if (!TextUtils.isEmpty(dueDate)) {
            String dateFormatted = formatDueDate(dueDate);
            if (!TextUtils.isEmpty(dateFormatted)) {
                if (sb.length() > 0) sb.append(" • ");
                sb.append(dateFormatted);
            }
        }
        holder.tvTaskSubtitle.setText(sb.toString());

        // Barre colorée
        String moduleColor = task.getModuleColor();
        try {
            holder.viewColorBar.setBackgroundColor(Color.parseColor(moduleColor));
        } catch (Exception e) {
            holder.viewColorBar.setBackgroundColor(Color.parseColor("#7B1FA2"));
        }

        // Icône chrono
        if (task.isDone()) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#BBBBBB"));
        } else if (isLate) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#F44336"));
        } else {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#CCCCCC"));
        }

        // Clic = cocher/décocher
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !task.isDone();
            task.setDone(newState);
            notifyItemChanged(holder.getAdapterPosition());
            if (checkedListener != null) checkedListener.onChecked(task.getId(), newState);
        });

        // Appui long = BottomSheet
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsBottomSheet(v, task);
            return true;
        });

        holder.layoutDateHeader.setVisibility(View.GONE);
    }

    private void showOptionsBottomSheet(View anchor, Task task) {
        Context ctx = anchor.getContext();
        BottomSheetDialog sheet = new BottomSheetDialog(ctx,
                com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

        View sheetView = LayoutInflater.from(ctx)
                .inflate(R.layout.bottom_sheet_task_options, null);
        sheet.setContentView(sheetView);

        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = sheetView.findViewById(R.id.tvBottomSheetTaskTitle);
        if (tvTitle != null) tvTitle.setText(task.getTitle());

        // Bouton Modifier
        LinearLayout btnEdit = sheetView.findViewById(R.id.btnBottomSheetEdit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                sheet.dismiss();
                if (editListener != null) editListener.onEdit(task);
            });
        }

        // Bouton Mode Chrono
        LinearLayout btnChrono = sheetView.findViewById(R.id.btnBottomSheetChrono);
        if (btnChrono != null) {
            btnChrono.setOnClickListener(v -> {
                sheet.dismiss();
                // Vérifier premium
                SharedPreferences prefs = ctx.getSharedPreferences("planexia_session", Context.MODE_PRIVATE);
                boolean isPremium = prefs.getBoolean("is_premium", false);
                if (!isPremium) {
                    long userId = new SessionManager(ctx).getUserId();
                    isPremium = new PlanexiaRepository(ctx).isPremium(userId);
                }

                if (isPremium) {
                    // Lancer le chrono avec la tâche
                    Intent intent = new Intent(ctx, ChronoActivity.class);
                    intent.putExtra("task_id", task.getId());
                    intent.putExtra("task_title", task.getTitle());
                    intent.putExtra("task_module", task.getModuleName());
                    ctx.startActivity(intent);
                } else {
                    // Proposer le Premium
                    if (ctx instanceof android.app.Activity) {
                        PremiumDialog.show((android.app.Activity) ctx, null);
                    } else {
                        Toast.makeText(ctx, "Cette fonctionnalité est réservée aux membres Premium ✦", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // Bouton Supprimer
        LinearLayout btnDelete = sheetView.findViewById(R.id.btnBottomSheetDelete);
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                sheet.dismiss();
                if (deleteListener != null) deleteListener.onDelete(task.getId());
            });
        }

        sheet.show();
    }

    @Override
    public int getItemCount() { return taskList.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        View layoutDateHeader;
        View viewColorBar;
        TextView tvTaskTitle;
        TextView tvTaskSubtitle;
        ImageView ivChronoBadge;
        MaterialCardView cardTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDateHeader = itemView.findViewById(R.id.layoutDateHeader);
            viewColorBar     = itemView.findViewById(R.id.viewColorBar);
            tvTaskTitle      = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskSubtitle   = itemView.findViewById(R.id.tvTaskSubtitle);
            ivChronoBadge    = itemView.findViewById(R.id.ivChronoBadge);
            cardTask         = itemView.findViewById(R.id.cardTask);
        }
    }

    /** Convertit "2025-12-03" en "3 déc." */
    private static String formatDueDate(String rawDate) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.LocalDate date = java.time.LocalDate.parse(rawDate);
                String[] months = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                return date.getDayOfMonth() + " " + months[date.getMonthValue() - 1];
            } else {
                java.text.SimpleDateFormat sdfIn  = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("d MMM", java.util.Locale.FRENCH);
                java.util.Date d = sdfIn.parse(rawDate);
                if (d != null) return sdfOut.format(d);
            }
        } catch (Exception ignored) {}
        return "";
    }

}