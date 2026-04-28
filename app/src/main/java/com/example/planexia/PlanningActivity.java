package com.example.planexia;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlanningActivity extends AppCompatActivity {

    private Button btnJour;
    private Button btnSemaine;
    private RecyclerView recyclerPlanning;
    private BottomNavigationView bottomNav;

    private PlanningTaskAdapter planningTaskAdapter;
    private PlanexiaRepository repository;
    private SessionManager session;

    private boolean isJourMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        repository = new PlanexiaRepository(this);
        session = new SessionManager(this);

        // Utilisateur de test si aucune session active
        if (!session.isLoggedIn()) {
            long userId = repository.createUser("test@planexia.com", "test", "Test", "Informatique", "L1");
            if (userId == -1) {
                userId = repository.login("test@planexia.com", "test");
            }
            session.saveUserId(userId);
            seedTestData(userId);
        }

        initViews();
        setupToggle();
        setupRecyclerView();
        setupBottomNav();
        loadJourMode();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    private void initViews() {
        btnJour          = findViewById(R.id.btnJour);
        btnSemaine       = findViewById(R.id.btnSemaine);
        recyclerPlanning = findViewById(R.id.recyclerPlanning);
        bottomNav        = findViewById(R.id.bottomNavigationView);

        Button btnPremium = findViewById(R.id.btnDebloquerPremium);
        Button btnIA      = findViewById(R.id.btnDecouvrirIA);

        btnPremium.setOnClickListener(v -> onPremiumClicked());
        btnIA.setOnClickListener(v -> onIAClicked());
    }

    private void setupToggle() {
        btnJour.setOnClickListener(v -> {
            if (!isJourMode) {
                isJourMode = true;
                updateToggleUI();
                loadJourMode();
            }
        });

        btnSemaine.setOnClickListener(v -> {
            if (isJourMode) {
                isJourMode = false;
                updateToggleUI();
                loadSemaineMode();
            }
        });
    }

    private void setupRecyclerView() {
        planningTaskAdapter = new PlanningTaskAdapter(new ArrayList<>(), (task, position) -> {
            // TODO : ouvrir le détail de la tâche
        });
        recyclerPlanning.setLayoutManager(new LinearLayoutManager(this));
        recyclerPlanning.setAdapter(planningTaskAdapter);
        recyclerPlanning.setNestedScrollingEnabled(false);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_planning);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_matieres) {
                startActivity(new android.content.Intent(this, com.example.planexia.ui.modules.ModulesActivity.class));
                finish();
            } else if (id == R.id.nav_taches) {
                startActivity(new android.content.Intent(this, com.example.planexia.ui.tasks.TasksActivity.class));
                finish();
            } else if (id == R.id.nav_progression) {
                startActivity(new android.content.Intent(this, com.example.planexia.ui.progression.ProgressionActivity.class));
                finish();
            } else if (id == R.id.nav_profil) {
            startActivity(new android.content.Intent(this, com.example.planexia.ProfileActivity.class));
            finish();
        }
            return true;
        });
    }

    // =========================================================================
    // Chargement des données
    // =========================================================================

    private void loadJourMode() {
        String today = getTodayString();
        List<Task> tasks = repository.getTasksForDateWithModule(session.getUserId(), today);
        planningTaskAdapter.updateTasks(toPlanning(tasks, today));
    }

    private void loadSemaineMode() {
        String today = getTodayString();
        List<PlanningTaskAdapter.PlanningTask> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            String date = getDateString(i);
            List<Task> tasks = repository.getTasksForDateWithModule(session.getUserId(), date);
            result.addAll(toPlanning(tasks, today));
        }

        planningTaskAdapter.updateTasks(result);
    }

    // =========================================================================
    // Conversion Task → PlanningTask
    // =========================================================================

    private List<PlanningTaskAdapter.PlanningTask> toPlanning(List<Task> tasks, String todayStr) {
        List<PlanningTaskAdapter.PlanningTask> result = new ArrayList<>();
        String lastDate = null;

        for (Task t : tasks) {
            String dueDate = t.getDueDate();
            boolean isFirstOfGroup = !dueDate.equals(lastDate);
            String label = isFirstOfGroup ? formatDisplayDate(calendarFromString(dueDate)) : null;
            boolean isToday = dueDate.equals(todayStr);

            result.add(new PlanningTaskAdapter.PlanningTask(
                    t.getTitle(),
                    t.getModuleName() != null ? t.getModuleName() : "",
                    0,
                    label,
                    isToday && isFirstOfGroup,
                    t.isDone()
            ));
            lastDate = dueDate;
        }

        return result;
    }

    // =========================================================================
    // Seed données de test (temporaire)
    // =========================================================================

    private void seedTestData(long userId) {
        long modMaths  = repository.addModule(userId, "Mathématiques", 3, "#5B2EE8");
        long modPhysiq = repository.addModule(userId, "Physique",      2, "#4CAF7D");

        long objMaths  = repository.addObjective(modMaths,  "Maîtriser les dérivées", getDateString(7));
        long objPhysiq = repository.addObjective(modPhysiq, "Préparer le TP mécanique", getDateString(7));

        String today    = getTodayString();
        String tomorrow = getDateString(1);
        String j2       = getDateString(2);

        repository.addTask(objMaths,  "Réviser les dérivées",   today,    null);
        repository.addTask(objPhysiq, "TD mécanique",           today,    null);
        repository.addTask(objMaths,  "Exercices intégrales",   tomorrow, null);
        repository.addTask(objPhysiq, "Rédiger rapport TP",     j2,       null);
    }

    // =========================================================================
    // Utilitaires de date
    // =========================================================================

    private String getTodayString() {
        return getDateString(0);
    }

    private String getDateString(int daysFromNow) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return sdf.format(cal.getTime());
    }

    private Calendar calendarFromString(String dateYYYYMMDD) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateYYYYMMDD));
            return cal;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }

    private String formatDisplayDate(Calendar cal) {
        String[] jours = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        String jourSemaine = jours[cal.get(Calendar.DAY_OF_WEEK) - 1];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return jourSemaine + " " + sdf.format(cal.getTime());
    }

    // =========================================================================
    // Mise à jour du toggle UI
    // =========================================================================

    private void updateToggleUI() {
        if (isJourMode) {
            btnJour.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnJour.setTextColor(getColor(R.color.purple_primary));
            btnSemaine.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnSemaine.setTextColor(android.graphics.Color.parseColor("#888888"));
        } else {
            btnSemaine.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnSemaine.setTextColor(getColor(R.color.purple_primary));
            btnJour.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnJour.setTextColor(android.graphics.Color.parseColor("#888888"));
        }
    }

    // =========================================================================
    // Actions bannières
    // =========================================================================

    private void onPremiumClicked() {
        // TODO : ouvrir l'écran d'achat Premium
    }

    private void onIAClicked() {
        // TODO : lancer la génération IA du planning
    }
}