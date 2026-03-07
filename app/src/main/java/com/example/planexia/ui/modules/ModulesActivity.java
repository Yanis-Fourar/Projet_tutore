package com.example.planexia.ui.modules;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Module;
import com.example.planexia.ui.adapters.ModuleAdapter;

import java.util.ArrayList;
import java.util.List;

public class ModulesActivity extends AppCompatActivity implements ModuleAdapter.OnModuleActionListener {

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<Module> moduleList;
    private ImageButton btnAddModule;

    private final ActivityResultLauncher<Intent> addOrEditModuleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                            String name = result.getData().getStringExtra("module_name");
                            int coefficient = result.getData().getIntExtra("module_coefficient", 1);
                            String color = result.getData().getStringExtra("module_color");

                            boolean isEditMode = result.getData().getBooleanExtra("edit_mode", false);
                            int editPosition = result.getData().getIntExtra("edit_position", -1);

                            if (isEditMode && editPosition != -1) {
                                Module module = moduleList.get(editPosition);
                                module.setName(name);
                                module.setCoefficient(coefficient);
                                module.setColor(color);
                                moduleAdapter.notifyItemChanged(editPosition);
                                Toast.makeText(this, "Module modifié", Toast.LENGTH_SHORT).show();
                            } else {
                                int newId = moduleList.size() + 1;
                                Module newModule = new Module(newId, name, coefficient, color);
                                moduleList.add(newModule);
                                moduleAdapter.notifyItemInserted(moduleList.size() - 1);
                                Toast.makeText(this, "Module ajouté", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        rvModules = findViewById(R.id.rvModules);
        btnAddModule = findViewById(R.id.btnAddModule);

        moduleList = getFakeModules();
        moduleAdapter = new ModuleAdapter(moduleList, this);

        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);

        btnAddModule.setOnClickListener(v -> {
            Intent intent = new Intent(ModulesActivity.this, AddModuleActivity.class);
            addOrEditModuleLauncher.launch(intent);
        });
    }

    private List<Module> getFakeModules() {
        List<Module> list = new ArrayList<>();
        list.add(new Module(1, "Mathématiques", 3, "#8B5CF6"));
        list.add(new Module(2, "Physique", 2, "#4F8EF7"));
        list.add(new Module(3, "Informatique", 4, "#10B981"));
        return list;
    }

    @Override
    public void onDeleteClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            moduleList.remove(position);
            moduleAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Module supprimé", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            Module module = moduleList.get(position);

            Intent intent = new Intent(ModulesActivity.this, AddModuleActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("edit_position", position);
            intent.putExtra("module_name", module.getName());
            intent.putExtra("module_coefficient", module.getCoefficient());
            intent.putExtra("module_color", module.getColor());

            addOrEditModuleLauncher.launch(intent);
        }
    }
}