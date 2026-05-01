package com.example.planexia;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
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
    public static final String CHANNEL_CHRONO_RUNNING = "channel_chrono_running"; // silencieux
    public static final String CHANNEL_CHRONO_DONE    = "channel_chrono_done";    // avec son
    public static final int    NOTIF_RUNNING_ID = 3001;
    public static final int    NOTIF_DONE_ID    = 3002;

    // ── Broadcasts ────────────────────────────────────────────────
    public static final String ACTION_TICK         = "com.example.planexia.CHRONO_TICK";
    public static final String ACTION_GOAL_REACHED = "com.example.planexia.CHRONO_GOAL_REACHED";
    public static final String EXTRA_ELAPSED       = "elapsed_ms";

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

    private int    goalMinutes = 0;
    private long   goalMs      = 0L;
    private String taskLabel   = "";

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
        startForeground(NOTIF_RUNNING_ID, buildRunningNotification("00:00"));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    // ═════════════════════════════════════════════════════════════
    //  API publique
    // ═════════════════════════════════════════════════════════════
    public void startTimer(int goalMin, String label) {
        this.goalMinutes = goalMin;
        this.goalMs      = (long) goalMin * 60 * 1000;
        this.taskLabel   = label;
        this.goalReached = false;
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
        stopForeground(true);
        stopSelf();
    }

    public long    getElapsed()      { return elapsedTime; }
    public boolean isRunning()       { return isRunning; }
    public boolean isGoalReached()   { return goalReached; }
    public int     getGoalMinutes()  { return goalMinutes; }

    // ═════════════════════════════════════════════════════════════
    //  Timer interne
    // ═════════════════════════════════════════════════════════════
    private void startTicking() {
        tickRunnable = new Runnable() {
            @Override public void run() {
                if (!isRunning) return;

                elapsedTime = System.currentTimeMillis() - startTime;
                updateRunningNotification(formatTime(elapsedTime));

                // Broadcast tick → Activity si elle est ouverte
                Intent tick = new Intent(ACTION_TICK);
                tick.putExtra(EXTRA_ELAPSED, elapsedTime);
                sendBroadcast(tick);

                // Objectif atteint ?
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

    /**
     * Appelé quand l'objectif est atteint — que l'Activity soit ouverte ou pas.
     * 1. Joue le son + vibration directement depuis le service
     * 2. Affiche la notification "Objectif atteint" avec son
     * 3. Envoie le broadcast à l'Activity (si elle est ouverte)
     */
    private void onGoalReachedInService() {
        // ── 1. Son + vibration depuis le service ──────────────────
        playAlarmInService();

        // ── 2. Notification "Objectif atteint" avec son ───────────
        showDoneNotification();

        // ── 3. Broadcast → Activity ───────────────────────────────
        sendBroadcast(new Intent(ACTION_GOAL_REACHED));

        // ── 4. Arrêter le foreground ──────────────────────────────
        stopForeground(true);
        // On ne stopSelf() pas immédiatement pour que l'Activity puisse
        // encore interroger isGoalReached() après onResume
        // Le service se fermera quand l'Activity appellera stopTimer()
    }

    // ═════════════════════════════════════════════════════════════
    //  Son + vibration dans le service
    // ═════════════════════════════════════════════════════════════
    private void playAlarmInService() {
        // Vibration
        try {
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                long[] pattern = {0, 400, 200, 400, 200, 800};
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vib.vibrate(pattern, -1);
                }
            }
        } catch (Exception ignored) {}

        // Son : ToneGenerator sur STREAM_ALARM (fonctionne même en arrière-plan)
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500);
            handler.postDelayed(() -> tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500), 700);
            handler.postDelayed(() -> {
                tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1000);
                handler.postDelayed(tg::release, 1200);
            }, 1400);
        } catch (Exception ignored) {}
    }

    // ═════════════════════════════════════════════════════════════
    //  Notifications
    // ═════════════════════════════════════════════════════════════
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // Canal 1 : chrono en cours — silencieux
            NotificationChannel running = new NotificationChannel(
                    CHANNEL_CHRONO_RUNNING,
                    "Chrono en cours",
                    NotificationManager.IMPORTANCE_LOW);
            running.setDescription("Affiche le temps de la session d'étude");
            running.setShowBadge(false);
            running.setSound(null, null);
            nm.createNotificationChannel(running);

            // Canal 2 : objectif atteint — avec son + vibration
            NotificationChannel done = new NotificationChannel(
                    CHANNEL_CHRONO_DONE,
                    "Objectif atteint",
                    NotificationManager.IMPORTANCE_HIGH);
            done.setDescription("Alerte quand l'objectif de travail est atteint");
            done.enableVibration(true);
            done.setVibrationPattern(new long[]{0, 400, 200, 400, 200, 800});
            // Son d'alarme système
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null)
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            done.setSound(alarmSound, new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
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
        PendingIntent pendingOpen = PendingIntent.getActivity(
                this, 1, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String label = taskLabel.isEmpty() ? "Session d'étude" : taskLabel;

        // Son d'alarme pour la notification
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmSound == null)
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_CHRONO_DONE)
                .setSmallIcon(R.drawable.ic_progression)
                .setContentTitle("🎉 Objectif atteint !")
                .setContentText(label + " · " + goalMinutes + " min terminées — Tap pour voir")
                .setContentIntent(pendingOpen)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(alarmSound)
                .setVibrate(new long[]{0, 400, 200, 400, 200, 800})
                .build();

        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.cancel(NOTIF_RUNNING_ID); // retirer la notif persistante
            nm.notify(NOTIF_DONE_ID, notif);
        }
    }

    // ── Format ────────────────────────────────────────────────────
    private String formatTime(long ms) {
        long s = ms / 1000, h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, sec);
        return String.format(Locale.getDefault(), "%02d:%02d", m, sec);
    }
}