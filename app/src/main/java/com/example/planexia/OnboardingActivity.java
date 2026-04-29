package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.data.SessionManager;
import com.example.planexia.notifications.NotificationHelper;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createNotificationChannels(this);

        // Si déjà connecté → aller directement à l'app
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, PlanningActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_onboarding);

        Button btnCommencer = findViewById(R.id.btnCommencer);
        btnCommencer.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, WelcomeActivity.class));
            finish();
        });
    }
}