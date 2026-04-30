package com.example.planexia.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlanexiaDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "planexia.db";
    public static final int DB_VERSION = 4;

    // Table names (contrat)
    public static final String T_USERS = "users";
    public static final String T_MODULES = "modules";
    public static final String T_OBJECTIVES = "objectives";
    public static final String T_TASKS = "tasks";

    // Column names (contrat)
    public static final String C_ID = "id";

    public static final String C_EMAIL = "email";
    public static final String C_PASSWORD_HASH = "password_hash";
    public static final String C_IS_PREMIUM = "is_premium";

    public static final String C_USER_ID = "user_id";
    public static final String C_NAME = "name";

    public static final String C_MODULE_ID = "module_id";
    public static final String C_TITLE = "title";
    public static final String C_DUE_DATE = "due_date"; // TEXT YYYY-MM-DD

    public static final String C_OBJECTIVE_ID = "objective_id";
    public static final String C_IS_DONE = "is_done"; // 0/1
    public static final String C_RESOURCE_TEXT = "resource_text";
    public static final String C_COEFFICIENT = "coefficient";
    public static final String C_COLOR = "color";
    public static final String C_PSEUDO = "pseudo";
    public static final String C_FILIERE = "filiere";
    public static final String C_ANNEE = "annee";
    public PlanexiaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Foreign keys ON (SQLite)
        db.setForeignKeyConstraintsEnabled(true);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_EMAIL + " TEXT NOT NULL UNIQUE," +
                        C_PASSWORD_HASH + " TEXT NOT NULL," +
                        C_IS_PREMIUM + " INTEGER NOT NULL DEFAULT 0," +
                        C_PSEUDO + " TEXT," +
                        C_FILIERE + " TEXT," +
                        C_ANNEE + " TEXT" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + T_MODULES + " (" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_USER_ID + " INTEGER NOT NULL," +
                        C_NAME + " TEXT NOT NULL," +
                        C_COEFFICIENT + " INTEGER NOT NULL," +
                        C_COLOR + " TEXT NOT NULL," +
                        "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_ID + ") ON DELETE CASCADE" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + T_OBJECTIVES + " (" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_MODULE_ID + " INTEGER NOT NULL," +
                        C_TITLE + " TEXT NOT NULL," +
                        C_DUE_DATE + " TEXT," + // YYYY-MM-DD
                        "FOREIGN KEY(" + C_MODULE_ID + ") REFERENCES " + T_MODULES + "(" + C_ID + ") ON DELETE CASCADE" +
                        ");"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + T_TASKS + " (" +
                        C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        C_OBJECTIVE_ID + " INTEGER NOT NULL," +
                        C_TITLE + " TEXT NOT NULL," +
                        C_IS_DONE + " INTEGER NOT NULL DEFAULT 0," +
                        C_DUE_DATE + " TEXT," + // YYYY-MM-DD
                        C_RESOURCE_TEXT + " TEXT," +
                        "FOREIGN KEY(" + C_OBJECTIVE_ID + ") REFERENCES " + T_OBJECTIVES + "(" + C_ID + ") ON DELETE CASCADE" +
                        ");"
        );

        // Indexes (perf + planning)
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_modules_user_id ON " + T_MODULES + "(" + C_USER_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_objectives_module_id ON " + T_OBJECTIVES + "(" + C_MODULE_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_objective_id ON " + T_TASKS + "(" + C_OBJECTIVE_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON " + T_TASKS + "(" + C_DUE_DATE + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // MVP: reset
        db.execSQL("DROP TABLE IF EXISTS " + T_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_OBJECTIVES);
        db.execSQL("DROP TABLE IF EXISTS " + T_MODULES);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }
}