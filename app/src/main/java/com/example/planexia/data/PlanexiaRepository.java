package com.example.planexia.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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

    // Retourne userId ou -1
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
        if (c.moveToFirst()) {
            userId = c.getLong(0);
        }
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
            int id = c.getInt(0);
            String name = c.getString(1);
            int coefficient = c.getInt(2);
            String color = c.getString(3);

            modules.add(new Module(id, name, coefficient, color));
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
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
        }
        c.close();
        return ids;
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
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
        }
        c.close();
        return ids;
    }

    // ---------- PLANNING ----------
    public List<Long> getTasksForDate(long userId, String dateYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID + " " +
                        "FROM " + PlanexiaDatabaseHelper.T_TASKS + " t " +
                        "JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " " +
                        "JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID + " " +
                        "WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " = ?;";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), dateYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
        }
        c.close();
        return ids;
    }

    public List<Long> getOverdueTasks(long userId, String todayYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT t." + PlanexiaDatabaseHelper.C_ID + " " +
                        "FROM " + PlanexiaDatabaseHelper.T_TASKS + " t " +
                        "JOIN " + PlanexiaDatabaseHelper.T_OBJECTIVES + " o ON o." + PlanexiaDatabaseHelper.C_ID + " = t." + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " " +
                        "JOIN " + PlanexiaDatabaseHelper.T_MODULES + " m ON m." + PlanexiaDatabaseHelper.C_ID + " = o." + PlanexiaDatabaseHelper.C_MODULE_ID + " " +
                        "WHERE m." + PlanexiaDatabaseHelper.C_USER_ID + " = ? " +
                        "AND t." + PlanexiaDatabaseHelper.C_IS_DONE + " = 0 " +
                        "AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " IS NOT NULL " +
                        "AND t." + PlanexiaDatabaseHelper.C_DUE_DATE + " < ?;";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), todayYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
        }
        c.close();
        return ids;
    }

    public int getObjectiveProgress(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor totalC = db.rawQuery(
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS +
                        " WHERE " + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ?;",
                new String[]{String.valueOf(objectiveId)}
        );

        int total = 0;
        if (totalC.moveToFirst()) {
            total = totalC.getInt(0);
        }
        totalC.close();

        if (total == 0) {
            return 0;
        }

        Cursor doneC = db.rawQuery(
                "SELECT COUNT(*) FROM " + PlanexiaDatabaseHelper.T_TASKS +
                        " WHERE " + PlanexiaDatabaseHelper.C_OBJECTIVE_ID + " = ? " +
                        "AND " + PlanexiaDatabaseHelper.C_IS_DONE + " = 1;",
                new String[]{String.valueOf(objectiveId)}
        );

        int done = 0;
        if (doneC.moveToFirst()) {
            done = doneC.getInt(0);
        }
        doneC.close();

        return (int) Math.round((done * 100.0) / total);
    }
}