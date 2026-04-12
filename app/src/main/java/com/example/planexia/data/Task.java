package com.example.planexia.data;

public class Task {
    private int id;
    private int objectiveId;
    private String title;
    private boolean isDone;
    private String dueDate;
    private String resourceText;
    private String moduleName;
    private String moduleColor;

    public Task(int id, int objectiveId, String title, boolean isDone, String dueDate, String resourceText) {
        this.id = id;
        this.objectiveId = objectiveId;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
    }

    public Task(int id, int objectiveId, String title, boolean isDone, String dueDate, String resourceText,
                String moduleName, String moduleColor) {
        this(id, objectiveId, title, isDone, dueDate, resourceText);
        this.moduleName = moduleName;
        this.moduleColor = moduleColor;
    }

    public int getId() { return id; }
    public int getObjectiveId() { return objectiveId; }
    public String getTitle() { return title; }
    public boolean isDone() { return isDone; }
    public String getDueDate() { return dueDate; }
    public String getResourceText() { return resourceText; }
    public String getModuleName() { return moduleName; }
    public String getModuleColor() { return moduleColor; }
}
