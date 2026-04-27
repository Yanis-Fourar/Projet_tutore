package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;
import com.example.planexia.data.SessionManager;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si déjà connecté → aller au Dashboard directement
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            //  DashboardActivity quand disponible

        }

        setContentView(R.layout.activity_onboarding);

        Button btnCommencer = findViewById(R.id.btnCommencer);
        btnCommencer.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, WelcomeActivity.class));
            finish();
        });
    }
}