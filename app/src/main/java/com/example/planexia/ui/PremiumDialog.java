package com.example.planexia.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;

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
        dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92f),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // Bouton fermer (croix)
        ImageButton btnClose = dialog.findViewById(R.id.btnClosePremium);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Bouton s'abonner mensuel
        Button btnMonthly = dialog.findViewById(R.id.btnSubscribeMonthly);
        btnMonthly.setOnClickListener(v -> {
            activatePremium(context, listener, dialog);
        });

        // Bouton s'abonner annuel
        Button btnYearly = dialog.findViewById(R.id.btnSubscribeYearly);
        btnYearly.setOnClickListener(v -> {
            activatePremium(context, listener, dialog);
        });

        dialog.show();
    }

    private static void activatePremium(Context context, OnPremiumActivatedListener listener, Dialog dialog) {
        // Simulation d'achat → activer Premium en DB
        SharedPreferences prefs = context.getSharedPreferences("planexia_session", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", 1);

        PlanexiaRepository repo = new PlanexiaRepository(context);
        repo.setPremium(userId, true);

        // Sauvegarder en SharedPreferences aussi
        prefs.edit().putBoolean("is_premium", true).apply();

        Toast.makeText(context, "🎉 Premium activé !", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        if (listener != null) listener.onPremiumActivated();
    }
}