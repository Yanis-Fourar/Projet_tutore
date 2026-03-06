package com.example.planexia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * PlanningActivity — affiche les tâches du jour ou de la semaine.
 * Respecte le contrat technique Planexia (V1).
 *
 * En V1 : données de template (hardcodées).
 * En V2 : remplacer buildTemplateTasks() par des appels au Repository.
 */
public class PlanningActivity extends AppCompatActivity {

    // --- Vues ---
    private Button btnJour;
    private Button btnSemaine;
    private RecyclerView recyclerPlanning;
    private BottomNavigationView bottomNav;

    // --- Adapter ---
    private TaskAdapter taskAdapter;

    // --- État ---
    private boolean isJourMode = true; // true = vue Jour, false = vue Semaine

    // =========================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning);

        initViews();
        setupToggle();
        setupRecyclerView();
        setupBottomNav();

        // Afficher la vue Jour par défaut
        loadJourMode();
    }

    // =========================================================================
    // Initialisation
    // =========================================================================

    private void initViews() {
        btnJour          = findViewById(R.id.btnJour);
        btnSemaine       = findViewById(R.id.btnSemaine);
        recyclerPlanning = findViewById(R.id.recyclerPlanning);
        bottomNav        = findViewById(R.id.bottomNav);

        // Boutons bannières
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
        taskAdapter = new TaskAdapter(new ArrayList<>(), (task, position) -> {
            // TODO V2 : ouvrir ObjectiveDetailActivity avec la tâche sélectionnée
        });
        recyclerPlanning.setLayoutManager(new LinearLayoutManager(this));
        recyclerPlanning.setAdapter(taskAdapter);
        recyclerPlanning.setNestedScrollingEnabled(false);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_planning);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_matieres) {
                // TODO : naviguer vers ModulesActivity
            } else if (id == R.id.nav_taches) {
                // TODO : naviguer vers tâches globales
            } else if (id == R.id.nav_progression) {
                // TODO : naviguer vers progression
            } else if (id == R.id.nav_profil) {
                // TODO : naviguer vers ProfileActivity
            }
            return true;
        });
    }

    // =========================================================================
    // Chargement des données
    // =========================================================================

    /**
     * Charge les tâches du jour uniquement.
     * En V2 : appeler repository.getTasksForDate(userId, today)
     */
    private void loadJourMode() {
        String today = getTodayString(); // YYYY-MM-DD
        List<TaskAdapter.PlanningTask> tasks = buildTemplateTasks(false);
        taskAdapter.updateTasks(tasks);
    }

    /**
     * Charge les tâches de la semaine complète.
     * En V2 : appeler repository sur les 7 prochains jours.
     */
    private void loadSemaineMode() {
        List<TaskAdapter.PlanningTask> tasks = buildTemplateTasks(true);
        taskAdapter.updateTasks(tasks);
    }

    // =========================================================================
    // Données de template (à remplacer par le Repository en V2)
    // =========================================================================

    /**
     * Génère des tâches de démonstration.
     * @param semaine true = toute la semaine, false = jour uniquement
     */
    private List<TaskAdapter.PlanningTask> buildTemplateTasks(boolean semaine) {
        List<TaskAdapter.PlanningTask> list = new ArrayList<>();

        // --- AUJOURD'HUI ---
        String todayLabel = getTodayDisplayLabel(); // ex: "Mercredi 03/12/2025"

        list.add(new TaskAdapter.PlanningTask(
                "Réviser les dérivées",
                "Mathématiques",
                60,
                todayLabel,  // premier item du groupe → affiche le header
                true,        // isToday = true → badge "Aujourd'hui"
                false
        ));

        list.add(new TaskAdapter.PlanningTask(
                "TD mécanique",
                "Physique",
                45,
                null,        // même groupe → pas de header
                false,
                false
        ));

        // --- JOURS SUIVANTS (mode Semaine uniquement) ---
        if (semaine) {
            list.add(new TaskAdapter.PlanningTask(
                    "Projet algorithmes",
                    "Informatique",
                    120,
                    getNextDayLabel(1), // ex: "Jeudi 04/12/2025"
                    false,
                    false
            ));

            list.add(new TaskAdapter.PlanningTask(
                    "Lecture chapitre 5",
                    "Histoire",
                    30,
                    null,
                    false,
                    false
            ));

            list.add(new TaskAdapter.PlanningTask(
                    "Exercices intégrales",
                    "Mathématiques",
                    90,
                    getNextDayLabel(2),
                    false,
                    false
            ));

            list.add(new TaskAdapter.PlanningTask(
                    "Rédiger rapport TP",
                    "Physique",
                    60,
                    getNextDayLabel(3),
                    false,
                    false
            ));
        }

        return list;
    }

    // =========================================================================
    // Utilitaires de date
    // =========================================================================

    /** Retourne la date du jour au format YYYY-MM-DD (pour la BDD). */
    private String getTodayString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(Calendar.getInstance().getTime());
    }

    /** Retourne la date du jour formatée pour l'affichage : "Mercredi 03/12/2025". */
    private String getTodayDisplayLabel() {
        return formatDisplayDate(Calendar.getInstance());
    }

    /** Retourne la date dans N jours formatée pour l'affichage. */
    private String getNextDayLabel(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        return formatDisplayDate(cal);
    }

    /** Formate un Calendar en "Lundi 01/01/2025". */
    private String formatDisplayDate(Calendar cal) {
        // Jour de la semaine en français
        String[] jours = {"Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
        String jourSemaine = jours[cal.get(Calendar.DAY_OF_WEEK) - 1];

        // Date au format DD/MM/YYYY
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return jourSemaine + " " + sdf.format(cal.getTime());
    }

    // =========================================================================
    // Mise à jour du toggle UI
    // =========================================================================

    private void updateToggleUI() {
        if (isJourMode) {
            // Jour = sélectionné
            btnJour.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnJour.setTextColor(getColor(R.color.purple_primary));
            btnSemaine.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            btnSemaine.setTextColor(android.graphics.Color.parseColor("#888888"));
        } else {
            // Semaine = sélectionné
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
        // TODO V2 : ouvrir l'écran d'achat Premium
    }

    private void onIAClicked() {
        // TODO V2 : lancer la génération IA du planning
    }
}