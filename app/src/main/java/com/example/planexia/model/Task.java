package com.example.planexia.model;

public class Task {

    private long id;
    private String title;
    private boolean isDone;
    private String dueDate;       // format YYYY-MM-DD (peut être null)
    private String resourceText;  // lien / note / ressource (peut être null)
    private String moduleName;     // nom du module parent (pour affichage dans la liste)
    private String moduleColor;    // couleur hex du module parent
    private String objectiveName;  // nom de l'objectif parent

    public Task(long id, String title, boolean isDone, String dueDate, String resourceText) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
        this.moduleName = "";
        this.moduleColor = "#7B1FA2";
        this.objectiveName = "";
    }

    public Task(long id, String title, boolean isDone, String dueDate, String resourceText,
                String moduleName, String moduleColor) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
        this.moduleName = moduleName != null ? moduleName : "";
        this.moduleColor = moduleColor != null ? moduleColor : "#7B1FA2";
        this.objectiveName = "";
    }

    public Task(long id, String title, boolean isDone, String dueDate, String resourceText,
                String moduleName, String moduleColor, String objectiveName) {
        this.id = id;
        this.title = title;
        this.isDone = isDone;
        this.dueDate = dueDate;
        this.resourceText = resourceText;
        this.moduleName = moduleName != null ? moduleName : "";
        this.moduleColor = moduleColor != null ? moduleColor : "#7B1FA2";
        this.objectiveName = objectiveName != null ? objectiveName : "";
    }

    // Constructeur simplifié (ajout rapide sans date ni ressource)
    public Task(String title) {
        this.id = -1;
        this.title = title;
        this.isDone = false;
        this.dueDate = null;
        this.resourceText = null;
        this.moduleName = "";
        this.moduleColor = "#7B1FA2";
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

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName != null ? moduleName : ""; }

    public String getModuleColor() { return moduleColor; }
    public void setModuleColor(String moduleColor) { this.moduleColor = moduleColor != null ? moduleColor : "#7B1FA2"; }

    public String getObjectiveName() { return objectiveName; }
    public void setObjectiveName(String objectiveName) { this.objectiveName = objectiveName != null ? objectiveName : ""; }
}