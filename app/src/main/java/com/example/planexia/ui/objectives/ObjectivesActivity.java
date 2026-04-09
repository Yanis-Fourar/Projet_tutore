package com.example.planexia.ui.objectives;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Objective;
import com.example.planexia.ui.adapters.ObjectiveAdapter;

import java.util.ArrayList;
import java.util.List;

public class ObjectivesActivity extends AppCompatActivity implements ObjectiveAdapter.OnObjectiveActionListener {

    public static final String EXTRA_MODULE_ID    = "module_id";
    public static final String EXTRA_MODULE_NAME  = "module_name";
    public static final String EXTRA_MODULE_COLOR = "module_color";

    // Clé cohérente avec C_DUE_DATE de la DB
    public static final String KEY_DUE_DATE = "objective_due_date";
    public static final String KEY_TITLE    = "objective_title";

    private RecyclerView rvObjectives;
    private ObjectiveAdapter objectiveAdapter;
    private List<Objective> objectiveList;
    private ImageButton btnAddObjective;
    private TextView tvModuleName;

    private int moduleId;
    private String moduleName;
    private String moduleColor;

    private final ActivityResultLauncher<Intent> addOrEditObjectiveLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String title   = result.getData().getStringExtra(KEY_TITLE);
                            String dueDate = result.getData().getStringExtra(KEY_DUE_DATE);
                            boolean isEditMode  = result.getData().getBooleanExtra("edit_mode", false);
                            int editPosition    = result.getData().getIntExtra("edit_position", -1);

                            if (isEditMode && editPosition != -1) {
                                Objective obj = objectiveList.get(editPosition);
                                obj.setTitle(title);
                                obj.setDueDate(dueDate);
                                objectiveAdapter.notifyItemChanged(editPosition);
                                Toast.makeText(this, "Objectif modifié", Toast.LENGTH_SHORT).show();
                            } else {
                                int newId = objectiveList.size() + 1;
                                objectiveList.add(new Objective(newId, moduleId, title, dueDate));
                                objectiveAdapter.notifyItemInserted(objectiveList.size() - 1);
                                Toast.makeText(this, "Objectif ajouté", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectives);

        moduleId    = getIntent().getIntExtra(EXTRA_MODULE_ID, -1);
        moduleName  = getIntent().getStringExtra(EXTRA_MODULE_NAME);
        moduleColor = getIntent().getStringExtra(EXTRA_MODULE_COLOR);

        tvModuleName    = findViewById(R.id.tvModuleName);
        rvObjectives    = findViewById(R.id.rvObjectives);
        btnAddObjective = findViewById(R.id.btnAddObjective);

        tvModuleName.setText(moduleName != null ? moduleName : "Objectifs");

        objectiveList    = getFakeObjectives();
        objectiveAdapter = new ObjectiveAdapter(objectiveList, moduleColor, this);
        rvObjectives.setLayoutManager(new LinearLayoutManager(this));
        rvObjectives.setAdapter(objectiveAdapter);

        btnAddObjective.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddObjectiveActivity.class);
            addOrEditObjectiveLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    // Données de test — à remplacer par repository.getObjectivesByModule(moduleId) plus tard
    private List<Objective> getFakeObjectives() {
        List<Objective> list = new ArrayList<>();
        list.add(new Objective(1, moduleId, "Réviser le chapitre 3", "2026-04-15"));
        list.add(new Objective(2, moduleId, "Faire les exercices TD2", "2026-03-20"));
        return list;
    }

    @Override
    public void onEditClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            Objective obj = objectiveList.get(position);
            Intent intent = new Intent(this, AddObjectiveActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("edit_position", position);
            intent.putExtra(KEY_TITLE, obj.getTitle());
            intent.putExtra(KEY_DUE_DATE, obj.getDueDate());
            addOrEditObjectiveLauncher.launch(intent);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            objectiveList.remove(position);
            objectiveAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Objectif supprimé", Toast.LENGTH_SHORT).show();
        }
    }
}