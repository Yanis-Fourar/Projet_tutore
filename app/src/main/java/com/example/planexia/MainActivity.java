package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.notifications.NotificationHelper;
import com.example.planexia.ui.modules.ModulesActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannels(this);

        // Redirige vers l'écran principal
        startActivity(new Intent(this, ModulesActivity.class));
        finish();
    }
}