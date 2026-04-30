package com.example.planexia.notifications;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaDatabaseHelper;
import com.example.planexia.data.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class NotificationsActivity extends AppCompatActivity {

    public static final String PREFS_NOTIF           = "planexia_notif_prefs";
    public static final String KEY_ALL_NOTIFS         = "all_notifs";
    public static final String KEY_TASK_REMINDERS     = "task_reminders";
    public static final String KEY_DEADLINE_ALERTS    = "deadline_alerts";
    public static final String KEY_PROGRESS_UPDATES   = "progress_updates";
    public static final String KEY_EMAIL_NOTIFS       = "email_notifs";

    private SharedPreferences prefs;

    private SwitchMaterial switchAll;
    private SwitchMaterial switchTaskReminders;
    private SwitchMaterial switchDeadlineAlerts;
    private SwitchMaterial switchProgressUpdates;
    private SwitchMaterial switchEmailNotifs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        NotificationHelper.createNotificationChannels(this);

        prefs = getSharedPreferences(PREFS_NOTIF, MODE_PRIVATE);

        switchAll             = findViewById(R.id.switchAllNotifs);
        switchTaskReminders   = findViewById(R.id.switchTaskReminders);
        switchDeadlineAlerts  = findViewById(R.id.switchDeadlineAlerts);
        switchProgressUpdates = findViewById(R.id.switchProgressUpdates);
        switchEmailNotifs     = findViewById(R.id.switchEmailNotifs);
        TextView tvEmail      = findViewById(R.id.tvUserEmail);

        CardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Afficher l'email de l'utilisateur
        if (tvEmail != null) tvEmail.setText(getUserEmail());

        // Charger les états sauvegardés
        loadSwitchStates();

        // Master switch
        switchAll.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_ALL_NOTIFS, isChecked).apply();
            setChildSwitchesEnabled(isChecked);
            if (isChecked) {
                if (switchTaskReminders.isChecked())
                    NotificationHelper.scheduleTaskReminderDaily(this);
                if (switchDeadlineAlerts.isChecked())
                    NotificationHelper.scheduleDeadlineAlertDaily(this);
                if (switchProgressUpdates.isChecked())
                    NotificationHelper.scheduleWeeklyProgress(this);
            } else {
                NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_TASK_REMINDER);
                NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_DEADLINE_DAILY);
                NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_PROGRESS_WEEKLY);
            }
        });

        switchTaskReminders.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_TASK_REMINDERS, isChecked).apply();
            if (isChecked) NotificationHelper.scheduleTaskReminderDaily(this);
            else NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_TASK_REMINDER);
        });

        switchDeadlineAlerts.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_DEADLINE_ALERTS, isChecked).apply();
            if (isChecked) NotificationHelper.scheduleDeadlineAlertDaily(this);
            else NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_DEADLINE_DAILY);
        });

        switchProgressUpdates.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_PROGRESS_UPDATES, isChecked).apply();
            if (isChecked) NotificationHelper.scheduleWeeklyProgress(this);
            else NotificationHelper.cancelAlarm(this, NotificationHelper.ALARM_PROGRESS_WEEKLY);
        });

        switchEmailNotifs.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(KEY_EMAIL_NOTIFS, isChecked).apply());
    }

    private void loadSwitchStates() {
        boolean allOn = prefs.getBoolean(KEY_ALL_NOTIFS, true);
        switchAll.setChecked(allOn);
        switchTaskReminders.setChecked(prefs.getBoolean(KEY_TASK_REMINDERS, true));
        switchDeadlineAlerts.setChecked(prefs.getBoolean(KEY_DEADLINE_ALERTS, true));
        switchProgressUpdates.setChecked(prefs.getBoolean(KEY_PROGRESS_UPDATES, false));
        switchEmailNotifs.setChecked(prefs.getBoolean(KEY_EMAIL_NOTIFS, true));
        setChildSwitchesEnabled(allOn);
    }

    private void setChildSwitchesEnabled(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.4f;
        switchTaskReminders.setEnabled(enabled);
        switchTaskReminders.setAlpha(alpha);
        switchDeadlineAlerts.setEnabled(enabled);
        switchDeadlineAlerts.setAlpha(alpha);
        switchProgressUpdates.setEnabled(enabled);
        switchProgressUpdates.setAlpha(alpha);
        switchEmailNotifs.setEnabled(enabled);
        switchEmailNotifs.setAlpha(alpha);
    }

    private String getUserEmail() {
        SessionManager session = new SessionManager(this);
        long userId = session.getUserId();
        if (userId == -1) return "";
        try {
            SQLiteDatabase db = new PlanexiaDatabaseHelper(this).getReadableDatabase();
            Cursor c = db.query(PlanexiaDatabaseHelper.T_USERS,
                    new String[]{PlanexiaDatabaseHelper.C_EMAIL},
                    PlanexiaDatabaseHelper.C_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null);
            String email = "";
            if (c.moveToFirst()) email = c.getString(0);
            c.close();
            return email;
        } catch (Exception e) { return ""; }
    }
}