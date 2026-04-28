package com.example.planexia.ui.tasks;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.planexia.ui.modules.ModulesActivity;
import com.example.planexia.ui.progression.ProgressionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ObjectiveDetailActivity extends AppCompatActivity {

    public static final String EXTRA_OBJECTIVE_ID    = "EXTRA_OBJECTIVE_ID";
    public static final String EXTRA_OBJECTIVE_TITLE = "EXTRA_OBJECTIVE_TITLE";

    private long objectiveId;
    private String objectiveTitle;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList;

    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvProgressLabel;
    private TextView tvObjectiveTitle;
    private Button btnAddTask;

    private PlanexiaRepository repository;
    private PlanexiaDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objective_detail);

        objectiveId    = getIntent().getLongExtra(EXTRA_OBJECTIVE_ID, -1);
        objectiveTitle = getIntent().getStringExtra(EXTRA_OBJECTIVE_TITLE);

        if (objectiveId == -1) {
            Toast.makeText(this, "Erreur : objectif introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repository = new PlanexiaRepository(this);
        dbHelper   = new PlanexiaDatabaseHelper(this);

        recyclerView      = findViewById(R.id.recyclerViewObjectiveTasks);
        progressBar       = findViewById(R.id.progressBarObjective);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressLabel   = findViewById(R.id.tvProgressLabel);
        tvObjectiveTitle  = findViewById(R.id.tvObjectiveTitle);
        btnAddTask        = findViewById(R.id.btnAddTask);

        if (tvObjectiveTitle != null && objectiveTitle != null) {
            tvObjectiveTitle.setText(objectiveTitle);
        }

        // Bouton retour
        androidx.cardview.widget.CardView btnBack = findViewById(R.id.btnBackDetail);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Init liste + adapter
        taskList = new ArrayList<>();
        adapter  = new TaskAdapter(taskList, (taskId, isDone) -> {
            repository.setTaskDone(taskId, isDone);
            refreshProgress();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (btnAddTask != null) btnAddTask.setOnClickListener(v -> showAddTaskDialog());

        setupBottomNav();
        loadTasks();
    }

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
            taskList.add(new Task(
                    c.getLong(0), c.getString(1), c.getInt(2) == 1,
                    c.getString(3), c.getString(4)
            ));
        }
        c.close();
        adapter.notifyDataSetChanged();
        refreshProgress();
    }

    private void refreshProgress() {
        int progress = repository.getObjectiveProgress(objectiveId);
        if (progressBar != null) progressBar.setProgress(progress);
        if (tvProgressPercent != null) tvProgressPercent.setText(progress + "%");
        int done = 0;
        for (Task t : taskList) if (t.isDone()) done++;
        if (tvProgressLabel != null)
            tvProgressLabel.setText(done + " / " + taskList.size() + " tâches complétées");
    }

    private void showAddTaskDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_task);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        EditText etTitle     = dialog.findViewById(R.id.etTaskTitle);
        EditText etResource  = dialog.findViewById(R.id.etTaskResource);
        LinearLayout btnDate = dialog.findViewById(R.id.btnPickDate);
        TextView tvDateValue = dialog.findViewById(R.id.tvDateValue);
        Button btnCancel     = dialog.findViewById(R.id.btnDialogCancel);
        Button btnAdd        = dialog.findViewById(R.id.btnDialogAdd);

        final String[] selectedDate = {null};

        // ✅ Récupérer la date limite de l'objectif pour bloquer le DatePicker
        String objectiveDueDate = getObjectiveDueDate();
        long objectiveMaxMillis = Long.MAX_VALUE;
        if (objectiveDueDate != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date d = sdf.parse(objectiveDueDate);
                if (d != null) objectiveMaxMillis = d.getTime();
            } catch (ParseException ignored) {}
        }
        final long maxDateMillis = objectiveMaxMillis;

        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day   = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        selectedDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                        String[] mois = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                                "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                        tvDateValue.setText(d + " " + mois[m] + " " + y);
                        tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                        btnDate.setBackgroundResource(R.drawable.bg_edit_text);
                    },
                    year, month, day
            );

            // ✅ Date minimum = aujourd'hui
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            // ✅ Date maximum = date limite de l'objectif (si elle existe)
            if (maxDateMillis != Long.MAX_VALUE) {
                datePicker.getDatePicker().setMaxDate(maxDateMillis);
            }

            datePicker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String resource = etResource.getText().toString().trim();
            boolean hasError = false;

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Le titre est obligatoire");
                hasError = true;
            }
            if (selectedDate[0] == null) {
                btnDate.setBackgroundResource(R.drawable.circle_outline_red);
                tvDateValue.setHint("⚠ Veuillez choisir une date");
                hasError = true;
            }
            if (hasError) return;

            String resourceToSave = TextUtils.isEmpty(resource) ? null : resource;
            long newId = repository.addTask(objectiveId, title, selectedDate[0], resourceToSave);
            if (newId != -1) {
                taskList.add(new Task(newId, title, false, selectedDate[0], resourceToSave));
                adapter.notifyItemInserted(taskList.size() - 1);
                refreshProgress();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /** Récupère la date limite (due_date) de l'objectif courant */
    private String getObjectiveDueDate() {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(
                    PlanexiaDatabaseHelper.T_OBJECTIVES,
                    new String[]{PlanexiaDatabaseHelper.C_DUE_DATE},
                    PlanexiaDatabaseHelper.C_ID + " = ?",
                    new String[]{String.valueOf(objectiveId)},
                    null, null, null
            );
            String dueDate = null;
            if (c.moveToFirst()) dueDate = c.getString(0);
            c.close();
            return dueDate;
        } catch (Exception e) {
            return null;
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        if (bottomNav == null) return;
        bottomNav.setSelectedItemId(R.id.nav_taches);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_taches) return true;
            else if (id == R.id.nav_matieres) {
                startActivity(new Intent(this, ModulesActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_progression) {
                startActivity(new Intent(this, ProgressionActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_planning) {
                startActivity(new Intent(this, com.example.planexia.PlanningActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profil) {
                startActivity(new android.content.Intent(this, com.example.planexia.ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}