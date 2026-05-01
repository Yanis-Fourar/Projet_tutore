package com.example.planexia.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.planexia.model.Module;
import com.example.planexia.model.Objective;
import com.example.planexia.model.Task;

import java.util.ArrayList;
import java.util.List;

public class PlanexiaRepository {

    private final PlanexiaDatabaseHelper dbHelper;

    public PlanexiaRepository(Context context) {
        this.dbHelper = new PlanexiaDatabaseHelper(context);
    }

    // ---------- USERS ----------
    public long createUser(String email, String password, String pseudo, String filiere, String annee) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_EMAIL, email);
        values.put(PlanexiaDatabaseHelper.C_PASSWORD_HASH, password);
        values.put(PlanexiaDatabaseHelper.C_IS_PREMIUM, 0);
        values.put(PlanexiaDatabaseHelper.C_PSEUDO, pseudo);
        values.put(PlanexiaDatabaseHelper.C_FILIERE, filiere);
        values.put(PlanexiaDatabaseHelper.C_ANNEE, annee);
        return db.insert(PlanexiaDatabaseHelper.T_USERS, null, values);
    }

    public long login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_USERS,
                new String[]{PlanexiaDatabaseHelper.C_ID},
                PlanexiaDatabaseHelper.C_EMAIL + " = ? AND " + PlanexiaDatabaseHelper.C_PASSWORD_HASH + " = ?",
                new String[]{email, password},
                null, null, null
        );
        long userId = -1;
        if (c.moveToFirst()) userId = c.getLong(0);
        c.close();
        return userId;
    }

    public String[] getUserInfo(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_USERS,
                new String[]{ PlanexiaDatabaseHelper.C_PSEUDO, PlanexiaDatabaseHelper.C_FILIERE, PlanexiaDatabaseHelper.C_ANNEE },
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{ String.valueOf(userId) },
                null, null, null
        );
        String[] info = {"?", "?", "?"};
        if (c.moveToFirst()) {
            info[0] = c.getString(0);
            info[1] = c.getString(1);
            info[2] = c.getString(2);
        }
        c.close();
        return info;
    }

    // ← AJOUT : Premium
    public void setPremium(long userId, boolean isPremium) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_IS_PREMIUM, isPremium ? 1 : 0);
        db.update(
                PlanexiaDatabaseHelper.T_USERS,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean isPremium(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_USERS,
                new String[]{PlanexiaDatabaseHelper.C_IS_PREMIUM},
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        boolean premium = false;
        if (c.moveToFirst()) premium = c.getInt(0) == 1;
        c.close();
        return premium;
    }

    // ---------- MODULES ----------
    public long addModule(long userId, String name, int coefficient, String color) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_USER_ID, userId);
        values.put(PlanexiaDatabaseHelper.C_NAME, name);
        values.put(PlanexiaDatabaseHelper.C_COEFFICIENT, coefficient);
        values.put(PlanexiaDatabaseHelper.C_COLOR, color);
        return db.insert(PlanexiaDatabaseHelper.T_MODULES, null, values);
    }

    public List<Module> getModulesByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_MODULES,
                new String[]{
                        PlanexiaDatabaseHelper.C_ID,
                        PlanexiaDatabaseHelper.C_NAME,
                        PlanexiaDatabaseHelper.C_COEFFICIENT,
                        PlanexiaDatabaseHelper.C_COLOR
                },
                PlanexiaDatabaseHelper.C_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " DESC"
        );
        List<Module> modules = new ArrayList<>();
        while (c.moveToNext()) {
            modules.add(new Module(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3)));
        }
        c.close();
        return modules;
    }

    public int updateModule(int moduleId, String name, int coefficient, String color) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_NAME, name);
        values.put(PlanexiaDatabaseHelper.C_COEFFICIENT, coefficient);
        values.put(PlanexiaDatabaseHelper.C_COLOR, color);
        return db.update(
                PlanexiaDatabaseHelper.T_MODULES,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(moduleId)}
        );
    }

    public int deleteModule(int moduleId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                PlanexiaDatabaseHelper.T_MODULES,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(moduleId)}
        );
    }

    // ---------- OBJECTIVES ----------
    public long addObjective(long moduleId, String title, String dueDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_MODULE_ID, moduleId);
        values.put(PlanexiaDatabaseHelper.C_TITLE, title);
        values.put(PlanexiaDatabaseHelper.C_DUE_DATE, dueDate);
        return db.insert(PlanexiaDatabaseHelper.T_OBJECTIVES, null, values);
    }

    public List<Objective> getObjectivesByModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                new String[]{
                        PlanexiaDatabaseHelper.C_ID,
                        PlanexiaDatabaseHelper.C_MODULE_ID,
                        PlanexiaDatabaseHelper.C_TITLE,
                        PlanexiaDatabaseHelper.C_DUE_DATE
                },
                PlanexiaDatabaseHelper.C_MODULE_ID + " = ?",
                new String[]{String.valueOf(moduleId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " DESC"
        );
        List<Objective> objectives = new ArrayList<>();
        while (c.moveToNext()) {
            objectives.add(new Objective(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3)));
        }
        c.close();
        return objectives;
    }

    public List<Objective> getObjectivesDetailByModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                new String[]{
                        PlanexiaDatabaseHelper.C_ID,
                        PlanexiaDatabaseHelper.C_MODULE_ID,
                        PlanexiaDatabaseHelper.C_TITLE,
                        PlanexiaDatabaseHelper.C_DUE_DATE
                },
                PlanexiaDatabaseHelper.C_MODULE_ID + " = ?",
                new String[]{String.valueOf(moduleId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " ASC"
        );
        List<Objective> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(new Objective(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3)));
        }
        c.close();
        return list;
    }

    public int updateObjective(long objectiveId, String title, String dueDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_TITLE, title);
        values.put(PlanexiaDatabaseHelper.C_DUE_DATE, dueDate);
        return db.update(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(objectiveId)}
        );
    }

    public int deleteObjective(long objectiveId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(objectiveId)}
        );
    }

    // ---------- TASKS ----------
    public long addTask(long objectiveId, String title, String dueDate, String resourceText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_OBJECTIVE_ID, objectiveId);
        values.put(PlanexiaDatabaseHelper.C_TITLE, title);
        values.put(PlanexiaDatabaseHelper.C_IS_DONE, 0);
        values.put(PlanexiaDatabaseHelper.C_DUE_DATE, dueDate);
        values.put(PlanexiaDatabaseHelper.C_RESOURCE_TEXT, resourceText);
        return db.insert(PlanexiaDatabaseHelper.T_TASKS, null, values);
    }

    public int setTaskDone(long taskId, boolean done) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_IS_DONE, done ? 1 : 0);
        return db.update(
                PlanexiaDatabaseHelper.T_TASKS,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public int updateTask(long taskId, String title, String dueDate, String resourceText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_TITLE, title);
        values.put(PlanexiaDatabaseHelper.C_DUE_DATE, dueDate);
        values.put(PlanexiaDatabaseHelper.C_RESOURCE_TEXT, resourceText);
        return db.update(
                PlanexiaDatabaseHelper.T_TASKS,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public int deleteTask(long taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                PlanexiaDatabaseHelper.T_TASKS,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public String getObjectiveDueDateForTask(long taskId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT o." + PlanexiaDatabaseHelper.C_DUE_DATE +
                " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID +
                " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                " WHERE t." + PlanexiaDatabaseHelper.C_ID + " = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(taskId)});
        String dueDate = null;
        if (c.moveToFirst()) dueDate = c.getString(0);
        c.close();
        return dueDate;
    }

    public List<Task> getTasksByObjective(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_TASKS,
                new String[]{
                        PlanexiaDatabaseHelper.C_ID,
                        PlanexiaDatabaseHelper.C_TITLE,
                        PlanexiaDatabaseHelper.C_IS_DONE,
                        PlanexiaDatabaseHelper.C_DUE_DATE,
                        PlanexiaDatabaseHelper.C_RESOURCE_TEXT
                },
                PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?",
                new String[]{String.valueOf(objectiveId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " DESC"
        );
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(c.getLong(0), c.getString(1), c.getInt(2) == 1, c.getString(3), c.getString(4)));
        }
        c.close();
        return tasks;
    }

    public List<Task> getAllTasksForUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        ", t." + PlanexiaDatabaseHelper.C_TITLE +
                        ", t." + PlanexiaDatabaseHelper.C_IS_DONE +
                        ", t." + PlanexiaDatabaseHelper.C_DUE_DATE +
                        ", t." + PlanexiaDatabaseHelper.C_RESOURCE_TEXT +
                        ", m." + PlanexiaDatabaseHelper.C_NAME +
                        ", m." + PlanexiaDatabaseHelper.C_COLOR +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?" +
                        " ORDER BY t." + PlanexiaDatabaseHelper.C_DUE_DATE + " ASC, t." + PlanexiaDatabaseHelper.C_ID + " ASC;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(
                    c.getLong(0), c.getString(1), c.getInt(2) == 1,
                    c.getString(3), c.getString(4), c.getString(5), c.getString(6)
            ));
        }
        c.close();
        return tasks;
    }

    // ---------- PLANNING ----------
    public List<Task> getTasksForDateWithModule(long userId, String dateYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        ", t." + PlanexiaDatabaseHelper.C_TITLE +
                        ", t." + PlanexiaDatabaseHelper.C_IS_DONE +
                        ", t." + PlanexiaDatabaseHelper.C_DUE_DATE +
                        ", t." + PlanexiaDatabaseHelper.C_RESOURCE_TEXT +
                        ", m." + PlanexiaDatabaseHelper.C_NAME +
                        ", m." + PlanexiaDatabaseHelper.C_COLOR +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), dateYYYYMMDD});
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(
                    c.getLong(0), c.getString(1), c.getInt(2) == 1,
                    c.getString(3), c.getString(4), c.getString(5), c.getString(6)
            ));
        }
        c.close();
        return tasks;
    }

    public List<Task> getTasksForDate(long userId, String dateYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        ", t." + PlanexiaDatabaseHelper.C_TITLE +
                        ", t." + PlanexiaDatabaseHelper.C_IS_DONE +
                        ", t." + PlanexiaDatabaseHelper.C_DUE_DATE +
                        ", t." + PlanexiaDatabaseHelper.C_RESOURCE_TEXT +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), dateYYYYMMDD});
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(c.getLong(0), c.getString(1), c.getInt(2) == 1, c.getString(3), c.getString(4)));
        }
        c.close();
        return tasks;
    }

    public List<Task> getOverdueTasks(long userId, String todayYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        ", t." + PlanexiaDatabaseHelper.C_TITLE +
                        ", t." + PlanexiaDatabaseHelper.C_IS_DONE +
                        ", t." + PlanexiaDatabaseHelper.C_DUE_DATE +
                        ", t." + PlanexiaDatabaseHelper.C_RESOURCE_TEXT +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?" +
                        " AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 0" +
                        " AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " IS NOT NULL" +
                        " AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " < ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), todayYYYYMMDD});
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(new Task(c.getLong(0), c.getString(1), c.getInt(2) == 1, c.getString(3), c.getString(4)));
        }
        c.close();
        return tasks;
    }

    // ---------- PROGRESSION ----------
    public int getObjectiveProgress(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor totalC = db.rawQuery(
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS +
                        " WHERE " + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?;",
                new String[]{String.valueOf(objectiveId)}
        );
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();
        if (total == 0) return 0;

        Cursor doneC = db.rawQuery(
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS +
                        " WHERE " + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?" +
                        " AND " + PlanexiaDatabaseHelper.C_IS_DONE + " = 1;",
                new String[]{String.valueOf(objectiveId)}
        );
        int done = 0;
        if (doneC.moveToFirst()) done = doneC.getInt(0);
        doneC.close();
        return (int) Math.round((done * 100.0) / total);
    }

    public int getGlobalProgress(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int[] counts = getTotalAndDone(db, userId);
        if (counts[0] == 0) return 0;
        return (int) Math.round((counts[1] * 100.0) / counts[0]);
    }

    public int getCompletedTasksCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getTotalAndDone(db, userId)[1];
    }

    public int getTotalTasksCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getTotalAndDone(db, userId)[0];
    }

    private int[] getTotalAndDone(SQLiteDatabase db, long userId) {
        String base =
                " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?";

        Cursor totalC = db.rawQuery("SELECT COUNT(*)" + base + ";", new String[]{String.valueOf(userId)});
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();

        Cursor doneC = db.rawQuery(
                "SELECT COUNT(*)" + base + " AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 1;",
                new String[]{String.valueOf(userId)}
        );
        int done = 0;
        if (doneC.moveToFirst()) done = doneC.getInt(0);
        doneC.close();

        return new int[]{total, done};
    }

    public int[] getProgressForModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sqlTotal =
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " WHERE o." + PlanexiaDatabaseHelper.C_MODULE_ID + " = ?;";

        Cursor totalC = db.rawQuery(sqlTotal, new String[]{String.valueOf(moduleId)});
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();

        String sqlDone =
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " WHERE o." + PlanexiaDatabaseHelper.C_MODULE_ID + " = ?" +
                        " AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 1;";

        Cursor doneC = db.rawQuery(sqlDone, new String[]{String.valueOf(moduleId)});
        int done = 0;
        if (doneC.moveToFirst()) done = doneC.getInt(0);
        doneC.close();

        return new int[]{total, done};
    }

    // ══════════════════════════════════════════════════════════════
    //  CHRONO SESSIONS
    // ══════════════════════════════════════════════════════════════

    /** Sauvegarder une session chrono */
    public long addChronoSession(long userId, String taskLabel, long durationMs, int goalMin) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(PlanexiaDatabaseHelper.C_USER_ID,    userId);
        v.put(PlanexiaDatabaseHelper.C_TASK_LABEL, taskLabel);
        v.put(PlanexiaDatabaseHelper.C_DURATION_MS, durationMs);
        v.put(PlanexiaDatabaseHelper.C_GOAL_MIN,   goalMin);
        v.put(PlanexiaDatabaseHelper.C_CREATED_AT, System.currentTimeMillis());
        return db.insert(PlanexiaDatabaseHelper.T_CHRONO_SESSIONS, null, v);
    }

    /**
     * Récupérer les sessions des dernières 24h pour un utilisateur.
     * Retourne une liste de tableaux : [taskLabel, durationMs, goalMin, createdAt]
     */
    public java.util.List<String[]> getChronoSessionsLast24h(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        long since = System.currentTimeMillis() - 24L * 60 * 60 * 1000;

        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_CHRONO_SESSIONS,
                new String[]{
                        PlanexiaDatabaseHelper.C_TASK_LABEL,
                        PlanexiaDatabaseHelper.C_DURATION_MS,
                        PlanexiaDatabaseHelper.C_GOAL_MIN,
                        PlanexiaDatabaseHelper.C_CREATED_AT
                },
                PlanexiaDatabaseHelper.C_USER_ID + " = ? AND " +
                        PlanexiaDatabaseHelper.C_CREATED_AT + " >= ?",
                new String[]{String.valueOf(userId), String.valueOf(since)},
                null, null,
                PlanexiaDatabaseHelper.C_CREATED_AT + " DESC"
        );

        java.util.List<String[]> result = new java.util.ArrayList<>();
        while (c.moveToNext()) {
            result.add(new String[]{
                    c.getString(0),               // taskLabel
                    String.valueOf(c.getLong(1)), // durationMs
                    String.valueOf(c.getInt(2)),  // goalMin
                    String.valueOf(c.getLong(3))  // createdAt
            });
        }
        c.close();
        return result;
    }

    /** Supprimer les sessions de plus de 24h */
    public void deleteOldChronoSessions(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long limit = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
        db.delete(
                PlanexiaDatabaseHelper.T_CHRONO_SESSIONS,
                PlanexiaDatabaseHelper.C_USER_ID + " = ? AND " +
                        PlanexiaDatabaseHelper.C_CREATED_AT + " < ?",
                new String[]{String.valueOf(userId), String.valueOf(limit)}
        );
    }
}