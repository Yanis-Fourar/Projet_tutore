package com.example.planexia;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.ui.modules.ModulesActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChronoActivity extends AppCompatActivity {

    // ── Service ────────────────────────────────────────────────────
    private ChronoService chronoService;
    private boolean       serviceBound = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            chronoService = ((ChronoService.ChronoBinder) binder).getService();
            serviceBound  = true;
            // Si le service était déjà en train de tourner (retour sur l'écran),
            // on synchronise l'affichage avec l'état actuel
            syncFromService();
        }
        @Override public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            chronoService = null;
        }
    };

    // ── BroadcastReceiver — tick toutes les secondes ───────────────
    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context ctx, Intent intent) {
            if (ChronoService.ACTION_TICK.equals(intent.getAction())) {
                long elapsed = intent.getLongExtra(ChronoService.EXTRA_ELAPSED, 0);
                tvChronoTime.setText(formatTime(elapsed));
                updateGoalDisplay(elapsed);
            } else if (ChronoService.ACTION_GOAL_REACHED.equals(intent.getAction())) {
                onGoalReached();
            }
        }
    };

    // ── Données courantes ──────────────────────────────────────────
    private int    goalMinutes       = 0;
    private long   goalMs            = 0L;
    private String currentTaskTitle  = "";
    private String currentModuleName = "";
    private boolean dialogShowing    = false;

    // ── Vues ───────────────────────────────────────────────────────
    private TextView     tvChronoTime;
    private TextView     tvSessionStatus;
    private TextView     tvCurrentTask;
    private TextView     tvCurrentModule;
    private CardView     cardCurrentTask;
    private ImageButton  btnPause;
    private TextView     tvSessionCount;
    private TextView     tvTodayTotal;
    private TextView     tvGoalPercent;
    private LinearLayout historyContainer;

    private PlanexiaRepository repo;
    private long userId;

    // ══════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("planexia_session", MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);
        repo   = new PlanexiaRepository(this);

        boolean isPremium = prefs.getBoolean("is_premium", false);
        if (!isPremium && userId != -1) isPremium = repo.isPremium(userId);
        if (!isPremium) { startActivity(new Intent(this, ProfileActivity.class)); finish(); return; }

        setContentView(R.layout.activity_chrono);
        bindViews();
        setupBottomNav();
        setupClickListeners();

        currentTaskTitle  = getOrEmpty(getIntent().getStringExtra("task_title"));
        currentModuleName = getOrEmpty(getIntent().getStringExtra("task_module"));

        repo.deleteOldChronoSessions(userId);
        loadHistoryFromDb();
        updateTaskCard();

        // Démarrer et se lier au service
        Intent svcIntent = new Intent(this, ChronoService.class);
        startService(svcIntent);
        bindService(svcIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Enregistrer le receiver pour les ticks du service
        IntentFilter filter = new IntentFilter();
        filter.addAction(ChronoService.ACTION_TICK);
        filter.addAction(ChronoService.ACTION_GOAL_REACHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(tickReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(tickReceiver, filter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryFromDb();
        // Si on revient sur l'écran et le service tourne déjà → sync l'UI
        if (serviceBound && chronoService != null) syncFromService();
        // Si le service a atteint l'objectif pendant qu'on était ailleurs → afficher le dialog
        if (serviceBound && chronoService != null && chronoService.isGoalReached() && !dialogShowing) {
            onGoalReached();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try { unregisterReceiver(tickReceiver); } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
        // Si le chrono n'est PAS en train de tourner (objectif atteint ou pas démarré)
        // on stoppe proprement le service pour éviter qu'il reste en mémoire
        if (chronoService != null && !chronoService.isRunning() && !chronoService.isGoalReached()) {
            chronoService.stopTimer();
        }
        chronoService = null;
    }

    // ── Synchroniser l'UI depuis l'état du service ─────────────────
    private void syncFromService() {
        if (chronoService == null) return;
        long elapsed = chronoService.getElapsed();
        goalMinutes  = chronoService.getGoalMinutes();
        goalMs       = (long) goalMinutes * 60 * 1000;

        tvChronoTime.setText(formatTime(elapsed));
        updateGoalDisplay(elapsed);

        if (chronoService.isRunning()) {
            tvSessionStatus.setText("Session en cours");
            btnPause.setImageResource(android.R.drawable.ic_media_pause);
            if (cardCurrentTask != null) cardCurrentTask.setVisibility(View.VISIBLE);
        } else if (elapsed > 0) {
            tvSessionStatus.setText("En pause");
            btnPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            // Service démarré mais pas encore de timer → afficher le dialog
            if (!dialogShowing) showGoalDialog();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Vues & listeners
    // ══════════════════════════════════════════════════════════════
    private void bindViews() {
        tvChronoTime     = findViewById(R.id.tvChronoTime);
        tvSessionStatus  = findViewById(R.id.tvSessionStatus);
        tvCurrentTask    = findViewById(R.id.tvCurrentTask);
        tvCurrentModule  = findViewById(R.id.tvCurrentModule);
        cardCurrentTask  = findViewById(R.id.cardCurrentTask);
        btnPause         = findViewById(R.id.btnPause);
        tvSessionCount   = findViewById(R.id.tvSessionCount);
        tvTodayTotal     = findViewById(R.id.tvTodayTotal);
        tvGoalPercent    = findViewById(R.id.tvGoalPercent);
        historyContainer = findViewById(R.id.historyContainer);
        tvChronoTime.setText("00:00");
        tvSessionStatus.setText("Définissez votre objectif…");
    }

    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        // ⚠️ Le chrono CONTINUE quand on quitte — le service tourne en bg

        btnPause.setOnClickListener(v -> {
            if (chronoService == null) return;
            if (chronoService.isRunning()) {
                chronoService.pauseTimer();
                tvSessionStatus.setText("En pause");
                btnPause.setImageResource(android.R.drawable.ic_media_play);
            } else if (chronoService.getElapsed() > 0) {
                chronoService.resumeTimer();
                tvSessionStatus.setText("Session en cours");
                btnPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        });

        findViewById(R.id.btnStop).setOnClickListener(v -> stopSession());
        findViewById(R.id.btnReset).setOnClickListener(v -> resetChrono());
    }

    // ══════════════════════════════════════════════════════════════
    //  Dialog objectif
    // ══════════════════════════════════════════════════════════════
    private void showGoalDialog() {
        if (dialogShowing) return;
        dialogShowing = true;

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chrono_goal);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int)(getResources().getDisplayMetrics().widthPixels * 0.92f),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCancelable(false);

        EditText etMinutes = dialog.findViewById(R.id.etGoalMinutes);
        dialog.findViewById(R.id.btnQuick25).setOnClickListener(v -> etMinutes.setText("25"));
        dialog.findViewById(R.id.btnQuick45).setOnClickListener(v -> etMinutes.setText("45"));
        dialog.findViewById(R.id.btnQuick60).setOnClickListener(v -> etMinutes.setText("60"));
        dialog.findViewById(R.id.btnQuick90).setOnClickListener(v -> etMinutes.setText("90"));

        dialog.findViewById(R.id.btnGoalCancel).setOnClickListener(v -> {
            dialogShowing = false;
            dialog.dismiss();
            // Si pas de timer actif → stopper le service et quitter
            if (chronoService != null && !chronoService.isRunning() && chronoService.getElapsed() == 0) {
                chronoService.stopTimer();
            }
            finish();
        });

        dialog.findViewById(R.id.btnGoalStart).setOnClickListener(v -> {
            String raw = etMinutes.getText().toString().trim();
            if (TextUtils.isEmpty(raw)) { etMinutes.setError("Entrez un nombre de minutes"); return; }
            int min = Integer.parseInt(raw);
            if (min <= 0 || min > 999) { etMinutes.setError("Entre 1 et 999 minutes"); return; }
            goalMinutes  = min;
            goalMs       = (long) goalMinutes * 60 * 1000;
            dialogShowing = false;
            dialog.dismiss();

            // Lancer le timer dans le service
            String label = currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle;
            if (chronoService != null) chronoService.startTimer(goalMinutes, label);

            tvSessionStatus.setText("Session en cours");
            btnPause.setImageResource(android.R.drawable.ic_media_pause);
            if (cardCurrentTask != null) cardCurrentTask.setVisibility(View.VISIBLE);
            updateGoalDisplay(0);
        });

        dialog.show();
    }

    // ══════════════════════════════════════════════════════════════
    //  Contrôles
    // ══════════════════════════════════════════════════════════════
    private void onGoalReached() {
        dialogShowing = true;
        tvSessionStatus.setText("🎉 Objectif atteint !");
        btnPause.setImageResource(android.R.drawable.ic_media_play);
        playAlarm();

        // Sauvegarder en DB
        String label = currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle;
        repo.addChronoSession(userId, label, goalMs, goalMinutes);
        loadHistoryFromDb();

        new AlertDialog.Builder(this)
                .setTitle("🎉 Objectif atteint !")
                .setMessage("Bravo ! " + goalMinutes + " min terminées.\n\nQue voulez-vous faire ?")
                .setPositiveButton("Même objectif", (d, w) -> {
                    dialogShowing = false;
                    tvChronoTime.setText("00:00");
                    tvSessionStatus.setText("Session en cours");
                    btnPause.setImageResource(android.R.drawable.ic_media_pause);
                    String lbl = currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle;
                    if (chronoService != null) chronoService.startTimer(goalMinutes, lbl);
                })
                .setNegativeButton("Changer l'objectif", (d, w) -> {
                    dialogShowing = false;
                    tvChronoTime.setText("00:00");
                    showGoalDialog();
                })
                .setNeutralButton("Quitter", (d, w) -> { dialogShowing = false; if (chronoService != null) chronoService.stopTimer(); finish(); })
                .setCancelable(false)
                .show();
    }

    private void stopSession() {
        if (chronoService == null) return;
        long elapsed = chronoService.getElapsed();
        if (elapsed == 0) return;

        chronoService.stopTimer();

        String label = currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle;
        repo.addChronoSession(userId, label, elapsed, goalMinutes);
        loadHistoryFromDb();

        tvChronoTime.setText("00:00");
        tvSessionStatus.setText("Session terminée ✓");
        btnPause.setImageResource(android.R.drawable.ic_media_play);

        // Redémarrer le service pour qu'il soit prêt pour la prochaine session
        Intent svcIntent = new Intent(this, ChronoService.class);
        startService(svcIntent);
        bindService(svcIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        new AlertDialog.Builder(this)
                .setTitle("Nouvelle session ?")
                .setMessage("Repartir avec " + goalMinutes + " min ?")
                .setPositiveButton("Même objectif", (d, w) -> {
                    String lbl = currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle;
                    if (chronoService != null) chronoService.startTimer(goalMinutes, lbl);
                    tvSessionStatus.setText("Session en cours");
                    btnPause.setImageResource(android.R.drawable.ic_media_pause);
                })
                .setNegativeButton("Changer l'objectif", (d, w) -> showGoalDialog())
                .setNeutralButton("Quitter", (d, w) -> finish())
                .show();
    }

    private void resetChrono() {
        if (chronoService != null) {
            chronoService.pauseTimer();
            // On stoppe le service et on en repart
            unbindService(serviceConnection);
            serviceBound = false;
            chronoService.stopTimer();
            chronoService = null;
        }
        tvChronoTime.setText("00:00");
        tvSessionStatus.setText("Réinitialisé");
        btnPause.setImageResource(android.R.drawable.ic_media_play);

        Intent svcIntent = new Intent(this, ChronoService.class);
        startService(svcIntent);
        bindService(svcIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // ══════════════════════════════════════════════════════════════
    //  Historique DB
    // ══════════════════════════════════════════════════════════════
    private void loadHistoryFromDb() {
        if (historyContainer == null) return;
        historyContainer.removeAllViews();

        List<String[]> sessions = repo.getChronoSessionsLast24h(userId);

        int  count    = sessions.size();
        long totalMs  = 0;
        for (String[] s : sessions) totalMs += Long.parseLong(s[1]);

        if (tvSessionCount != null) tvSessionCount.setText(String.valueOf(count));
        if (tvTodayTotal   != null) tvTodayTotal.setText(formatDuration(totalMs));

        if (sessions.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (String[] s : sessions) {
            String taskLbl    = s[0];
            long   durationMs = Long.parseLong(s[1]);
            int    goalMin    = Integer.parseInt(s[2]);
            long   createdAt  = Long.parseLong(s[3]);

            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_chrono_history, historyContainer, false);
            ((TextView) row.findViewById(R.id.tvHistoryTask)).setText(taskLbl);
            ((TextView) row.findViewById(R.id.tvHistoryObjective)).setText(sdf.format(new Date(createdAt)));
            ((TextView) row.findViewById(R.id.tvHistoryDuration)).setText(formatDuration(durationMs));
            ((TextView) row.findViewById(R.id.tvHistoryGoal)).setText(goalMin > 0 ? "/ " + goalMin + " min" : "");
            historyContainer.addView(row);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Affichage
    // ══════════════════════════════════════════════════════════════
    private void updateTaskCard() {
        if (tvCurrentTask   != null) tvCurrentTask.setText(currentTaskTitle.isEmpty() ? "Session d'étude" : currentTaskTitle);
        if (tvCurrentModule != null) tvCurrentModule.setText(currentModuleName);
    }

    private void updateGoalDisplay(long elapsed) {
        if (tvGoalPercent == null) return;
        if (goalMs <= 0) { tvGoalPercent.setText("—"); return; }
        int pct = (int) Math.min(100, elapsed * 100 / goalMs);
        tvGoalPercent.setText(pct + "%");
    }

    // ══════════════════════════════════════════════════════════════
    //  Son + vibration (quand l'écran est ouvert)
    // ══════════════════════════════════════════════════════════════
    private final Handler alarmHandler = new Handler(Looper.getMainLooper());
    private void playAlarm() {
        try {
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vib.vibrate(VibrationEffect.createWaveform(new long[]{0,300,200,300,200,600}, -1));
                else
                    vib.vibrate(new long[]{0,300,200,300,200,600}, -1);
            }
        } catch (Exception ignored) {}
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400);
            alarmHandler.postDelayed(() -> tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400), 600);
            alarmHandler.postDelayed(() -> {
                tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 800);
                alarmHandler.postDelayed(tg::release, 1000);
            }, 1200);
        } catch (Exception ignored) {}
    }

    // ══════════════════════════════════════════════════════════════
    //  Utilitaires
    // ══════════════════════════════════════════════════════════════
    private String getOrEmpty(String s) { return s != null ? s : ""; }

    private String formatTime(long ms) {
        long s = ms / 1000, h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, sec);
        return String.format(Locale.getDefault(), "%02d:%02d", m, sec);
    }

    private String formatDuration(long ms) {
        long min = ms / 60000;
        if (min == 0) return "< 1min";
        long h = min / 60, m = min % 60;
        if (h > 0) return String.format(Locale.getDefault(), "%dh %02dmin", h, m);
        return min + "min";
    }

    // ── Navigation ─────────────────────────────────────────────────
    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // ⚠️ On ne stoppe PAS le service — le chrono continue en bg
            if      (id == R.id.nav_matieres)   { startActivity(new Intent(this, ModulesActivity.class)); finish(); }
            else if (id == R.id.nav_taches)      { startActivity(new Intent(this, com.example.planexia.ui.tasks.TasksActivity.class)); finish(); }
            else if (id == R.id.nav_planning)    { startActivity(new Intent(this, PlanningActivity.class)); finish(); }
            else if (id == R.id.nav_progression) { startActivity(new Intent(this, com.example.planexia.ui.progression.ProgressionActivity.class)); finish(); }
            else if (id == R.id.nav_profil)      { startActivity(new Intent(this, ProfileActivity.class)); finish(); }
            return true;
        });
    }
}