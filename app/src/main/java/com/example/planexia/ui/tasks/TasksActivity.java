package com.example.planexia.ui.tasks;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
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
import com.example.planexia.ui.PremiumDialog;
import com.example.planexia.util.PdfExporter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executors;
import com.example.planexia.ui.modules.ModulesActivity;
import com.example.planexia.ui.progression.ProgressionActivity;
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

        userId = new com.example.planexia.data.SessionManager(this).getUserId();
        repository = new PlanexiaRepository(this);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        tvTodo       = findViewById(R.id.tvTodoCount);
        tvDone       = findViewById(R.id.tvDoneCount);

        CardView btnAdd = findViewById(R.id.btnAddTask);
        if (btnAdd != null) btnAdd.setOnClickListener(v -> showAddTaskDialog());

        Button btnExportPDF = findViewById(R.id.btnDecouvrirExportPDF);
        if (btnExportPDF != null) {
            // Texte du bouton selon statut premium
            if (repository.isPremium(userId)) {
                btnExportPDF.setText("Exporter en PDF");
            }
            btnExportPDF.setOnClickListener(v -> {
                if (repository.isPremium(userId)) {
                    exportPdf();
                } else {
                    PremiumDialog.show(this, () -> {
                        // Après activation premium : changer le texte et exporter
                        btnExportPDF.setText("Exporter en PDF");
                        exportPdf();
                    });
                }
            });
        }

        allTasks       = new ArrayList<>();
        displayedTasks = new ArrayList<>();

        adapter = new TaskAdapter(displayedTasks,
                (taskId, isDone) -> {
                    repository.setTaskDone(taskId, isDone);
                    for (Task t : allTasks) {
                        if (t.getId() == taskId) { t.setDone(isDone); break; }
                    }
                    applyFilter(currentFilter);
                    updateCounts();
                },
                task -> showEditTaskDialog(task),
                taskId -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Supprimer la tâche")
                            .setMessage("Es-tu sûr de vouloir supprimer cette tâche ?")
                            .setPositiveButton("Supprimer", (d, w) -> {
                                repository.deleteTask(taskId);
                                loadTasks();
                                Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Annuler", null)
                            .show();
                }
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);

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

    private void showAddTaskDialog() {
        List<Module> modules = repository.getModulesByUser(userId);
        if (modules.isEmpty()) {
            Toast.makeText(this, "Crée d'abord un module dans Matières", Toast.LENGTH_LONG).show();
            return;
        }
        showStep1ModuleDialog(modules);
    }

    private void showStep1ModuleDialog(List<Module> modules) {
        Dialog dialog = makeStepDialog();
        TextView tvTitle = dialog.findViewById(R.id.tvDialogStepTitle);
        TextView tvSub   = dialog.findViewById(R.id.tvDialogStepSub);
        Spinner spinner  = dialog.findViewById(R.id.spinnerChoice);
        Button btnCancel = dialog.findViewById(R.id.btnDialogStepCancel);
        Button btnNext   = dialog.findViewById(R.id.btnDialogStepNext);

        tvTitle.setText("Nouvelle tâche");
        tvSub.setText("1 / 3 — Choisir la matière");

        List<String> names = new ArrayList<>();
        for (Module m : modules) names.add(m.getName());
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnNext.setOnClickListener(v -> {
            Module selected = modules.get(spinner.getSelectedItemPosition());
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

    private void showStep2ObjectiveDialog(List<Objective> objectives) {
        Dialog dialog = makeStepDialog();
        TextView tvTitle = dialog.findViewById(R.id.tvDialogStepTitle);
        TextView tvSub   = dialog.findViewById(R.id.tvDialogStepSub);
        Spinner spinner  = dialog.findViewById(R.id.spinnerChoice);
        Button btnCancel = dialog.findViewById(R.id.btnDialogStepCancel);
        Button btnNext   = dialog.findViewById(R.id.btnDialogStepNext);

        tvTitle.setText("Nouvelle tâche");
        tvSub.setText("2 / 3 — Choisir l'objectif");

        List<String> titles = new ArrayList<>();
        for (Objective o : objectives) titles.add(o.getTitle());
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, titles));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnNext.setOnClickListener(v -> {
            Objective selected = objectives.get(spinner.getSelectedItemPosition());
            dialog.dismiss();
            showStep3TaskDialog(selected);
        });

        dialog.show();
    }

    private void showStep3TaskDialog(Objective objective) {
        long objectiveId = objective.getId();
        String objectiveDueDate = objective.getDueDate();

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

        final String[] selectedDate = {objectiveDueDate};
        if (objectiveDueDate != null && !objectiveDueDate.isEmpty()) {
            try {
                String[] parts = objectiveDueDate.split("-");
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int d = Integer.parseInt(parts[2]);
                String[] moisNoms = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                tvDateValue.setText(d + " " + moisNoms[m] + " " + y);
                tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                btnDate.setBackgroundResource(R.drawable.bg_edit_text);
            } catch (Exception ignored) {}
        }

        long maxDateMillis = Long.MAX_VALUE;
        if (objectiveDueDate != null && !objectiveDueDate.isEmpty()) {
            try {
                String[] parts = objectiveDueDate.split("-");
                Calendar calMax = Calendar.getInstance();
                calMax.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]), 23, 59, 59);
                maxDateMillis = calMax.getTimeInMillis();
            } catch (Exception ignored) {}
        }
        final long finalMaxDateMillis = maxDateMillis;

        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedDate[0] != null && !selectedDate[0].isEmpty()) {
                try {
                    String[] parts = selectedDate[0].split("-");
                    cal.set(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[2]));
                } catch (Exception ignored) {}
            }
            DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                String[] moisNoms = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                tvDateValue.setText(d + " " + moisNoms[m] + " " + y);
                tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                btnDate.setBackgroundResource(R.drawable.bg_edit_text);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            if (finalMaxDateMillis != Long.MAX_VALUE) {
                picker.getDatePicker().setMaxDate(finalMaxDateMillis);
            }
            picker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String resource = etResource.getText().toString().trim();
            boolean error   = false;

            if (TextUtils.isEmpty(title)) { etTitle.setError("Le titre est obligatoire"); error = true; }
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
                loadTasks();
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditTaskDialog(Task task) {
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

        btnAdd.setText("Modifier");
        etTitle.setText(task.getTitle());
        if (!TextUtils.isEmpty(task.getResourceText())) {
            etResource.setText(task.getResourceText());
        }

        final String[] selectedDate = {task.getDueDate()};
        if (!TextUtils.isEmpty(task.getDueDate())) {
            try {
                String[] parts = task.getDueDate().split("-");
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]) - 1;
                int d = Integer.parseInt(parts[2]);
                String[] moisNoms = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                tvDateValue.setText(d + " " + moisNoms[m] + " " + y);
                tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                btnDate.setBackgroundResource(R.drawable.bg_edit_text);
            } catch (Exception ignored) {}
        }

        long maxDateMillis = Long.MAX_VALUE;
        String objectiveDueDate = repository.getObjectiveDueDateForTask(task.getId());
        if (objectiveDueDate != null && !objectiveDueDate.isEmpty()) {
            try {
                String[] parts = objectiveDueDate.split("-");
                Calendar calMax = Calendar.getInstance();
                calMax.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]), 23, 59, 59);
                maxDateMillis = calMax.getTimeInMillis();
            } catch (Exception ignored) {}
        }
        final long finalMaxDateMillis = maxDateMillis;

        btnDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            if (selectedDate[0] != null && !selectedDate[0].isEmpty()) {
                try {
                    String[] parts = selectedDate[0].split("-");
                    cal.set(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[2]));
                } catch (Exception ignored) {}
            }
            DatePickerDialog picker = new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate[0] = String.format("%04d-%02d-%02d", y, m + 1, d);
                String[] moisNoms = {"jan.", "fév.", "mars", "avr.", "mai", "juin",
                        "juil.", "août", "sep.", "oct.", "nov.", "déc."};
                tvDateValue.setText(d + " " + moisNoms[m] + " " + y);
                tvDateValue.setTextColor(Color.parseColor("#1F1F1F"));
                btnDate.setBackgroundResource(R.drawable.bg_edit_text);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            if (finalMaxDateMillis != Long.MAX_VALUE) {
                picker.getDatePicker().setMaxDate(finalMaxDateMillis);
            }
            picker.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String title    = etTitle.getText().toString().trim();
            String resource = etResource.getText().toString().trim();
            boolean error   = false;

            if (TextUtils.isEmpty(title)) { etTitle.setError("Le titre est obligatoire"); error = true; }
            if (selectedDate[0] == null) {
                btnDate.setBackgroundResource(R.drawable.circle_outline_red);
                error = true;
            }
            if (error) return;

            int updated = repository.updateTask(task.getId(), title, selectedDate[0],
                    TextUtils.isEmpty(resource) ? null : resource);
            if (updated > 0) {
                dialog.dismiss();
                Toast.makeText(this, "Tâche modifiée ✓", Toast.LENGTH_SHORT).show();
                loadTasks();
            } else {
                Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private Dialog makeStepDialog() {
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

    private void setFilter(String filter, Button btnAll, Button btnLate, Button btnDone) {
        currentFilter = filter;
        resetButtons(btnAll, btnLate, btnDone);
        Button active = filter.equals("all") ? btnAll : filter.equals("late") ? btnLate : btnDone;
        active.setTextColor(Color.WHITE);
        active.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6C3FC5")));
        applyFilter(filter);
    }

    private void resetButtons(Button... buttons) {
        for (Button b : buttons) {
            b.setTextColor(Color.parseColor("#888888"));
            b.setBackgroundTintList(null);
        }
    }

    private void applyFilter(String filter) {
        String today = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
            today = LocalDate.now().toString();
        final String t = today;
        displayedTasks.clear();
        for (Task task : allTasks) {
            switch (filter) {
                case "late":
                    boolean isLate = !task.isDone() && !TextUtils.isEmpty(task.getDueDate())
                            && !t.isEmpty() && task.getDueDate().compareTo(t) < 0;
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

    private void exportPdf() {
        if (allTasks.isEmpty()) {
            Toast.makeText(this, "Aucune tâche à exporter", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Génération du PDF…", Toast.LENGTH_SHORT).show();
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Uri uri = PdfExporter.export(this, allTasks);
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                handler.post(() -> startActivity(android.content.Intent.createChooser(intent, "Ouvrir le PDF")));
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
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
                startActivity(new Intent(this, com.example.planexia.ProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}