package com.example.planexia.model;

public class Task {

    private long id;
    private String title;
    private boolean isDone;
    private String dueDate;       // format YYYY-MM-DD (peut être null)
    private String resourceText;  // lien / note / ressource (peut être null)

    public Task(long id, String title, boolean isDone, String dueDate, String resourceText) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
    }

    // Constructeur simplifié (ajout rapide sans date ni ressource)
    public Task(String title) {
        this.id = -1;
        this.title = title;
        this.isDone = false;
        this.dueDate = null;
        this.resourceText = null;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isDone() { return isDone; }
    public void setDone(boolean done) { isDone = done; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getResourceText() { return resourceText; }
    public void setResourceText(String resourceText) { this.resourceText = resourceText; }
}