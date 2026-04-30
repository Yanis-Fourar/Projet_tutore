package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;
import com.example.planexia.data.SessionManager;

/**
 * Premier écran : choix entre se connecter et créer un compte.
 * Si l'utilisateur est déjà connecté, on saute cet écran et on va au Dashboard.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si déjà connecté → DashboardActivity directement
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            goToDashboard();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button btnGoLogin = findViewById(R.id.btnGoLogin);
        Button btnGoRegister = findViewById(R.id.btnGoRegister);

        btnGoLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void goToDashboard() {
        // ⚠️ DashboardActivity sera créée par un autre membre.
        // Tant qu'elle n'existe pas, on reste sur Welcome (le try/catch évite le crash).
        try {
            Class<?> dashboardClass = Class.forName("com.example.planexia.ui.DashboardActivity");
            Intent intent = new Intent(this, dashboardClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}