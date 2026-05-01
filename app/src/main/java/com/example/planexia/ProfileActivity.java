package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.ui.PremiumDialog;
import com.example.planexia.ui.modules.ModulesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);

        setupBottomNav();
        setupUserInfo();
        setupClickListeners();
    }

    private void setupUserInfo() {
        SessionManager session = new SessionManager(this);
        long userId = session.getUserId();

        PlanexiaRepository repo = new PlanexiaRepository(this);
        String[] info = repo.getUserInfo(userId);

        String pseudo  = (info[0] != null && !info[0].isEmpty()) ? info[0] : "Utilisateur";
        String filiere = (info[1] != null && !info[1].isEmpty()) ? info[1] : "";
        String annee   = (info[2] != null && !info[2].isEmpty()) ? info[2] : "";
        String initial = pseudo.substring(0, 1).toUpperCase();

        ((TextView) findViewById(R.id.tvAvatarInitial)).setText(initial);
        ((TextView) findViewById(R.id.tvUsername)).setText(pseudo);
        ((TextView) findViewById(R.id.tvFiliere)).setText(filiere);
        ((TextView) findViewById(R.id.tvAnnee)).setText(annee);
    }

    private void setupClickListeners() {
        Button btnPasserPremium    = findViewById(R.id.btnPasserPremium);
        Button btnDecouvrirPremium = findViewById(R.id.btnDecouvrirPremium);
        LinearLayout rowParametres    = findViewById(R.id.rowParametres);
        LinearLayout rowNotifications = findViewById(R.id.rowNotifications);
        LinearLayout rowAide          = findViewById(R.id.rowAide);
        LinearLayout rowDeconnexion   = findViewById(R.id.rowDeconnexion);

        // ← MODIFIÉ : brancher le popup Premium
        btnPasserPremium.setOnClickListener(v ->
                PremiumDialog.show(this, () -> {
                    // callback : recharger le profil
                    setupUserInfo();
                })
        );

        btnDecouvrirPremium.setOnClickListener(v ->
                PremiumDialog.show(this, () -> {
                    setupUserInfo();
                })
        );

        rowParametres.setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Paramètres à venir", android.widget.Toast.LENGTH_SHORT).show());

        rowNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.planexia.notifications.NotificationsActivity.class)));

        rowAide.setOnClickListener(v ->
                android.widget.Toast.makeText(this, "Aide & Support à venir", android.widget.Toast.LENGTH_SHORT).show());

        rowDeconnexion.setOnClickListener(v -> confirmerDeconnexion());
    }

    private void confirmerDeconnexion() {
        new AlertDialog.Builder(this)
                .setTitle("Se déconnecter")
                .setMessage("Tu vas être redirigé vers l'écran de connexion.")
                .setPositiveButton("Se déconnecter", (dialog, which) -> {
                    sessionManager.clear();
                    Intent intent = new Intent(this, OnboardingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_profil);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_matieres) {
                startActivity(new Intent(this, ModulesActivity.class));
                finish();
            } else if (id == R.id.nav_taches) {
                startActivity(new Intent(this, com.example.planexia.ui.tasks.TasksActivity.class));
                finish();
            } else if (id == R.id.nav_planning) {
                startActivity(new Intent(this, PlanningActivity.class));
                finish();
            } else if (id == R.id.nav_progression) {
                startActivity(new Intent(this, com.example.planexia.ui.progression.ProgressionActivity.class));
                finish();
            }
            return true;
        });
    }
}