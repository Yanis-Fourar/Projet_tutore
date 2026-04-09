package com.example.planexia.model;

public class Objective {
    private int id;
    private int moduleId;
    private String title;
    private String dueDate; // format "yyyy-MM-dd" — correspond à C_DUE_DATE en DB

    public Objective(int id, int moduleId, String title, String dueDate) {
        this.id = id;
        this.moduleId = moduleId;
        this.title = title;
        this.dueDate = dueDate;
    }

    public int getId() { return id; }
    public int getModuleId() { return moduleId; }
    public String getTitle() { return title; }
    public String getDueDate() { return dueDate; }

    public void setTitle(String title) { this.title = title; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    /**
     * Retourne le nombre de jours restants avant la dueDate.
     * Négatif si en retard, 0 si c'est aujourd'hui.
     */
    public long getDaysRemaining() {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date dueDateParsed = sdf.parse(dueDate);
            java.util.Calendar calToday = java.util.Calendar.getInstance();
            calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
            calToday.set(java.util.Calendar.MINUTE, 0);
            calToday.set(java.util.Calendar.SECOND, 0);
            calToday.set(java.util.Calendar.MILLISECOND, 0);
            long diff = dueDateParsed.getTime() - calToday.getTimeInMillis();
            return diff / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }
}