package com.example.planexia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.ui.modules.ModulesActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlanexiaRepository repo = new PlanexiaRepository(this);

        // Récupérer l'userId depuis les SharedPreferences
        SharedPreferences prefs = getSharedPreferences("planexia_prefs", MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId == -1) {
            // Pas encore d'user → créer un user test et sauvegarder son id
            long newId = repo.createUser("test@test.com", "1234");
            if (newId == -1) {
                // L'user existe déjà → récupérer son id
                newId = repo.login("test@test.com", "1234");
            }
            userId = newId;

            // Sauvegarder l'userId pour les prochains lancements
            prefs.edit().putLong("user_id", userId).apply();
        }

        // Lancer l'écran principal : liste des modules
        Intent intent = new Intent(this, ModulesActivity.class);
        startActivity(intent);
        finish(); // fermer MainActivity pour ne pas revenir dessus
    }
}