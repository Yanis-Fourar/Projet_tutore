package com.example.planexia.ui.tasks;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planexia.R;
import com.example.planexia.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TasksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTodo;
    private TextView tvDone;

    private List<Task> taskList;
    private TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerViewTasks);
        tvTodo = findViewById(R.id.tvTodoCount);
        tvDone = findViewById(R.id.tvDoneCount);

        // Initialisation de la liste temporaire
        taskList = new ArrayList<>();
        taskList.add(new Task("Réviser les dérivées"));
        taskList.add(new Task("TD mécanique"));
        taskList.add(new Task("Projet algorithmes"));

        // Configuration RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TaskAdapter(taskList, () -> {
            updateCounts();
        });

        recyclerView.setAdapter(adapter);

        // Mettre à jour les compteurs au démarrage
        updateCounts();
    }

    // Mise à jour des compteurs À faire / Complétées
    private void updateCounts() {

        int done = 0;

        for (Task t : taskList) {
            if (t.isDone()) {
                done++;
            }
        }

        int todo = taskList.size() - done;

        tvTodo.setText("À faire : " + todo);
        tvDone.setText("Complétées : " + done);
    }
}