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

public class PremiumDialog {

    public interface OnPremiumActivatedListener {
        void onPremiumActivated();
    }

    public static void show(Context context, OnPremiumActivatedListener listener) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_premium);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        ImageButton btnClose = dialog.findViewById(R.id.btnClosePremium);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        Button btnMonthly = dialog.findViewById(R.id.btnSubscribeMonthly);
        if (btnMonthly != null) btnMonthly.setOnClickListener(v -> activatePremium(context, listener, dialog));

        Button btnYearly = dialog.findViewById(R.id.btnSubscribeYearly);
        if (btnYearly != null) btnYearly.setOnClickListener(v -> activatePremium(context, listener, dialog));

        dialog.show();
    }

    private static void activatePremium(Context context, OnPremiumActivatedListener listener, Dialog dialog) {
        SharedPreferences prefs = context.getSharedPreferences("planexia_session", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);

        if (userId != -1) {
            PlanexiaRepository repo = new PlanexiaRepository(context);
            repo.setPremium(userId, true);
        }

        prefs.edit().putBoolean("is_premium", true).apply();

        Toast.makeText(context, "🎉 Premium activé !", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        if (listener != null) listener.onPremiumActivated();
    }
}
