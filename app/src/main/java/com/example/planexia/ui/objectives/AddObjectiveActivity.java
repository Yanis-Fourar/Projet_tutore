package com.example.planexia.ui.objectives;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddObjectiveActivity extends AppCompatActivity {

    private EditText etTitle;
    private TextView tvDeadlineValue;
    private Button btnPickDate, btnSave;

    private String selectedDueDate = "";
    private boolean isEditMode = false;
    private long editObjectiveId = -1; // ID en DB, pas la position dans la liste

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_objective);

        etTitle         = findViewById(R.id.etObjectiveTitle);
        tvDeadlineValue = findViewById(R.id.tvDeadlineValue);
        btnPickDate     = findViewById(R.id.btnPickDate);
        btnSave         = findViewById(R.id.btnSaveObjective);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnPickDate.setOnClickListener(v -> openDatePicker());
        btnSave.setOnClickListener(v -> saveObjective());

        readEditDataIfNeeded();
    }

    private void openDatePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDueDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvDeadlineValue.setText(String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.show();
    }

    private void readEditDataIfNeeded() {
        Intent intent = getIntent();
        if (intent == null) return;

        isEditMode = intent.getBooleanExtra("edit_mode", false);
        if (!isEditMode) return;

        // Récupère l'ID en DB (pas la position)
        editObjectiveId = intent.getLongExtra("edit_objective_id", -1);
        String title   = intent.getStringExtra(ObjectivesActivity.KEY_TITLE);
        String dueDate = intent.getStringExtra(ObjectivesActivity.KEY_DUE_DATE);

        etTitle.setText(title);

        if (dueDate != null && !dueDate.isEmpty()) {
            selectedDueDate = dueDate;
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
            Toast.makeText(this, "Choisissez une date limite", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date chosen = sdf.parse(selectedDueDate);
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if (chosen.before(today.getTime())) {
                Toast.makeText(this, "La date doit être aujourd'hui ou dans le futur", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Date invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent result = new Intent();
        result.putExtra(ObjectivesActivity.KEY_TITLE, title);
        result.putExtra(ObjectivesActivity.KEY_DUE_DATE, selectedDueDate);
        result.putExtra("edit_mode", isEditMode);
        result.putExtra("edit_objective_id", editObjectiveId); // ID en DB
        setResult(RESULT_OK, result);
        finish();
    }
}

