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
import com.example.planexia.model.Module;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private List<Module> moduleList;
    private OnModuleActionListener listener;

    public interface OnModuleActionListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
        void onModuleClick(int position); // clic sur l'item → ouvre les objectifs
    }

    public ModuleAdapter(List<Module> moduleList, OnModuleActionListener listener) {
        this.moduleList = moduleList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = moduleList.get(position);

        holder.tvName.setText(module.getName());
        holder.tvCoefficient.setText("Coefficient " + module.getCoefficient());

        try {
            holder.viewColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.parseColor(module.getColor()))
            );
        } catch (Exception e) {
            holder.viewColor.setBackgroundTintList(
                    ColorStateList.valueOf(Color.GRAY)
            );
        }

        // Clic sur l'item → objectifs
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onModuleClick(holder.getAdapterPosition());
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(holder.getAdapterPosition());
        });

        holder.ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return moduleList.size(); }

    public static class ModuleViewHolder extends RecyclerView.ViewHolder {
        View viewColor;
        TextView tvName, tvCoefficient;
        ImageView ivEdit, ivDelete;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
            tvName = itemView.findViewById(R.id.tvName);
            tvCoefficient = itemView.findViewById(R.id.tvCoefficient);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}