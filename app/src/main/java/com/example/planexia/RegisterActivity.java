package com.example.planexia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.notifications.NotificationHelper;
import com.example.planexia.utils.PasswordUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private EditText etPseudo;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etFiliere;
    private EditText etAnnee;
    private Button btnRegister;

    private PlanexiaRepository repository;
    private SessionManager sessionManager;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = new PlanexiaRepository(this);
        sessionManager = new SessionManager(this);

        etPseudo = findViewById(R.id.etPseudo);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFiliere = findViewById(R.id.etFiliere);
        etAnnee = findViewById(R.id.etAnnee);
        btnRegister = findViewById(R.id.btnRegister);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String pseudo = etPseudo.getText().toString().trim();
        String email = etEmail.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString();
        String filiere = etFiliere.getText().toString().trim();
        String annee = etAnnee.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(pseudo)) {
            etPseudo.setError("Pseudo requis");
            etPseudo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requis");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email invalide");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Au moins 6 caractères");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(filiere)) {
            etFiliere.setError("Filière requise");
            etFiliere.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(annee)) {
            etAnnee.setError("Année requise");
            etAnnee.requestFocus();
            return;
        }

        // ⚠️ NOTE :
        // Le contrat technique définit la table `users` avec uniquement
        // (id, email, password_hash, is_premium).
        // Les champs pseudo / filiere / annee ne sont PAS stockés en V1.
        // → À discuter avec l'équipe : étendre la table users
        //   ou créer une table user_profile (côté membre BDD).

        btnRegister.setEnabled(false);

        // Hash AVANT d'envoyer au repository
        String hash = PasswordUtils.hashPassword(password);

        executor.execute(() -> {
            long result;
            try {
                result = repository.createUser(email, hash, pseudo, filiere, annee);
            } catch (android.database.sqlite.SQLiteConstraintException e) {
                // Email déjà pris (contrainte UNIQUE sur users.email)
                result = -1L;
            }

            final long finalResult = result;
            mainHandler.post(() -> {
                btnRegister.setEnabled(true);
                if (finalResult > 0) {
                    sessionManager.saveUserId(finalResult);
                    // ✅ Notification de bienvenue
                    NotificationHelper.createNotificationChannels(this);
                    NotificationHelper.sendWelcomeNotification(this, pseudo);
                    Toast.makeText(this, "Compte créé avec succès",
                            Toast.LENGTH_SHORT).show();
                    goToDashboard();
                } else {
                    // Le repository.insert() retourne -1 sur conflit unique aussi
                    etEmail.setError("Cet email est déjà utilisé ou erreur BDD");
                    etEmail.requestFocus();
                }
            });
        });
    }

    private void goToDashboard() {
        Intent intent = new Intent(this, PlanningActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}