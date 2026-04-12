package com.example.planexia.ui.tasks;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.model.Module;
import com.example.planexia.model.Objective;
import com.example.planexia.model.Task;
import com.example.planexia.ui.modules.ModulesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTodo;
    private TextView tvDone;
    private TextView tvSubtitle;

    private List<Task> allTasks;
    private List<Task> displayedTasks;
    private TaskAdapter adapter;

    private PlanexiaRepository repository;
    private long userId = -1;

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        SharedPreferences prefs = getSharedPreferences("planexia_prefs", MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);

        repository = new PlanexiaRepository(this);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        tvTodo       = findViewById(R.id.tvTodoCount);
        tvDone       = findViewById(R.id.tvDoneCount);
        tvSubtitle   = findViewById(R.id.tvSubtitle);

        // ✅ Bouton + → ouvre le dialog 3 étapes
        CardView btnAdd = findViewById(R.id.btnAddTask);
        if (btnAdd != null) {
            btnAdd.setOnClickListener(v -> showAddTaskDialog());
        }

        allTasks       = new ArrayList<>();
        displayedTasks = new ArrayList<>();

        adapter = new TaskAdapter(displayedTasks, (taskId, isDone) -> {
            repository.setTaskDone(taskId, isDone);
            for (Task t : allTasks) {
                if (t.getId() == taskId) { t.setDone(isDone); break; }
            }
            applyFilter(currentFilter);
            updateCounts();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Filtres
        Button btnFilterAll  = findViewById(R.id.btnFilterAll);
        Button btnFilterLate = findViewById(R.id.btnFilterLate);
        Button btnFilterDone = findViewById(R.id.btnFilterDone);

        if (btnFilterAll  != null) btnFilterAll.setOnClickListener(v  -> setFilter("all",  btnFilterAll, btnFilterLate, btnFilterDone));
        if (btnFilterLate != null) btnFilterLate.setOnClickListener(v -> setFilter("late", btnFilterAll, btnFilterLate, btnFilterDone));
        if (btnFilterDone != null) btnFilterDone.setOnClickListener(v -> setFilter("done", btnFilterAll, btnFilterLate, btnFilterDone));

        setupBottomNav();
        loadTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void loadTasks() {
        allTasks.clear();
        if (userId != -1) allTasks.addAll(repository.getAllTasksForUser(userId));
        applyFilter(currentFilter);
        updateCounts();
    }

    // ─────────────────────────────────────────────
    //  DIALOG 3 ÉTAPES
    //  Étape 1 : choisir le module
    //  Étape 2 : choisir l'objectif
    //  Étape 3 : titre + date
    // ─────────────────────────────────────────────
    private void showAddTaskDialog() {
        // Charger les modules de l'utilisateur
        List<Module> modules = repository.getModulesByUser(userId);

        if (modules.isEmpty()) {
            Toast.makeText(this, "Crée d'abord un module dans Matières", Toast.LENGTH_LONG).show();
            return;
        }

        showStep1ModuleDialog(modules);
    }

    /** Étape 1 — Choisir le module */
    private void showStep1ModuleDialog(List<Module> modules) {
        Dialog dialog = makeDialog();

        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogStepTitle);
        TextView tvDialogSub   = dialog.findViewById(R.id.tvDialogStepSub);
        Spinner spinner        = dialog.findViewById(R.id.spinnerChoice);
        Button btnCancel       = dialog.findViewById(R.id.btnDialogStepCancel);
        Button btnNext         = dialog.findViewById(R.id.btnDialogStepNext);

        tvDialogTitle.setText("Nouvelle tâche");
        tvDialogSub.setText("1 / 3 — Choisir la matière");

        List<String> moduleNames = new ArrayList<>();
        for (Module m : modules) moduleNames.add(m.getName());
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, moduleNames));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnNext.setOnClickListener(v -> {
            int pos = spinner.getSelectedItemPosition();
            Module selected = modules.get(pos);

            List<Objective> objectives = repository.getObjectivesDetailByModule(selected.getId());
            if (objectives.isEmpty()) {
                Toast.makeText(this, "Ce module n'a pas encore d'objectif", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            showStep2ObjectiveDialog(objectives);
        });

        dialog.show();
    }

    /** Étape 2 — Choisir l'objectif */
    private void showStep2ObjectiveDialog(List<Objective> objectives) {
        Dialog dialog = makeDialog();

        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogStepTitle);
        TextView tvDialogSub   = dialog.findViewById(R.id.tvDialogStepSub);
        Spinner spinner        = dialog.findViewById(R.id.spinnerChoice);
        Button btnCancel       = dialog.findViewById(R.id.btnDialogStepCancel);
        Button btnNext         = dialog.findViewById(R.id.btnDialogStepNext);

        tvDialogTitle.setText("Nouvelle tâche");
        tvDialogSub.setText("2 / 3 — Choisir l'objectif");
        btnNext.setText("Suivant");

        List<String> objectiveTitles = new ArrayList<>();
        for (Objective o : objectives) objectiveTitles.add(o.getTitle());
        spinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, objectiveTitles));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnNext.setOnClickListener(v -> {
            int pos = spinner.getSelectedItemPosition();
            Objective selected = objectives.get(pos);
            dialog.dismiss();
            showStep3TaskDialog(selected.getId());
        });

        dialog.show();
    }

    /** Étape 3 — Titre + date */
    private void showStep3TaskDialog(long objectiveId) {
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

        // Changer le titre du dialog pour l'étape 3
        TextView tvTitle = dialog.findViewById(R.id.tvDialogAddTaskTitle);
        if (tvTitle != null) tvTitle.setText("3 / 3 — Détails de la tâche");

        final String[] selectedDate = {null};

        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                String[] mois = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                tvDateValue.setText(d + " " + mois[m] + " " + y);
                tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                btnDate.setBackgroundResource(R.drawable.bg_edit_text);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            {{getDatePicker().setMinDate(System.currentTimeMillis() - 1000);}}
                    .show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String resource = etResource.getText().toString().trim();
            boolean error   = false;

            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Le titre est obligatoire");
                error = true;
            }
            if (selectedDate[0] == null) {
                btnDate.setBackgroundResource(R.drawable.circle_outline_red);
                tvDateValue.setHint("⚠ Veuillez choisir une date");
                error = true;
            }
            if (error) return;

            long newId = repository.addTask(objectiveId, title, selectedDate[0],
                    TextUtils.isEmpty(resource) ? null : resource);

            if (newId != -1) {
                dialog.dismiss();
                Toast.makeText(this, "Tâche ajoutée ✓", Toast.LENGTH_SHORT).show();
                loadTasks(); // rafraîchir la liste
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /** Crée un dialog réutilisable pour les étapes 1 et 2 */
    private Dialog makeDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_step_selector);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.92f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        return dialog;
    }

    // ─────────────────────────────────────────────
    //  FILTRES
    // ─────────────────────────────────────────────
    private void setFilter(String filter, Button btnAll, Button btnLate, Button btnDone) {
        currentFilter = filter;
        resetFilterButtons(btnAll, btnLate, btnDone);
        Button active = filter.equals("all") ? btnAll : filter.equals("late") ? btnLate : btnDone;
        active.setTextColor(Color.WHITE);
        active.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#6C3FC5")));
        applyFilter(filter);
    }

    private void resetFilterButtons(Button... buttons) {
        for (Button b : buttons) {
            b.setTextColor(Color.parseColor("#888888"));
            b.setBackgroundTintList(null);
        }
    }

    private void applyFilter(String filter) {
        String today = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            today = LocalDate.now().toString();
        }
        final String t = today;
        displayedTasks.clear();
        for (Task task : allTasks) {
            switch (filter) {
                case "late":
                    boolean isLate = !task.isDone()
                            && !TextUtils.isEmpty(task.getDueDate())
                            && !t.isEmpty()
                            && task.getDueDate().compareTo(t) < 0;
                    if (isLate) displayedTasks.add(task);
                    break;
                case "done":
                    if (task.isDone()) displayedTasks.add(task);
                    break;
                default:
                    displayedTasks.add(task);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateCounts() {
        int done = 0;
        for (Task t : allTasks) if (t.isDone()) done++;
        int todo = allTasks.size() - done;
        if (tvTodo != null) tvTodo.setText(String.valueOf(todo));
        if (tvDone != null) tvDone.setText(String.valueOf(done));
        if (tvSubtitle != null)
            tvSubtitle.setText(todo + " tâche" + (todo > 1 ? "s" : "") + " active" + (todo > 1 ? "s" : ""));
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
            }
            return false;
        });
    }
}