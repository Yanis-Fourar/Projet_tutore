package com.example.planexia.ui.objectives;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;

import java.util.Calendar;

public class AddObjectiveActivity extends AppCompatActivity {

    private EditText etTitle;
    private TextView tvDeadlineValue;
    private Button btnPickDate, btnSave;

    private String selectedDueDate = ""; // format yyyy-MM-dd — correspond à C_DUE_DATE
    private boolean isEditMode = false;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_objective);

        etTitle         = findViewById(R.id.etObjectiveTitle);
        tvDeadlineValue = findViewById(R.id.tvDeadlineValue);
        btnPickDate     = findViewById(R.id.btnPickDate);
        btnSave         = findViewById(R.id.btnSaveObjective);

        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnSave.setOnClickListener(v -> saveObjective());

        readEditDataIfNeeded();
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    // Stocke en yyyy-MM-dd pour la DB
                    selectedDueDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    // Affiche en dd/MM/yyyy pour l'utilisateur
                    tvDeadlineValue.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void readEditDataIfNeeded() {
        Intent intent = getIntent();
        if (intent == null) return;

        isEditMode = intent.getBooleanExtra("edit_mode", false);
        if (!isEditMode) return;

        editPosition = intent.getIntExtra("edit_position", -1);
        String title   = intent.getStringExtra(ObjectivesActivity.KEY_TITLE);
        String dueDate = intent.getStringExtra(ObjectivesActivity.KEY_DUE_DATE);

        etTitle.setText(title);

        if (dueDate != null && !dueDate.isEmpty()) {
            selectedDueDate = dueDate;
            // Convertit yyyy-MM-dd → dd/MM/yyyy pour l'affichage
            String[] parts = dueDate.split("-");
            if (parts.length == 3) {
                tvDeadlineValue.setText(parts[2] + "/" + parts[1] + "/" + parts[0]);
            }
        }

        btnSave.setText("Modifier");
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        if (tvFormTitle != null) tvFormTitle.setText("Modifier l'objectif");
    }

    private void saveObjective() {
        String title = etTitle.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Entrez un titre");
            return;
        }
        if (selectedDueDate.isEmpty()) {
            tvDeadlineValue.setError("Choisissez une date");
            return;
        }

        Intent result = new Intent();
        result.putExtra(ObjectivesActivity.KEY_TITLE, title);
        result.putExtra(ObjectivesActivity.KEY_DUE_DATE, selectedDueDate); // yyyy-MM-dd
        result.putExtra("edit_mode", isEditMode);
        result.putExtra("edit_position", editPosition);
        setResult(RESULT_OK, result);
        finish();
    }
}