package com.example.planexia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Module;

import java.util.List;

public class ParametresActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        sessionManager = new SessionManager(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupClearData();
        setupDeleteAccount();
    }

    // ── Effacer données ───────────────────────────────────────────
    private void setupClearData() {
        LinearLayout row = findViewById(R.id.rowClearData);
        row.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Effacer mes données")
                        .setMessage("Toutes tes matières, objectifs et tâches seront supprimés. Ton compte sera conservé.")
                        .setPositiveButton("Effacer", (d, w) -> clearData())
                        .setNegativeButton("Annuler", null)
                        .show()
        );
    }

    private void clearData() {
        long userId = sessionManager.getUserId();
        PlanexiaRepository repo = new PlanexiaRepository(this);
        List<Module> modules = repo.getModulesByUser(userId);
        for (Module m : modules) repo.deleteModule(m.getId());
        Toast.makeText(this, modules.size() + " matière(s) supprimée(s)", Toast.LENGTH_SHORT).show();
    }

    // ── Supprimer compte ──────────────────────────────────────────
    private void setupDeleteAccount() {
        LinearLayout row = findViewById(R.id.rowDeleteAccount);
        row.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Supprimer mon compte")
                        .setMessage("Cette action est définitive. Toutes tes données seront perdues.")
                        .setPositiveButton("Supprimer", (d, w) -> deleteAccount())
                        .setNegativeButton("Annuler", null)
                        .show()
        );
    }

    private void deleteAccount() {
        long userId = sessionManager.getUserId();
        PlanexiaRepository repo = new PlanexiaRepository(this);
        repo.deleteUser(userId);

        sessionManager.clear();
        getSharedPreferences("planexia_session", MODE_PRIVATE)
                .edit().clear().apply();

        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
