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
import com.example.planexia.data.PlanexiaRepository; // ← AJOUT
import com.example.planexia.model.Module;
import com.example.planexia.ui.adapters.ModuleAdapter;
import com.example.planexia.ui.objectives.ObjectivesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ModulesActivity extends AppCompatActivity implements ModuleAdapter.OnModuleActionListener {

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<Module> moduleList;
    private ImageButton btnAddModule;
    private PlanexiaRepository repository; // ← AJOUT
    private long userId; // ← AJOUT

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
                                // ← AJOUT : mettre à jour en DB
                                repository.updateModule(module.getId(), name, coefficient, color);
                                module.setName(name);
                                module.setCoefficient(coefficient);
                                module.setColor(color);
                                moduleAdapter.notifyItemChanged(editPosition);
                                Toast.makeText(this, "Module modifié", Toast.LENGTH_SHORT).show();
                            } else {
                                // ← AJOUT : ajouter en DB
                                long newId = repository.addModule(userId, name, coefficient, color);
                                if (newId != -1) {
                                    moduleList.add(new Module((int) newId, name, coefficient, color));
                                    moduleAdapter.notifyItemInserted(moduleList.size() - 1);
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

        // ← AJOUT : init repository + userId temporaire
        repository = new PlanexiaRepository(this);
        SharedPreferences prefs = getSharedPreferences("planexia_session", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 1); // 1 = temporaire jusqu'au login

        rvModules = findViewById(R.id.rvModules);
        btnAddModule = findViewById(R.id.btnAddModule);

        // ← CHANGEMENT : remplace getFakeModules()
        moduleList = new ArrayList<>();
        moduleList.addAll(repository.getModulesByUser(userId));

        moduleAdapter = new ModuleAdapter(moduleList, this);
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);

        btnAddModule.setOnClickListener(v -> {
            Intent intent = new Intent(ModulesActivity.this, AddModuleActivity.class);
            addOrEditModuleLauncher.launch(intent);
        });

        // Bottom Navigation — inchangé
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_modules);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_modules) {
                return true;
            } else if (id == R.id.nav_tasks) {
                Toast.makeText(this, "Tâches - à venir", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_planning) {
                Toast.makeText(this, "Planning - à venir", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_progress) {
                Toast.makeText(this, "Progression - à venir", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profil - à venir", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    // ← AJOUT : recharger au retour
    @Override
    protected void onResume() {
        super.onResume();
        moduleList.clear();
        moduleList.addAll(repository.getModulesByUser(userId));
        moduleAdapter.notifyDataSetChanged();
    }

    // ← SUPPRIMÉ : getFakeModules()

    @Override
    public void onModuleClick(int position) {
        Module module = moduleList.get(position);
        Intent intent = new Intent(this, ObjectivesActivity.class);
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_ID, module.getId());
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_NAME, module.getName());
        intent.putExtra(ObjectivesActivity.EXTRA_MODULE_COLOR, module.getColor());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            // ← AJOUT : supprimer en DB
            Module module = moduleList.get(position);
            repository.deleteModule(module.getId());
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