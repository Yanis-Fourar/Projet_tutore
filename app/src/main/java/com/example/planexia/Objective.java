package com.example.planexia.data;

public class Objective {
    private int id;
    private String title;
    private String dueDate;

    public Objective(int id, String title, String dueDate) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }
}
