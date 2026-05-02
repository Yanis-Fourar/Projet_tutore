package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.data.SessionManager;

/**
 * Premier écran : choix entre se connecter et créer un compte.
 * Si l'utilisateur est déjà connecté, on saute cet écran et on va à
 * PlanningActivity (le vrai dashboard de l'app).
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si déjà connecté → PlanningActivity directement
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            Intent intent = new Intent(this, PlanningActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
}