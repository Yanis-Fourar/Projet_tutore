package com.example.planexia.data;

public class Objective {
    private int id;
    private int moduleId;
    private String title;
    private String dueDate;

    public Objective(int id, int moduleId, String title, String dueDate) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }
}
