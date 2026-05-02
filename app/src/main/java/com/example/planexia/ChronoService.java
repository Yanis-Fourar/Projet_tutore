package com.example.planexia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class ChronoService extends Service {

    // ── Canaux notif ───────────────────────────────────────────────
    public static final String CHANNEL_CHRONO_RUNNING = "channel_chrono_running";
    public static final String CHANNEL_CHRONO_DONE    = "channel_chrono_done";
    public static final int    NOTIF_RUNNING_ID = 3001;
    public static final int    NOTIF_DONE_ID    = 3002;

    // ── Broadcasts ────────────────────────────────────────────────
    public static final String ACTION_TICK         = "com.example.planexia.CHRONO_TICK";
    public static final String ACTION_GOAL_REACHED = "com.example.planexia.CHRONO_GOAL_REACHED";
    public static final String EXTRA_ELAPSED       = "elapsed_ms";

    // ── Intent flags ──────────────────────────────────────────────
    public static final String EXTRA_SHOW_GOAL_DIALOG = "show_goal_dialog";
    public static final String ACTION_STOP_ALARM      = "com.example.planexia.STOP_ALARM";

    // ── Binder ────────────────────────────────────────────────────
    public class ChronoBinder extends Binder {
        public ChronoService getService() { return ChronoService.this; }
    }
    private final IBinder binder = new ChronoBinder();

    // ── État ──────────────────────────────────────────────────────
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;

    private long    startTime   = 0L;
    private long    elapsedTime = 0L;
    private boolean isRunning   = false;
    private boolean goalReached = false;

    // ── Alarme contrôlable ────────────────────────────────────────
    private Ringtone alarmRingtone  = null;
    private Vibrator activeVibrator = null;
    private boolean  alarmStarted   = false;  // évite de jouer le son deux fois

    private int    goalMinutes = 0;
    private long   goalMs      = 0L;
    private String taskLabel   = "";  // ← conservé même quand l'Activity est détruite

    // ═════════════════════════════════════════════════════════════
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    @Override
    public IBinder onBind(Intent intent) { return binder; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarm();
            Intent open = new Intent(this, ChronoActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            open.putExtra(EXTRA_SHOW_GOAL_DIALOG, true);
            startActivity(open);
            return START_NOT_STICKY;
        }
        startForeground(NOTIF_RUNNING_ID, buildRunningNotification("00:00"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        stopAlarm();
    }

    // ═════════════════════════════════════════════════════════════
    //  API publique
    // ═════════════════════════════════════════════════════════════
    public void startTimer(int goalMin, String label) {
        this.goalMinutes = goalMin;
        this.goalMs      = (long) goalMin * 60 * 1000;
        this.taskLabel   = label;
        this.goalReached = false;
        this.alarmStarted = false;  // réinitialiser pour la nouvelle session
        this.elapsedTime = 0L;
        this.startTime   = System.currentTimeMillis();
        this.isRunning   = true;
        startTicking();
    }

    public void pauseTimer() {
        if (!isRunning) return;
        isRunning = false;
        handler.removeCallbacks(tickRunnable);
        updateRunningNotification("⏸ " + formatTime(elapsedTime));
    }

    public void resumeTimer() {
        if (isRunning) return;
        isRunning  = true;
        startTime  = System.currentTimeMillis() - elapsedTime;
        startTicking();
    }

    public void stopTimer() {
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        stopAlarm();
        stopForeground(true);
        stopSelf();
    }

    public long    getElapsed()      { return elapsedTime; }
    public boolean isRunning()       { return isRunning; }
    public boolean isGoalReached()   { return goalReached; }
    public int     getGoalMinutes()  { return goalMinutes; }
    /** Retourne le label de la tâche en cours — utilisé par l'Activity au retour */
    public String  getTaskLabel()    { return taskLabel; }

    /**
     * Arrête le son en boucle + vibration + notification "Objectif atteint".
     * Appelé uniquement quand l'utilisateur appuie sur un bouton du dialog.
     */
    public void stopAlarm() {
        alarmStarted = false;  // permettre de rejouer pour la prochaine session
        try {
            if (alarmRingtone != null && alarmRingtone.isPlaying()) {
                alarmRingtone.stop();
            }
            alarmRingtone = null;
        } catch (Exception ignored) {}

        try {
            if (activeVibrator != null) {
                activeVibrator.cancel();
            }
            activeVibrator = null;
        } catch (Exception ignored) {}

        try {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.cancel(NOTIF_DONE_ID);
        } catch (Exception ignored) {}
    }

    // ═════════════════════════════════════════════════════════════
    //  Timer interne
    // ═════════════════════════════════════════════════════════════
    private void startTicking() {
        tickRunnable = new Runnable() {
            @Override public void run() {
                if (!isRunning) return;

                elapsedTime = System.currentTimeMillis() - startTime;
                updateRunningNotification(formatTime(elapsedTime));

                Intent tick = new Intent(ACTION_TICK);
                tick.putExtra(EXTRA_ELAPSED, elapsedTime);
                sendBroadcast(tick);

                if (goalMs > 0 && elapsedTime >= goalMs && !goalReached) {
                    goalReached = true;
                    isRunning   = false;
                    onGoalReachedInService();
                    return;
                }

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(tickRunnable);
    }

    private void onGoalReachedInService() {
        // 1. Son en boucle + vibration (dans l'app ET hors app)
        playAlarmLooping();
        // 2. Notification persistante avec bouton "Arrêter"
        showDoneNotification();
        // 3. Broadcast → Activity si elle est ouverte en premier plan
        sendBroadcast(new Intent(ACTION_GOAL_REACHED));
        // 4. Retirer la notif "chrono en cours"
        stopForeground(true);
    }

    // ═════════════════════════════════════════════════════════════
    //  Son en BOUCLE (fonctionne dans l'app ET hors app)
    // ═════════════════════════════════════════════════════════════
    private void playAlarmLooping() {
        // Sécurité : ne pas jouer le son deux fois si déjà en cours
        if (alarmStarted) return;
        alarmStarted = true;
        // Vibration en boucle
        try {
            activeVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (activeVibrator != null && activeVibrator.hasVibrator()) {
                long[] pattern = {0, 400, 200, 400, 200, 800, 600};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activeVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    activeVibrator.vibrate(pattern, 0);
                }
            }
        } catch (Exception ignored) {}

        // Ringtone d'alarme système en boucle
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null)
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            alarmRingtone = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);
            if (alarmRingtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    alarmRingtone.setLooping(true); // boucle jusqu'à stopAlarm()
                }
                alarmRingtone.play();
            }
        } catch (Exception ignored) {}
    }

    // ═════════════════════════════════════════════════════════════
    //  Notifications
    // ═════════════════════════════════════════════════════════════
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            NotificationChannel running = new NotificationChannel(
                    CHANNEL_CHRONO_RUNNING, "Chrono en cours",
                    NotificationManager.IMPORTANCE_LOW);
            running.setDescription("Affiche le temps de la session d'étude");
            running.setShowBadge(false);
            running.setSound(null, null);
            nm.createNotificationChannel(running);

            // Canal sans son propre — le son est géré par playAlarmLooping()
            NotificationChannel done = new NotificationChannel(
                    CHANNEL_CHRONO_DONE, "Objectif atteint",
                    NotificationManager.IMPORTANCE_HIGH);
            done.setDescription("Alerte quand l'objectif de travail est atteint");
            done.setSound(null, null);
            done.enableVibration(false);
            nm.createNotificationChannel(done);
        }
    }

    private Notification buildRunningNotification(String timeText) {
        Intent openIntent = new Intent(this, ChronoActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String subtitle = taskLabel.isEmpty() ? "Session d'étude" : taskLabel;
        if (goalMinutes > 0) subtitle += " · objectif " + goalMinutes + " min";

        return new NotificationCompat.Builder(this, CHANNEL_CHRONO_RUNNING)
                .setSmallIcon(R.drawable.ic_progression)
                .setContentTitle("⏱ " + timeText)
                .setContentText(subtitle)
                .setContentIntent(pendingOpen)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateRunningNotification(String timeText) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(NOTIF_RUNNING_ID, buildRunningNotification(timeText));
    }

    private void showDoneNotification() {
        Intent openIntent = new Intent(this, ChronoActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.putExtra(EXTRA_SHOW_GOAL_DIALOG, true);
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 1, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent stopIntent = new Intent(this, ChronoService.class);
        stopIntent.setAction(ACTION_STOP_ALARM);
        PendingIntent pendingStop = PendingIntent.getService(
                this, 2, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = taskLabel.isEmpty() ? "Session d'étude" : taskLabel;

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_CHRONO_DONE)
                .setSmallIcon(R.drawable.ic_progression)
                .setContentTitle("🎉 Objectif atteint !")
                .setContentText(label + " · " + goalMinutes + " min terminées — Appuie pour choisir")
                .setContentIntent(pendingOpen)
                .addAction(android.R.drawable.ic_delete, "Arrêter l'alarme", pendingStop)
                .setAutoCancel(false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.cancel(NOTIF_RUNNING_ID);
            nm.notify(NOTIF_DONE_ID, notif);
        }
    }

    private String formatTime(long ms) {
        long s = ms / 1000, h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, sec);
        return String.format(Locale.getDefault(), "%02d:%02d", m, sec);
    }
}