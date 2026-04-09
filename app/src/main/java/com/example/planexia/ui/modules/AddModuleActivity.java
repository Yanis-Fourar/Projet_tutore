package com.example.planexia.ui.modules;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;

public class AddModuleActivity extends AppCompatActivity {

    private EditText etName, etCoefficient;
    private ViewColorSelector selectedColor = ViewColorSelector.BLUE;

    private ImageView colorBlue, colorPurple, colorGreen, colorOrange;
    private Button btnSave;

    private boolean isEditMode = false;
    private int editPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_module);

        etName = findViewById(R.id.etName);
        etCoefficient = findViewById(R.id.etCoefficient);

        colorBlue = findViewById(R.id.colorBlue);
        colorPurple = findViewById(R.id.colorPurple);
        colorGreen = findViewById(R.id.colorGreen);
        colorOrange = findViewById(R.id.colorOrange);

        btnSave = findViewById(R.id.btnSaveModule);

        colorBlue.setOnClickListener(v -> selectedColor = ViewColorSelector.BLUE);
        colorPurple.setOnClickListener(v -> selectedColor = ViewColorSelector.PURPLE);
        colorGreen.setOnClickListener(v -> selectedColor = ViewColorSelector.GREEN);
        colorOrange.setOnClickListener(v -> selectedColor = ViewColorSelector.ORANGE);

        readEditDataIfNeeded();

        btnSave.setOnClickListener(v -> saveModule());
    }

    private void readEditDataIfNeeded() {
        Intent intent = getIntent();

        if (intent != null && intent.hasExtra("edit_mode")) {
            isEditMode = intent.getBooleanExtra("edit_mode", false);
        }

        if (isEditMode) {
            editPosition = intent.getIntExtra("edit_position", -1);

            String name = intent.getStringExtra("module_name");
            int coefficient = intent.getIntExtra("module_coefficient", 1);
            String color = intent.getStringExtra("module_color");

            etName.setText(name);
            etCoefficient.setText(String.valueOf(coefficient));

            if (color != null) {
                selectedColor = colorFromHex(color);
            }

            btnSave.setText("Modifier");
        }
    }

    private void saveModule() {
        String name = etName.getText().toString().trim();
        String coefficientText = etCoefficient.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Entrez un nom");
            return;
        }

        if (coefficientText.isEmpty()) {
            etCoefficient.setError("Entrez un coefficient");
            return;
        }

        int coefficient;
        try {
            coefficient = Integer.parseInt(coefficientText);
        } catch (NumberFormatException e) {
            etCoefficient.setError("Coefficient invalide");
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("module_name", name);
        resultIntent.putExtra("module_coefficient", coefficient);
        resultIntent.putExtra("module_color", selectedColor.hex);
        resultIntent.putExtra("edit_mode", isEditMode);
        resultIntent.putExtra("edit_position", editPosition);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private ViewColorSelector colorFromHex(String hex) {
        for (ViewColorSelector color : ViewColorSelector.values()) {
            if (color.hex.equalsIgnoreCase(hex)) {
                return color;
            }
        }
        return ViewColorSelector.BLUE;
    }

    private enum ViewColorSelector {
        BLUE("#4F8EF7"),
        PURPLE("#8B5CF6"),
        GREEN("#10B981"),
        ORANGE("#F59E0B");

        final String hex;

        ViewColorSelector(String hex) {
            this.hex = hex;
        }
    }
}