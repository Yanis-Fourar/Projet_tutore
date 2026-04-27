package com.example.planexia.ui.modules;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.model.Module;
import com.example.planexia.ui.adapters.ModuleAdapter;
import com.example.planexia.ui.objectives.ObjectivesActivity;
import com.example.planexia.ui.progression.ProgressionActivity;
import com.example.planexia.ui.tasks.TasksActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ModulesActivity extends AppCompatActivity implements ModuleAdapter.OnModuleActionListener {

    private static final int FREE_MODULE_LIMIT = 3;

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<Module> moduleList;
    private ImageButton btnAddModule;
    private android.widget.TextView tvModulesCount;
    private PlanexiaRepository repository;
    private long userId;

    private final ActivityResultLauncher<Intent> addOrEditModuleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String name        = result.getData().getStringExtra("module_name");
                            int coefficient    = result.getData().getIntExtra("module_coefficient", 1);
                            String color       = result.getData().getStringExtra("module_color");
                            boolean isEditMode = result.getData().getBooleanExtra("edit_mode", false);
                            int editPosition   = result.getData().getIntExtra("edit_position", -1);

                            if (isEditMode && editPosition != -1) {
                                Module module = moduleList.get(editPosition);
                                repository.updateModule(module.getId(), name, coefficient, color);
                                module.setName(name);
                                module.setCoefficient(coefficient);
                                module.setColor(color);
                                moduleAdapter.notifyItemChanged(editPosition);
                                Toast.makeText(this, "Module modifié", Toast.LENGTH_SHORT).show();
                            } else {
                                long newId = repository.addModule(userId, name, coefficient, color);
                                if (newId != -1) {
                                    moduleList.add(new Module((int) newId, name, coefficient, color));
                                    moduleAdapter.notifyItemInserted(moduleList.size() - 1);
                                    updateModulesCount();
                                    Toast.makeText(this, "Module ajouté", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        repository = new PlanexiaRepository(this);
        SharedPreferences prefs = getSharedPreferences("planexia_session", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 1); // 1 = temporaire jusqu'au login

        rvModules      = findViewById(R.id.rvModules);
        btnAddModule   = findViewById(R.id.btnAddModule);
        tvModulesCount = findViewById(R.id.tvModulesCount);

        moduleList    = new ArrayList<>();
        moduleAdapter = new ModuleAdapter(moduleList, this);
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);

        reloadModules();

        btnAddModule.setOnClickListener(v -> {
            Intent intent = new Intent(ModulesActivity.this, AddModuleActivity.class);
            addOrEditModuleLauncher.launch(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_matieres);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_matieres) {
                    return true;
                } else if (id == R.id.nav_taches) {
                    startActivity(new Intent(this, TasksActivity.class));
                    return true;
                } else if (id == R.id.nav_progression) {
                    startActivity(new Intent(this, ProgressionActivity.class));
                    return true;
                } else if (id == R.id.nav_planning) {
                    startActivity(new Intent(this, com.example.planexia.PlanningActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_profil) {
                    Toast.makeText(this, "Profil bientôt disponible", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadModules();
    }

    private void reloadModules() {
        moduleList.clear();
        moduleList.addAll(repository.getModulesByUser(userId));
        moduleAdapter.notifyDataSetChanged();
        updateModulesCount();
    }

    private void updateModulesCount() {
        if (tvModulesCount != null) {
            tvModulesCount.setText(moduleList.size() + "/" + FREE_MODULE_LIMIT + " matières utilisées");
        }
    }

    @Override
    public void onModuleClick(int position) {
        if (position < 0 || position >= moduleList.size()) return;
        Module module = moduleList.get(position);
        Intent intent = new Intent(this, ObjectivesActivity.class);
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_ID, module.getId());
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_NAME, module.getName());
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_COLOR, module.getColor());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        if (position < 0 || position >= moduleList.size()) return;
        Module module = moduleList.get(position);
        repository.deleteModule(module.getId());
        moduleList.remove(position);
        moduleAdapter.notifyItemRemoved(position);
        updateModulesCount();
        Toast.makeText(this, "Module supprimé", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(int position) {
        if (position < 0 || position >= moduleList.size()) return;
        Module module = moduleList.get(position);
        Intent intent = new Intent(ModulesActivity.this, AddModuleActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("edit_position", position);
        intent.putExtra("edit_module_id", module.getId());
        intent.putExtra("module_name", module.getName());
        intent.putExtra("module_coefficient", module.getCoefficient());
        intent.putExtra("module_color", module.getColor());
        addOrEditModuleLauncher.launch(intent);
    }
}