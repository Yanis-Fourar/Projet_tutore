package com.example.planexia.ui.adapters;

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

    public ModuleAdapter(List<Module> moduleList) {
        this.moduleList = moduleList;
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
                    android.content.res.ColorStateList.valueOf(Color.parseColor(module.getColor()))
            );
        } catch (Exception e) {
            holder.viewColor.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.GRAY)
            );
        }

        holder.ivEdit.setOnClickListener(v -> {
            // plus tard : ouvrir AddModuleActivity en mode édition
        });

        holder.ivDelete.setOnClickListener(v -> {
            // plus tard : supprimer l'élément
        });
    }

    @Override
    public int getItemCount() {
        return moduleList.size();
    }

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