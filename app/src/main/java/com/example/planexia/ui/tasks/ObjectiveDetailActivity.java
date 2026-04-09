package com.example.planexia.ui.tasks;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaDatabaseHelper;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.model.Task;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveDetailActivity extends AppCompatActivity {

    // Clé pour recevoir l'id de l'objectif depuis l'activité précédente
    public static final String EXTRA_OBJECTIVE_ID = "EXTRA_OBJECTIVE_ID";
    public static final String EXTRA_OBJECTIVE_TITLE = "EXTRA_OBJECTIVE_TITLE";

    private long objectiveId;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvProgressLabel;
    private Button btnAddTask;

    private PlanexiaRepository repository;
    private PlanexiaDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objective_detail);

        // Récupérer l'objectif passé en intent
        objectiveId = getIntent().getLongExtra(EXTRA_OBJECTIVE_ID, -1);
        String objectiveTitle = getIntent().getStringExtra(EXTRA_OBJECTIVE_TITLE);

        if (objectiveId == -1) {
            Toast.makeText(this, "Erreur : objectif introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Init Repository et DB
        repository = new PlanexiaRepository(this);
        dbHelper = new PlanexiaDatabaseHelper(this);

        // Lier les vues
        recyclerView     = findViewById(R.id.recyclerViewObjectiveTasks);
        progressBar      = findViewById(R.id.progressBarObjective);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressLabel  = findViewById(R.id.tvProgressLabel);
        btnAddTask       = findViewById(R.id.btnAddTask);

        // Titre de l'écran
        if (getSupportActionBar() != null && objectiveTitle != null) {
            getSupportActionBar().setTitle(objectiveTitle);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Init liste + adapter
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, (taskId, isDone) -> {
            // Callback appelé quand une checkbox est cochée/décochée
            repository.setTaskDone(taskId, isDone);
            refreshProgress();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Bouton ajouter une task
        btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        // Charger les données depuis la DB
        loadTasks();
    }

    // Charge les tasks depuis la base de données
    private void loadTasks() {
        taskList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_TASKS,
                new String[]{
                        PlanexiaDatabaseHelper.C_ID,
                        PlanexiaDatabaseHelper.C_TITLE,
                        PlanexiaDatabaseHelper.C_IS_DONE,
                        PlanexiaDatabaseHelper.C_DUE_DATE,
                        PlanexiaDatabaseHelper.C_RESOURCE_TEXT
                },
                PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?",
                new String[]{String.valueOf(objectiveId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " ASC"
        );

        while (c.moveToNext()) {
            long id            = c.getLong(0);
            String title       = c.getString(1);
            boolean isDone     = c.getInt(2) == 1;
            String dueDate     = c.getString(3);
            String resource    = c.getString(4);

            taskList.add(new Task(id, title, isDone, dueDate, resource));
        }
        c.close();

        adapter.notifyDataSetChanged();
        refreshProgress();
    }

    // Met à jour la barre de progression
    private void refreshProgress() {
        int progress = repository.getObjectiveProgress(objectiveId);
        progressBar.setProgress(progress);
        tvProgressPercent.setText(progress + "%");

        int done = 0;
        for (Task t : taskList) {
            if (t.isDone()) done++;
        }
        tvProgressLabel.setText(done + " / " + taskList.size() + " tâches complétées");
    }

    // Dialog pour ajouter une nouvelle task
    private void showAddTaskDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);

        EditText etTitle    = dialogView.findViewById(R.id.etTaskTitle);
        EditText etDate     = dialogView.findViewById(R.id.etTaskDate);
        EditText etResource = dialogView.findViewById(R.id.etTaskResource);

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle tâche")
                .setView(dialogView)
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String title    = etTitle.getText().toString().trim();
                    String date     = etDate.getText().toString().trim();
                    String resource = etResource.getText().toString().trim();

                    if (TextUtils.isEmpty(title)) {
                        Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Date vide → null
                    String dateToSave     = TextUtils.isEmpty(date)     ? null : date;
                    String resourceToSave = TextUtils.isEmpty(resource) ? null : resource;

                    long newId = repository.addTask(objectiveId, title, dateToSave, resourceToSave);

                    if (newId != -1) {
                        taskList.add(new Task(newId, title, false, dateToSave, resourceToSave));
                        adapter.notifyItemInserted(taskList.size() - 1);
                        refreshProgress();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}