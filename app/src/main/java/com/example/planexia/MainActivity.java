package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.ui.tasks.ObjectiveDetailActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ============================================================
        // TEST TEMPORAIRE — à supprimer quand les autres auront fini
        // ============================================================
        PlanexiaRepository repo = new PlanexiaRepository(this);

        // Crée un user test (retourne -1 si l'email existe déjà, c'est normal)
        long userId = repo.createUser("test@test.com", "1234");
        if (userId == -1) {
            // L'user existe déjà, on récupère son id
            userId = repo.login("test@test.com", "1234");
        }

        // Crée un module test
        long moduleId = repo.addModule(userId, "Mathématiques", 2, "#7B1FA2");

        // Crée un objectif test
        long objectiveId = repo.addObjective(moduleId, "Révisions examens", "2025-06-15");

        // Lance directement ton écran tasks
        Intent intent = new Intent(this, ObjectiveDetailActivity.class);
        intent.putExtra(ObjectiveDetailActivity.EXTRA_OBJECTIVE_ID, objectiveId);
        intent.putExtra(ObjectiveDetailActivity.EXTRA_OBJECTIVE_TITLE, "Révisions examens");
        startActivity(intent);
        // ============================================================
    }
}