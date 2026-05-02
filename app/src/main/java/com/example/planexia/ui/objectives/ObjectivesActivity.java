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
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.model.Objective;
import com.example.planexia.ui.adapters.ObjectiveAdapter;
import com.example.planexia.ui.tasks.ObjectiveDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ObjectivesActivity extends AppCompatActivity implements ObjectiveAdapter.OnObjectiveActionListener {

    public static final String EXTRA_MODULE_ID    = "module_id";
    public static final String EXTRA_MODULE_NAME  = "module_name";
    public static final String EXTRA_MODULE_COLOR = "module_color";

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

    private PlanexiaRepository repository;

    private final ActivityResultLauncher<Intent> addOrEditObjectiveLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            String title         = result.getData().getStringExtra(KEY_TITLE);
                            String dueDate       = result.getData().getStringExtra(KEY_DUE_DATE);
                            boolean isEditMode   = result.getData().getBooleanExtra("edit_mode", false);
                            long editObjectiveId = result.getData().getLongExtra("edit_objective_id", -1);

                            if (isEditMode && editObjectiveId != -1) {
                                // Modifier en DB — onResume() rechargera la liste proprement
                                repository.updateObjective(editObjectiveId, title, dueDate);
                                Toast.makeText(this, "Objectif modifié", Toast.LENGTH_SHORT).show();
                            } else {
                                // Ajouter en DB — onResume() rechargera la liste proprement
                                long newId = repository.addObjective(moduleId, title, dueDate);
                                if (newId != -1) {
                                    Toast.makeText(this, "Objectif ajouté", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_objectives);

        moduleId    = getIntent().getIntExtra(EXTRA_MODULE_ID, -1);
        moduleName  = getIntent().getStringExtra(EXTRA_MODULE_NAME);
        moduleColor = getIntent().getStringExtra(EXTRA_MODULE_COLOR);

        repository = new PlanexiaRepository(this);

        tvModuleName    = findViewById(R.id.tvModuleName);
        rvObjectives    = findViewById(R.id.rvObjectives);
        btnAddObjective = findViewById(R.id.btnAddObjective);

        tvModuleName.setText(moduleName != null ? moduleName : "Objectifs");

        objectiveList    = new ArrayList<>();
        objectiveAdapter = new ObjectiveAdapter(objectiveList, moduleColor, this);
        rvObjectives.setLayoutManager(new LinearLayoutManager(this));
        rvObjectives.setAdapter(objectiveAdapter);

        objectiveAdapter.setOnObjectiveClickListener(position -> {
            if (position >= 0 && position < objectiveList.size()) {
                Objective obj = objectiveList.get(position);
                Intent intent = new Intent(this, ObjectiveDetailActivity.class);
                intent.putExtra(ObjectiveDetailActivity.EXTRA_OBJECTIVE_ID, (long) obj.getId());
                intent.putExtra(ObjectiveDetailActivity.EXTRA_OBJECTIVE_TITLE, obj.getTitle());
                startActivity(intent);
            }
        });

        btnAddObjective.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddObjectiveActivity.class);
            addOrEditObjectiveLauncher.launch(intent);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharge toujours depuis la DB → liste toujours à jour
        if (moduleId != -1) {
            objectiveList.clear();
            objectiveList.addAll(repository.getObjectivesDetailByModule(moduleId));
            objectiveAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onEditClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            Objective obj = objectiveList.get(position);
            Intent intent = new Intent(this, AddObjectiveActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("edit_objective_id", (long) obj.getId()); // ID en DB
            intent.putExtra(KEY_TITLE, obj.getTitle());
            intent.putExtra(KEY_DUE_DATE, obj.getDueDate());
            addOrEditObjectiveLauncher.launch(intent);
        }
    }

    @Override
    public void onDeleteClick(int position) {
        if (position != RecyclerView.NO_POSITION) {
            Objective obj = objectiveList.get(position);
            repository.deleteObjective(obj.getId());
            objectiveList.remove(position);
            objectiveAdapter.notifyItemRemoved(position);
            Toast.makeText(this, "Objectif supprimé", Toast.LENGTH_SHORT).show();
        }
    }
}