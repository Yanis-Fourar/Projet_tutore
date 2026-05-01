package com.example.planexia.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;

/**
 * Dialog Premium partagé entre tous les écrans.
 * Usage :
 *   PremiumDialog.show(this, () -> {
 *       // callback exécuté après activation premium
 *   });
 */
public class PremiumDialog {

    public interface OnPremiumActivated {
        void onActivated();
    }

    public static void show(Context context, OnPremiumActivated callback) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92),
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        View btnMonthly = dialog.findViewById(R.id.btnMonthly);
        View btnYearly  = dialog.findViewById(R.id.btnYearly);
        View btnCancel  = dialog.findViewById(R.id.btnCancel);

        View.OnClickListener activateListener = v -> {
            activatePremium(context);
            dialog.dismiss();
            if (callback != null) callback.onActivated();
        };

        btnMonthly.setOnClickListener(activateListener);
        btnYearly.setOnClickListener(activateListener);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void activatePremium(Context context) {
        // 1. Mise à jour en base de données
        SharedPreferences prefs = context.getSharedPreferences("planexia_prefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId != -1) {
            PlanexiaRepository repository = new PlanexiaRepository(context);
            repository.setPremium(userId, true);
        }

        // 2. Sauvegarde dans SharedPreferences (accès rapide sans BDD)
        prefs.edit().putBoolean("is_premium", true).apply();
    }
}
