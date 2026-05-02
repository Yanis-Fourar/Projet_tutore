package com.example.planexia;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Application class : exécutée AVANT toute Activity au démarrage de l'app.
 * Sert à appliquer le mode sombre dès le boot pour que tous les écrans
 * s'affichent directement avec le bon thème.
 *
 * Sans cette classe, il faudrait redémarrer l'app après chaque changement
 * de mode pour que ça soit pris en compte partout.
 */
public class PlanexiaApp extends Application {

    public static final String PREFS_PARAM = "planexia_param";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_SON = "son_enabled";

    @Override
    public void onCreate() {
        super.onCreate();
        applyDarkMode();
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_PARAM, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
    }
}