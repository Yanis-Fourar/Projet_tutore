package com.example.planexia.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

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

    public interface OnPremiumActivatedListener {
        void onPremiumActivated();
    }

    public static void show(Context context, OnPremiumActivatedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        // Bouton fermer (croix)
        ImageButton btnClose = dialog.findViewById(R.id.btnClosePremium);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        // Bouton s'abonner mensuel
        Button btnMonthly = dialog.findViewById(R.id.btnSubscribeMonthly);
        if (btnMonthly != null) btnMonthly.setOnClickListener(v -> activatePremium(context, listener, dialog));

        // Bouton s'abonner annuel
        Button btnYearly = dialog.findViewById(R.id.btnSubscribeYearly);
        if (btnYearly != null) btnYearly.setOnClickListener(v -> activatePremium(context, listener, dialog));

        dialog.show();
    }

    private static void activatePremium(Context context, OnPremiumActivatedListener listener, Dialog dialog) {
        // Clé "planexia_prefs" utilisée dans tout le projet
        SharedPreferences prefs = context.getSharedPreferences("planexia_prefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId != -1) {
            PlanexiaRepository repo = new PlanexiaRepository(context);
            repo.setPremium(userId, true);
        }

        // Sauvegarde en SharedPreferences pour accès rapide sans BDD
        prefs.edit().putBoolean("is_premium", true).apply();

        Toast.makeText(context, "🎉 Premium activé !", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        if (listener != null) listener.onPremiumActivated();
    }
}
