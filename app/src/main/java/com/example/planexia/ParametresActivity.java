package com.example.planexia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Module;

import java.util.List;

public class ParametresActivity extends AppCompatActivity {

    private static final String PREFS_PARAM = "planexia_param";
    private static final String KEY_SON     = "son_enabled";

    private SharedPreferences prefs;
    private SessionManager sessionManager;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        prefs          = getSharedPreferences(PREFS_PARAM, MODE_PRIVATE);
        sessionManager = new SessionManager(this);
        audioManager   = (AudioManager) getSystemService(AUDIO_SERVICE);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupSon();
        setupClearData();
        setupDeleteAccount();
    }

    // ── Son ───────────────────────────────────────────────────────
    private void setupSon() {
        SwitchCompat swSon = findViewById(R.id.swSon);
        swSon.setChecked(prefs.getBoolean(KEY_SON, true));
        swSon.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_SON, isChecked).apply();
            applySon(isChecked);
            Toast.makeText(this,
                    isChecked ? "Son activé" : "Son désactivé",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void applySon(boolean enabled) {
        if (audioManager == null) return;
        try {
            int stream = AudioManager.STREAM_MUSIC;
            audioManager.setStreamVolume(stream,
                    enabled ? audioManager.getStreamMaxVolume(stream) / 2 : 0, 0);
        } catch (SecurityException ignored) {}
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
        // Effacer aussi le statut premium
        getSharedPreferences("planexia_session", MODE_PRIVATE)
                .edit().clear().apply();

        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}