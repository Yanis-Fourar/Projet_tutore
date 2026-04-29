package com.example.planexia.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.planexia.R;

import java.util.Calendar;

public class NotificationHelper {

    public static final String CHANNEL_TASK_REMINDER  = "channel_task_reminder";
    public static final String CHANNEL_DEADLINE_ALERT = "channel_deadline_alert";
    public static final String CHANNEL_PROGRESS       = "channel_progress";

    public static final int ALARM_TASK_REMINDER   = 1001;
    public static final int ALARM_DEADLINE_DAILY  = 1002;
    public static final int ALARM_PROGRESS_WEEKLY = 1003;

    public static final int NOTIF_TASK_REMINDER  = 2001;
    public static final int NOTIF_DEADLINE_ALERT = 2002;
    public static final int NOTIF_PROGRESS       = 2003;

    /** Appeler dans MainActivity.onCreate() */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_TASK_REMINDER, "Rappels de tâches", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_DEADLINE_ALERT, "Alertes deadlines", NotificationManager.IMPORTANCE_HIGH));
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_PROGRESS, "Progression hebdomadaire", NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    // ── AlarmManager ──

    public static void scheduleTaskReminderDaily(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1);
        scheduleRepeating(context, ALARM_TASK_REMINDER,
                TaskReminderReceiver.TYPE_TASK_REMINDER,
                cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY);
    }

    public static void scheduleDeadlineAlertDaily(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis())
            cal.add(Calendar.DAY_OF_YEAR, 1);
        scheduleRepeating(context, ALARM_DEADLINE_DAILY,
                TaskReminderReceiver.TYPE_DEADLINE_ALERT,
                cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY);
    }

    public static void scheduleWeeklyProgress(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        if (cal.getTimeInMillis() <= System.currentTimeMillis())
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        scheduleRepeating(context, ALARM_PROGRESS_WEEKLY,
                TaskReminderReceiver.TYPE_PROGRESS_UPDATE,
                cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7);
    }

    private static void scheduleRepeating(Context context, int alarmId,
                                          String type, long triggerAt, long interval) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra(TaskReminderReceiver.EXTRA_TYPE, type);
        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAt, interval, pi);
    }

    public static void cancelAlarm(Context context, int alarmId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) am.cancel(pi);
    }

    // ── Envoi notifications ──

    public static void sendTaskReminderNotification(Context context, String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TASK_REMINDER)
                .setSmallIcon(R.drawable.ic_taches)           // ✅ ic_taches
                .setContentTitle("⏰ Rappel de tâche")
                .setContentText("\"" + taskTitle + "\" arrive à échéance bientôt !")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notify(context, NOTIF_TASK_REMINDER, builder);
    }

    public static void sendDeadlineAlertNotification(Context context, int taskCount) {
        String text = taskCount == 0
                ? "Aucune tâche due aujourd'hui 🎉"
                : taskCount + " tâche(s) à compléter aujourd'hui !";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DEADLINE_ALERT)
                .setSmallIcon(R.drawable.ic_taches)           // ✅ ic_taches
                .setContentTitle("📅 Deadlines du jour")
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notify(context, NOTIF_DEADLINE_ALERT, builder);
    }

    public static void sendProgressUpdateNotification(Context context, int percent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_PROGRESS)
                .setSmallIcon(R.drawable.ic_progression)      // ✅ ic_progression
                .setContentTitle("📈 Résumé de ta semaine")
                .setContentText("Ta progression globale : " + percent + "%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        notify(context, NOTIF_PROGRESS, builder);
    }

    public static void sendWelcomeNotification(Context context, String pseudo) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TASK_REMINDER)
                .setSmallIcon(R.drawable.ic_taches)
                .setContentTitle("🎉 Bienvenue sur Planexia !")
                .setContentText("Salut " + pseudo + " ! Commence par ajouter tes matières.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        notify(context, 3000, builder);
    }

    private static void notify(Context context, int id, NotificationCompat.Builder builder) {
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException ignored) {}
    }
}