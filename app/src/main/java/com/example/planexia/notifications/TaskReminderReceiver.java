package com.example.planexia.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.planexia.data.PlanexiaDatabaseHelper;
import com.example.planexia.data.SessionManager;

public class TaskReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_TYPE          = "notif_type";
    public static final String TYPE_TASK_REMINDER  = "task_reminder";
    public static final String TYPE_DEADLINE_ALERT = "deadline_alert";
    public static final String TYPE_PROGRESS_UPDATE= "progress_update";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // Après reboot → replanifier
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            rescheduleOnBoot(context);
            return;
        }

        // Vérifier master switch
        SharedPreferences prefs = context.getSharedPreferences(
                NotificationsActivity.PREFS_NOTIF, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(NotificationsActivity.KEY_ALL_NOTIFS, true)) return;

        String type = intent.getStringExtra(EXTRA_TYPE);
        if (type == null) return;

        switch (type) {
            case TYPE_TASK_REMINDER:
                if (prefs.getBoolean(NotificationsActivity.KEY_TASK_REMINDERS, true)) {
                    String task = getNextDueTask(context);
                    NotificationHelper.sendTaskReminderNotification(context,
                            task != null ? task : "Tu as des tâches à compléter !");
                }
                break;
            case TYPE_DEADLINE_ALERT:
                if (prefs.getBoolean(NotificationsActivity.KEY_DEADLINE_ALERTS, true)) {
                    NotificationHelper.sendDeadlineAlertNotification(context, countTasksDueToday(context));
                }
                break;
            case TYPE_PROGRESS_UPDATE:
                if (prefs.getBoolean(NotificationsActivity.KEY_PROGRESS_UPDATES, false)) {
                    NotificationHelper.sendProgressUpdateNotification(context, getGlobalProgress(context));
                }
                break;
        }
    }

    private String getNextDueTask(Context context) {
        SessionManager session = new SessionManager(context);
        long userId = session.getUserId();
        if (userId == -1) return null;
        String today = "";
        String inTwoDays = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            today = java.time.LocalDate.now().toString();
            inTwoDays = java.time.LocalDate.now().plusDays(2).toString();
        }
        try {
            SQLiteDatabase db = new PlanexiaDatabaseHelper(context).getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT t.title FROM tasks t " +
                            "JOIN objectives o ON t.objective_id = o.id " +
                            "JOIN modules m ON o.module_id = m.id " +
                            "WHERE m.user_id = ? AND t.is_done = 0 " +
                            "AND t.due_date >= ? AND t.due_date <= ? " +
                            "ORDER BY t.due_date ASC LIMIT 1",
                    new String[]{String.valueOf(userId), today, inTwoDays});
            String title = null;
            if (c.moveToFirst()) title = c.getString(0);
            c.close();
            return title;
        } catch (Exception e) { return null; }
    }

    private int countTasksDueToday(Context context) {
        SessionManager session = new SessionManager(context);
        long userId = session.getUserId();
        if (userId == -1) return 0;
        String today = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            today = java.time.LocalDate.now().toString();
        }
        try {
            SQLiteDatabase db = new PlanexiaDatabaseHelper(context).getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM tasks t " +
                            "JOIN objectives o ON t.objective_id = o.id " +
                            "JOIN modules m ON o.module_id = m.id " +
                            "WHERE m.user_id = ? AND t.is_done = 0 AND t.due_date = ?",
                    new String[]{String.valueOf(userId), today});
            int count = 0;
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
            return count;
        } catch (Exception e) { return 0; }
    }

    private int getGlobalProgress(Context context) {
        SessionManager session = new SessionManager(context);
        long userId = session.getUserId();
        if (userId == -1) return 0;
        try {
            SQLiteDatabase db = new PlanexiaDatabaseHelper(context).getReadableDatabase();
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*), SUM(CASE WHEN t.is_done=1 THEN 1 ELSE 0 END) " +
                            "FROM tasks t JOIN objectives o ON t.objective_id=o.id " +
                            "JOIN modules m ON o.module_id=m.id WHERE m.user_id=?",
                    new String[]{String.valueOf(userId)});
            int percent = 0;
            if (c.moveToFirst()) {
                int total = c.getInt(0);
                int done  = c.getInt(1);
                if (total > 0) percent = (done * 100) / total;
            }
            c.close();
            return percent;
        } catch (Exception e) { return 0; }
    }

    private void rescheduleOnBoot(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                NotificationsActivity.PREFS_NOTIF, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(NotificationsActivity.KEY_ALL_NOTIFS, true)) return;
        if (prefs.getBoolean(NotificationsActivity.KEY_TASK_REMINDERS, true))
            NotificationHelper.scheduleTaskReminderDaily(context);
        if (prefs.getBoolean(NotificationsActivity.KEY_DEADLINE_ALERTS, true))
            NotificationHelper.scheduleDeadlineAlertDaily(context);
        if (prefs.getBoolean(NotificationsActivity.KEY_PROGRESS_UPDATES, false))
            NotificationHelper.scheduleWeeklyProgress(context);
    }
}