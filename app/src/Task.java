package com.example.planexia.data;

public class Task {
    private int id;
    private String title;
    private boolean isDone;
    private String dueDate;
    private String resourceText;

    public Task(int id, String title, boolean isDone, String dueDate, String resourceText) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDone() {
        return isDone;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getResourceText() {
        return resourceText;
    }
}
