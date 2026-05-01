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
    public long createUser(String email, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_EMAIL, email);
        values.put(PlanexiaDatabaseHelper.C_PASSWORD_HASH, password);
        values.put(PlanexiaDatabaseHelper.C_IS_PREMIUM, 0);
        return db.insert(PlanexiaDatabaseHelper.T_USERS, null, values);
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

    public void setPremium(long userId, boolean premium) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PlanexiaDatabaseHelper.C_IS_PREMIUM, premium ? 1 : 0);
        db.update(
                PlanexiaDatabaseHelper.T_USERS,
                values,
                PlanexiaDatabaseHelper.C_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
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

    public List<Long> getObjectivesByModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                new String[]{PlanexiaDatabaseHelper.C_ID},
                PlanexiaDatabaseHelper.C_MODULE_ID + " = ?",
                new String[]{String.valueOf(moduleId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " DESC"
        );
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
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

    public List<Long> getTasksByObjective(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_TASKS,
                new String[]{PlanexiaDatabaseHelper.C_ID},
                PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?",
                new String[]{String.valueOf(objectiveId)},
                null, null,
                PlanexiaDatabaseHelper.C_ID + " DESC"
        );
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
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
    public List<Long> getTasksForDate(long userId, String dateYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), dateYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    public List<Long> getOverdueTasks(long userId, String todayYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID +
                        " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                        " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                        " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                        " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?" +
                        " AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 0" +
                        " AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " IS NOT NULL" +
                        " AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " < ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), todayYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
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

    /** Progression globale de l'utilisateur en % */
    public int getGlobalProgress(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int[] counts = getTotalAndDone(db, userId);
        if (counts[0] == 0) return 0;
        return (int) Math.round((counts[1] * 100.0) / counts[0]);
    }

    /** Nombre total de tâches complétées pour l'utilisateur */
    public int getCompletedTasksCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return getTotalAndDone(db, userId)[1];
    }

    /** Nombre total de tâches de l'utilisateur */
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

    public int getPendingTasksCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 0;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getTotalObjectivesCount(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o" +
                " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getObjectivesCountForModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_OBJECTIVES +
                        " WHERE " + PlanexiaDatabaseHelper.C_MODULE_ID + " = ?;",
                new String[]{String.valueOf(moduleId)}
        );
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    /** Retourne int[7] : nb de tâches ayant une due_date par jour [Lun, Mar, Mer, Jeu, Ven, Sam, Dim] */
    public int[] getTasksDueByDayOfWeek(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int[] counts = new int[7];
        String sql = "SELECT strftime('%w', t." + PlanexiaDatabaseHelper.C_DUE_DATE + "), COUNT(*)" +
                " FROM " + PlanexiaDatabaseHelper.T_TASKS + " t" +
                " JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID +
                " JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID +
                " WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ?" +
                " AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " IS NOT NULL AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " != ''" +
                " GROUP BY strftime('%w', t." + PlanexiaDatabaseHelper.C_DUE_DATE + ");";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            int dow = c.getInt(0); // 0=Dim, 1=Lun, ..., 6=Sam
            int count = c.getInt(1);
            int idx = (dow == 0) ? 6 : dow - 1; // Lun=0, ..., Dim=6
            counts[idx] = count;
        }
        c.close();
        return counts;
    }

    /**
     * Retourne pour chaque module : [total tasks, done tasks]
     * Utilisé par la page Progression pour afficher la barre par matière.
     */
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
}