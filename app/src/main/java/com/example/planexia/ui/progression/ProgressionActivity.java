package com.example.planexia.ui.progression;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.model.Module;
import com.example.planexia.ui.modules.ModulesActivity;
import com.example.planexia.ui.tasks.TasksActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ProgressionActivity extends AppCompatActivity {

    private PlanexiaRepository repository;
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progression);

        SharedPreferences prefs = getSharedPreferences("planexia_session", MODE_PRIVATE);
        userId = prefs.getLong("user_id", 1);

        repository = new PlanexiaRepository(this);

        loadData();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        if (userId == -1) return;

        int globalProgress = repository.getGlobalProgress(userId);
        int doneTasks      = repository.getCompletedTasksCount(userId);
        int totalTasks     = repository.getTotalTasksCount(userId);

        TextView tvProgress  = findViewById(R.id.tvGlobalPercent);
        TextView tvDoneTasks = findViewById(R.id.tvDoneTasksCount);
        TextView tvTotal     = findViewById(R.id.tvTotalTasksCount);
        ProgressBar pbGlobal = findViewById(R.id.pbGlobal);

        if (tvProgress  != null) tvProgress.setText(globalProgress + "%");
        if (tvDoneTasks != null) tvDoneTasks.setText(String.valueOf(doneTasks));
        if (tvTotal     != null) tvTotal.setText(totalTasks + " tâches");
        if (pbGlobal    != null) pbGlobal.setProgress(globalProgress);

        LinearLayout containerModules = findViewById(R.id.containerModules);
        if (containerModules == null) return;
        containerModules.removeAllViews();

        List<Module> modules = repository.getModulesByUser(userId);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Module module : modules) {
            int[] prog = repository.getProgressForModule(module.getId());
            int total = prog[0];
            int done  = prog[1];
            int pct   = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);

            View row = inflater.inflate(R.layout.item_module_progress, containerModules, false);

            View dot = row.findViewById(R.id.viewModuleDot);
            try {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(module.getColor())));
            } catch (Exception e) {
                dot.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#7B1FA2")));
            }

            TextView tvName = row.findViewById(R.id.tvModuleProgressName);
            tvName.setText(module.getName());

            TextView tvFraction = row.findViewById(R.id.tvModuleProgressFraction);
            tvFraction.setText(done + "/" + total + " (" + pct + "%)");

            ProgressBar pb = row.findViewById(R.id.pbModule);
            pb.setProgress(pct);
            try {
                pb.setProgressTintList(ColorStateList.valueOf(Color.parseColor(module.getColor())));
            } catch (Exception e) {
                pb.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#7B1FA2")));
            }

            containerModules.addView(row);
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_progression);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_progression) return true;
            else if (id == R.id.nav_matieres) {
                startActivity(new Intent(this, ModulesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_taches) {
                startActivity(new Intent(this, TasksActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_planning) {
                startActivity(new Intent(this, com.example.planexia.PlanningActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}