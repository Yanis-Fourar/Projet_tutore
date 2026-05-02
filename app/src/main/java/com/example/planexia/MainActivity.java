package com.example.planexia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.planexia.notifications.NotificationHelper;
import com.example.planexia.ui.modules.ModulesActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIF_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Créer les channels
        NotificationHelper.createNotificationChannels(this);

        // 2. Demander la permission POST_NOTIFICATIONS sur Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIF_PERMISSION);
                return; // On attend la réponse dans onRequestPermissionsResult
            }
        }

        // 3. Planifier les alarmes et continuer
        scheduleAndProceed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Qu'il accepte ou refuse, on continue quand même
        scheduleAndProceed();
    }

    private void scheduleAndProceed() {
        // Planifier les notifications quotidiennes (seulement si pas déjà fait)
        NotificationHelper.scheduleTaskReminderDaily(this);
        NotificationHelper.scheduleDeadlineAlertDaily(this);

        startActivity(new Intent(this, ModulesActivity.class));
        finish();
    }
}