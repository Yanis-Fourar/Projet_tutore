package com.example.planexia.ui.modules;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Module;
import com.example.planexia.ui.adapters.ModuleAdapter;

import java.util.ArrayList;
import java.util.List;

public class ModulesActivity extends AppCompatActivity {

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<Module> moduleList;
    private ImageButton btnAddModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modules);

        rvModules = findViewById(R.id.rvModules);
        btnAddModule = findViewById(R.id.btnAddModule);

        // 1. préparer les fake data
        moduleList = getFakeModules();

        // 2. créer l'adapter
        moduleAdapter = new ModuleAdapter(moduleList);

        // 3. configurer le RecyclerView
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        rvModules.setAdapter(moduleAdapter);

        // 4. bouton ajout
        btnAddModule.setOnClickListener(v -> {
            // plus tard : startActivity(new Intent(this, AddModuleActivity.class));
        });
    }

    private List<Module> getFakeModules() {
        List<Module> list = new ArrayList<>();
        list.add(new Module(1, "Mathématiques", 3, "#8B5CF6"));
        list.add(new Module(2, "Physique", 2, "#4F8EF7"));
        list.add(new Module(3, "Informatique", 4, "#10B981"));
        return list;
    }
}