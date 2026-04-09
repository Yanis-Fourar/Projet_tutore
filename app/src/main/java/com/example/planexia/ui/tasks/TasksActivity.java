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

        recyclerView = findViewById(R.id.recyclerViewTasks);
        tvTodo = findViewById(R.id.tvTodoCount);
        tvDone = findViewById(R.id.tvDoneCount);

        taskList = new ArrayList<>();
        taskList.add(new Task("Réviser les dérivées"));
        taskList.add(new Task("TD mécanique"));
        taskList.add(new Task("Projet algorithmes"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nouveau callback compatible avec OnTaskCheckedListener
        adapter = new TaskAdapter(taskList, (taskId, isDone) -> {
            updateCounts();
        });

        recyclerView.setAdapter(adapter);
        updateCounts();
    }

    private void updateCounts() {
        int done = 0;
        for (Task t : taskList) {
            if (t.isDone()) done++;
        }
        int todo = taskList.size() - done;
        tvTodo.setText("À faire : " + todo);
        tvDone.setText("Complétées : " + done);
    }
}