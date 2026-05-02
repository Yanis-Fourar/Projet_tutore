package com.example.planexia.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlanexiaDatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME    = "planexia.db";
    public static final int    DB_VERSION = 5; // ← incrémenté pour créer la nouvelle table

    // Tables
    public static final String T_USERS           = "users";
    public static final String T_MODULES         = "modules";
    public static final String T_OBJECTIVES      = "objectives";
    public static final String T_TASKS           = "tasks";
    public static final String T_CHRONO_SESSIONS = "chrono_sessions"; // ← nouvelle

    // Colonnes communes
    public static final String C_ID            = "id";
    public static final String C_USER_ID       = "user_id";
    public static final String C_MODULE_ID     = "module_id";
    public static final String C_OBJECTIVE_ID  = "objective_id";
    public static final String C_TITLE         = "title";
    public static final String C_DUE_DATE      = "due_date";
    public static final String C_IS_DONE       = "is_done";
    public static final String C_RESOURCE_TEXT = "resource_text";
    public static final String C_COEFFICIENT   = "coefficient";
    public static final String C_COLOR         = "color";
    public static final String C_NAME          = "name";

    // Users
    public static final String C_EMAIL         = "email";
    public static final String C_PASSWORD_HASH = "password_hash";
    public static final String C_IS_PREMIUM    = "is_premium";
    public static final String C_PSEUDO        = "pseudo";
    public static final String C_FILIERE       = "filiere";
    public static final String C_ANNEE         = "annee";

    // Chrono sessions
    public static final String C_TASK_LABEL   = "task_label";   // nom de la tâche (texte libre)
    public static final String C_DURATION_MS  = "duration_ms";  // durée en millisecondes
    public static final String C_GOAL_MIN     = "goal_min";     // objectif en minutes
    public static final String C_CREATED_AT   = "created_at";   // timestamp Unix (ms)

    public PlanexiaDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_USERS + " (" +
                C_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_EMAIL         + " TEXT NOT NULL UNIQUE," +
                C_PASSWORD_HASH + " TEXT NOT NULL," +
                C_IS_PREMIUM    + " INTEGER NOT NULL DEFAULT 0," +
                C_PSEUDO        + " TEXT," +
                C_FILIERE       + " TEXT," +
                C_ANNEE         + " TEXT" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_MODULES + " (" +
                C_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_USER_ID     + " INTEGER NOT NULL," +
                C_NAME        + " TEXT NOT NULL," +
                C_COEFFICIENT + " INTEGER NOT NULL," +
                C_COLOR       + " TEXT NOT NULL," +
                "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_ID + ") ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_OBJECTIVES + " (" +
                C_ID        + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_MODULE_ID + " INTEGER NOT NULL," +
                C_TITLE     + " TEXT NOT NULL," +
                C_DUE_DATE  + " TEXT," +
                "FOREIGN KEY(" + C_MODULE_ID + ") REFERENCES " + T_MODULES + "(" + C_ID + ") ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_TASKS + " (" +
                C_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_OBJECTIVE_ID + " INTEGER NOT NULL," +
                C_TITLE        + " TEXT NOT NULL," +
                C_IS_DONE      + " INTEGER NOT NULL DEFAULT 0," +
                C_DUE_DATE     + " TEXT," +
                C_RESOURCE_TEXT + " TEXT," +
                "FOREIGN KEY(" + C_OBJECTIVE_ID + ") REFERENCES " + T_OBJECTIVES + "(" + C_ID + ") ON DELETE CASCADE" +
                ");");

        // ── Nouvelle table : sessions chrono ──────────────────────
        db.execSQL("CREATE TABLE IF NOT EXISTS " + T_CHRONO_SESSIONS + " (" +
                C_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                C_USER_ID    + " INTEGER NOT NULL," +
                C_TASK_LABEL + " TEXT NOT NULL," +
                C_DURATION_MS + " INTEGER NOT NULL," +
                C_GOAL_MIN   + " INTEGER NOT NULL DEFAULT 0," +
                C_CREATED_AT + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_ID + ") ON DELETE CASCADE" +
                ");");

        // Index
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_modules_user_id     ON " + T_MODULES    + "(" + C_USER_ID   + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_objectives_module   ON " + T_OBJECTIVES + "(" + C_MODULE_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_objective     ON " + T_TASKS      + "(" + C_OBJECTIVE_ID + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_tasks_due_date      ON " + T_TASKS      + "(" + C_DUE_DATE  + ");");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_chrono_user_date    ON " + T_CHRONO_SESSIONS + "(" + C_USER_ID + "," + C_CREATED_AT + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            // Juste ajouter la table sans tout supprimer
            db.execSQL("CREATE TABLE IF NOT EXISTS " + T_CHRONO_SESSIONS + " (" +
                    C_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    C_USER_ID     + " INTEGER NOT NULL," +
                    C_TASK_LABEL  + " TEXT NOT NULL," +
                    C_DURATION_MS + " INTEGER NOT NULL," +
                    C_GOAL_MIN    + " INTEGER NOT NULL DEFAULT 0," +
                    C_CREATED_AT  + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + C_USER_ID + ") REFERENCES " + T_USERS + "(" + C_ID + ") ON DELETE CASCADE" +
                    ");");
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_chrono_user_date ON " + T_CHRONO_SESSIONS + "(" + C_USER_ID + "," + C_CREATED_AT + ");");
        }
    }
}