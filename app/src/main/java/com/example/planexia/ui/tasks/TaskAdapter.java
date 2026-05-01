package com.example.planexia.ui.tasks;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.google.android.material.card.MaterialCardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Task;
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

        // --- Fond transparent sur itemView pour éviter la double bordure ---
        holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        // --- Titre ---
        holder.tvTaskTitle.setText(task.getTitle());
        if (task.isDone()) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(Color.parseColor("#AAAAAA"));
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTaskTitle.setTextColor(isLate ? Color.parseColor("#F44336") : Color.parseColor("#1A1A2E"));
        }

        // --- Bordure uniquement sur la CardView ---
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

        // --- Sous-titre : Module • Objectif • ressource ---
        String subtitle = TextUtils.isEmpty(task.getModuleName()) ? "" : task.getModuleName();
        if (!TextUtils.isEmpty(task.getObjectiveName())) {
            subtitle += (subtitle.isEmpty() ? "" : " • ") + task.getObjectiveName();
        }
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

        // --- Icône chrono ---
        if (task.isDone()) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#BBBBBB"));
        } else if (isLate) {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#F44336"));
        } else {
            holder.ivChronoBadge.setColorFilter(Color.parseColor("#CCCCCC"));
        }

        // --- Clic = cocher/décocher ---
        holder.itemView.setOnClickListener(v -> {
            boolean newState = !task.isDone();
            task.setDone(newState);
            notifyItemChanged(holder.getAdapterPosition());
            if (checkedListener != null) checkedListener.onChecked(task.getId(), newState);
        });

        // --- Appui long = BottomSheet modifier/supprimer ---
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsBottomSheet(v, task);
            return true;
        });

        // --- En-tête de date masqué ---
        holder.layoutDateHeader.setVisibility(View.GONE);
    }

    private void showOptionsBottomSheet(View anchor, Task task) {
        BottomSheetDialog sheet = new BottomSheetDialog(anchor.getContext(),
                com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

        View sheetView = LayoutInflater.from(anchor.getContext())
                .inflate(R.layout.bottom_sheet_task_options, null);
        sheet.setContentView(sheetView);

        if (sheet.getWindow() != null) {
            sheet.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = sheetView.findViewById(R.id.tvBottomSheetTaskTitle);
        if (tvTitle != null) tvTitle.setText(task.getTitle());

        LinearLayout btnEdit = sheetView.findViewById(R.id.btnBottomSheetEdit);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                sheet.dismiss();
                if (editListener != null) editListener.onEdit(task);
            });
        }

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
}