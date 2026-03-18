package com.example.planexia.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Objective;

import java.util.List;

public class ObjectiveAdapter extends RecyclerView.Adapter<ObjectiveAdapter.ObjectiveViewHolder> {

    private List<Objective> objectiveList;
    private String moduleColor;
    private OnObjectiveActionListener listener;

    public interface OnObjectiveActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public ObjectiveAdapter(List<Objective> objectiveList, String moduleColor, OnObjectiveActionListener listener) {
        this.objectiveList = objectiveList;
        this.moduleColor = moduleColor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObjectiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_objective, parent, false);
        return new ObjectiveViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectiveViewHolder holder, int position) {
        Objective objective = objectiveList.get(position);

        holder.tvTitle.setText(objective.getTitle());

        // Pastille couleur du module parent
        try {
            holder.viewColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor(moduleColor))
            );
        } catch (Exception e) {
            holder.viewColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.GRAY)
            );
        }

        // Jours restants (FS4.3) — utilise getDueDate()
        long days = objective.getDaysRemaining();
        if (days < 0) {
            holder.tvDaysRemaining.setText("En retard de " + Math.abs(days) + "j");
            holder.tvDaysRemaining.setTextColor(Color.parseColor("#EF4444"));
        } else if (days == 0) {
            holder.tvDaysRemaining.setText("Aujourd'hui !");
            holder.tvDaysRemaining.setTextColor(Color.parseColor("#F59E0B"));
        } else {
            holder.tvDaysRemaining.setText("J-" + days);
            holder.tvDaysRemaining.setTextColor(Color.parseColor("#10B981"));
        }

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(holder.getAdapterPosition());
        });
        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return objectiveList.size(); }

    public static class ObjectiveViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvTitle, tvDaysRemaining;
        ImageView ivEdit, ivDelete;

        public ObjectiveViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDaysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}