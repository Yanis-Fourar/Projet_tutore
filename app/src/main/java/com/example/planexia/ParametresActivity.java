package com.example.planexia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Module;

import java.util.List;

/**
 * Page Paramètres :
 *   - Mode sombre (switch, persistant et appliqué à toute l'app)
 *   - Son (switch on/off, met le système en mode silencieux)
 *   - Effacer mes données (vide les matières/objectifs/tâches, garde le compte)
 *   - Supprimer mon compte (action définitive)
 */
public class ParametresActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private SharedPreferences prefs;
    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        sessionManager = new SessionManager(this);
        prefs = getSharedPreferences(PlanexiaApp.PREFS_PARAM, MODE_PRIVATE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        setupBack();
        setupDarkMode();
        setupSon();
        setupClearData();
        setupDeleteAccount();
    }

    // ========== HEADER ==========
    private void setupBack() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    // ========== MODE SOMBRE ==========
    private void setupDarkMode() {
        SwitchCompat swDark = findViewById(R.id.swDarkMode);
        boolean isDark = prefs.getBoolean(PlanexiaApp.KEY_DARK_MODE, false);
        swDark.setChecked(isDark);

        swDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PlanexiaApp.KEY_DARK_MODE, isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // ========== SON ==========
    private void setupSon() {
        SwitchCompat swSon = findViewById(R.id.swSon);
        boolean enabled = prefs.getBoolean(PlanexiaApp.KEY_SON, true);
        swSon.setChecked(enabled);

        swSon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PlanexiaApp.KEY_SON, isChecked).apply();
            applySonSetting(isChecked);
            String msg = isChecked ? "Son activé" : "Son coupé";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Coupe ou rétablit le son du média (pas le son système).
     * Mute = volume à 0, unmute = volume à la moitié du max.
     */
    private void applySonSetting(boolean enabled) {
        if (audioManager == null) return;
        try {
            int streamType = AudioManager.STREAM_MUSIC;
            if (enabled) {
                int half = audioManager.getStreamMaxVolume(streamType) / 2;
                audioManager.setStreamVolume(streamType, half, 0);
            } else {
                audioManager.setStreamVolume(streamType, 0, 0);
            }
        } catch (SecurityException ignored) {
            // Sur certaines versions Android, modifier le volume requiert
            // une permission spéciale. Dans ce cas on garde juste la pref
            // et l'app peut consulter prefs.getBoolean(KEY_SON,...) avant
            // chaque playback pour décider de jouer ou non.
        }
    }

    // ========== EFFACER MES DONNÉES ==========
    private void setupClearData() {
        LinearLayout rowClear = findViewById(R.id.rowClearData);
        rowClear.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Effacer mes données")
                    .setMessage("Toutes tes matières, objectifs et tâches seront " +
                            "supprimés. Ton compte sera conservé. Continuer ?")
                    .setPositiveButton("Effacer", (dialog, which) -> clearUserData())
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void clearUserData() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Aucun compte connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        PlanexiaRepository repo = new PlanexiaRepository(this);
        // Récupère tous les modules de l'user et les supprime un par un.
        // Le CASCADE de la BDD se charge des objectifs et tâches associés.
        List<Module> modules = repo.getModulesByUser(userId);
        int count = 0;
        for (Module m : modules) {
            if (repo.deleteModule(m.getId()) > 0) count++;
        }
        Toast.makeText(this,
                count + " matière(s) supprimée(s)",
                Toast.LENGTH_SHORT).show();
    }

    // ========== SUPPRESSION DU COMPTE ==========
    private void setupDeleteAccount() {
        LinearLayout rowDelete = findViewById(R.id.rowDeleteAccount);
        rowDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Supprimer mon compte")
                    .setMessage("Cette action est définitive. Toutes tes données " +
                            "seront perdues et tu seras déconnecté. Continuer ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    private void deleteAccount() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "Aucun compte connecté", Toast.LENGTH_SHORT).show();
            return;
        }

        PlanexiaRepository repo = new PlanexiaRepository(this);
        boolean ok = repo.deleteUser(userId);

        if (ok) {
            sessionManager.clear();
            Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erreur lors de la suppression",
                    Toast.LENGTH_SHORT).show();
        }
    }
}