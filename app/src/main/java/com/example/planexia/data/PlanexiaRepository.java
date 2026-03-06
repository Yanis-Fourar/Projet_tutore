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
        values.put("email", email);
        values.put("password_hash", password);
        values.put("is_premium", 0);
        return db.insert(PlanexiaDatabaseHelper.T_USERS, null, values);
    }

    // Retourne userId ou -1
    public long login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_USERS,
                new String[]{"id"},
                "email = ? AND password_hash = ?",
                new String[]{email, password},
                null, null, null
        );

        long userId = -1;
        if (c.moveToFirst()) userId = c.getLong(0);
        c.close();
        return userId;
    }

    // ---------- MODULES ----------
    public long addModule(long userId, String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("name", name);
        return db.insert(PlanexiaDatabaseHelper.T_MODULES, null, values);
    }

    public List<Long> getModulesByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_MODULES,
                new String[]{"id"},
                "user_id = ?",
                new String[]{String.valueOf(userId)},
                null, null, "id DESC"
        );

        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    // ---------- OBJECTIVES ----------
    public long addObjective(long moduleId, String title, String dueDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("module_id", moduleId);
        values.put("title", title);
        values.put("due_date", dueDate);
        return db.insert(PlanexiaDatabaseHelper.T_OBJECTIVES, null, values);
    }

    public List<Long> getObjectivesByModule(long moduleId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_OBJECTIVES,
                new String[]{"id"},
                "module_id = ?",
                new String[]{String.valueOf(moduleId)},
                null, null, "id DESC"
        );

        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    // ---------- TASKS ----------
    public long addTask(long objectiveId, String title, String dueDate, String resourceText) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("objective_id", objectiveId);
        values.put("title", title);
        values.put("is_done", 0);
        values.put("due_date", dueDate);
        values.put("resource_text", resourceText);
        return db.insert(PlanexiaDatabaseHelper.T_TASKS, null, values);
    }

    public int setTaskDone(long taskId, boolean done) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_done", done ? 1 : 0);
        return db.update(
                PlanexiaDatabaseHelper.T_TASKS,
                values,
                "id = ?",
                new String[]{String.valueOf(taskId)}
        );
    }

    public List<Long> getTasksByObjective(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                PlanexiaDatabaseHelper.T_TASKS,
                new String[]{"id"},
                "objective_id = ?",
                new String[]{String.valueOf(objectiveId)},
                null, null, "id DESC"
        );

        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    // ---------- PLANNING ----------
    public List<Long> getTasksForDate(long userId, String dateYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT t.id " +
                        "FROM tasks t " +
                        "JOIN objectives o ON o.id = t.objective_id " +
                        "JOIN modules m ON m.id = o.module_id " +
                        "WHERE m.user_id = ? AND t.due_date = ?;";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), dateYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    public List<Long> getOverdueTasks(long userId, String todayYYYYMMDD) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String sql =
                "SELECT t.id " +
                        "FROM tasks t " +
                        "JOIN objectives o ON o.id = t.objective_id " +
                        "JOIN modules m ON m.id = o.module_id " +
                        "WHERE m.user_id = ? AND t.is_done = 0 AND t.due_date IS NOT NULL AND t.due_date < ?;";

        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId), todayYYYYMMDD});
        List<Long> ids = new ArrayList<>();
        while (c.moveToNext()) ids.add(c.getLong(0));
        c.close();
        return ids;
    }

    public int getObjectiveProgress(long objectiveId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor totalC = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE objective_id = ?;",
                new String[]{String.valueOf(objectiveId)}
        );
        int total = 0;
        if (totalC.moveToFirst()) total = totalC.getInt(0);
        totalC.close();

        if (total == 0) return 0;

        Cursor doneC = db.rawQuery(
                "SELECT COUNT(*) FROM tasks WHERE objective_id = ? AND is_done = 1;",
                new String[]{String.valueOf(objectiveId)}
        );
        int done = 0;
        if (doneC.moveToFirst()) done = doneC.getInt(0);
        doneC.close();

        return (int) Math.round((done * 100.0) / total);
    }
}