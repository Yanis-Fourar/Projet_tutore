package com.example.planexia.ui.premium;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.planexia.R;
import com.example.planexia.data.PlanexiaRepository;
import com.example.planexia.data.SessionManager;
import com.example.planexia.model.Module;

import java.util.List;

public class PremiumStatsActivity extends AppCompatActivity {

    private PlanexiaRepository repository;
    private long userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_stats);

        // Utiliser SessionManager comme ta version (planexia_session)
        userId = new SessionManager(this).getUserId();
        repository = new PlanexiaRepository(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadData();
    }

    private void loadData() {
        if (userId == -1) return;

        int globalProgress  = repository.getGlobalProgress(userId);
        int doneTasks       = repository.getCompletedTasksCount(userId);
        int pendingTasks    = repository.getPendingTasksCount(userId);
        int totalObjectives = repository.getTotalObjectivesCount(userId);

        setText(R.id.tvCompletionRate,  globalProgress + "%");
        setText(R.id.tvDoneCount,       String.valueOf(doneTasks));
        setText(R.id.tvPendingCount,    String.valueOf(pendingTasks));
        setText(R.id.tvObjectivesCount, String.valueOf(totalObjectives));

        buildModuleCards();
        buildBarChart();
    }

    private void buildModuleCards() {
        LinearLayout container = findViewById(R.id.containerModulesDetail);
        if (container == null) return;
        container.removeAllViews();

        List<Module> modules = repository.getModulesByUser(userId);
        for (Module module : modules) {
            int[] prog   = repository.getProgressForModule(module.getId());
            int total    = prog[0];
            int done     = prog[1];
            int pct      = total == 0 ? 0 : (int) Math.round((done * 100.0) / total);
            int objCount = repository.getObjectivesCountForModule(module.getId());
            int color    = parseColor(module.getColor(), "#7B3FF2");

            CardView card = new CardView(this);
            CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(10);
            card.setLayoutParams(cardParams);
            card.setRadius(dp(14));
            card.setCardElevation(dp(2));
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setPadding(dp(14), dp(14), dp(14), dp(14));

            // Row 1 : dot + nom + coef
            LinearLayout row1 = new LinearLayout(this);
            row1.setOrientation(LinearLayout.HORIZONTAL);
            row1.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row1.setPadding(0, 0, 0, dp(6));

            View dot = new View(this);
            LinearLayout.LayoutParams dotP = new LinearLayout.LayoutParams(dp(12), dp(12));
            dotP.rightMargin = dp(8);
            dot.setLayoutParams(dotP);
            dot.setBackground(getDrawable(R.drawable.circle_dot_purple));
            dot.setBackgroundTintList(ColorStateList.valueOf(color));

            TextView tvName = new TextView(this);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvName.setText(module.getName());
            tvName.setTextSize(15);
            tvName.setTextColor(Color.parseColor("#1F1F1F"));
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvCoef = new TextView(this);
            tvCoef.setText("Coef. " + module.getCoefficient());
            tvCoef.setTextSize(11);
            tvCoef.setTextColor(Color.WHITE);
            tvCoef.setBackground(getDrawable(R.drawable.bg_avatar_circle));
            tvCoef.setBackgroundTintList(ColorStateList.valueOf(color));
            tvCoef.setPadding(dp(8), dp(3), dp(8), dp(3));

            row1.addView(dot);
            row1.addView(tvName);
            row1.addView(tvCoef);

            // Row 2 : stats + %
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setPadding(0, 0, 0, dp(8));

            TextView tvStats = new TextView(this);
            tvStats.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvStats.setText(done + "/" + total + " tâches  •  " + objCount + " objectif" + (objCount > 1 ? "s" : ""));
            tvStats.setTextSize(12);
            tvStats.setTextColor(Color.parseColor("#666666"));

            TextView tvPct = new TextView(this);
            tvPct.setText(pct + "%");
            tvPct.setTextSize(14);
            tvPct.setTextColor(color);
            tvPct.setTypeface(null, android.graphics.Typeface.BOLD);

            row2.addView(tvStats);
            row2.addView(tvPct);

            // Barre de progression
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(8)));
            pb.setMax(100);
            pb.setProgress(pct);
            pb.setProgressTintList(ColorStateList.valueOf(color));
            pb.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEE8FA")));

            inner.addView(row1);
            inner.addView(row2);
            inner.addView(pb);
            card.addView(inner);
            container.addView(card);
        }
    }

    private void buildBarChart() {
        LinearLayout container = findViewById(R.id.containerBarChart);
        if (container == null) return;
        container.removeAllViews();

        int[] dayCounts = repository.getTasksDueByDayOfWeek(userId);

        int maxVal = 1;
        for (int v : dayCounts) if (v > maxVal) maxVal = v;

        int chartHeightPx = dp(120);
        int barColor      = Color.parseColor("#7B3FF2");
        int barColorWknd  = Color.parseColor("#9B6FE8");
        int barColorEmpty = Color.parseColor("#E8E0F5");

        for (int i = 0; i < 7; i++) {
            LinearLayout col = new LinearLayout(this);
            LinearLayout.LayoutParams colP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            colP.leftMargin  = dp(2);
            colP.rightMargin = dp(2);
            col.setLayoutParams(colP);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

            TextView tvCount = new TextView(this);
            tvCount.setText(dayCounts[i] > 0 ? String.valueOf(dayCounts[i]) : "");
            tvCount.setTextSize(10);
            tvCount.setTextColor(Color.parseColor("#6C3FC5"));
            tvCount.setGravity(android.view.Gravity.CENTER);

            int barHeightPx = dayCounts[i] == 0 ? dp(3)
                    : Math.max(dp(6), (int) ((dayCounts[i] * 1.0 / maxVal) * (chartHeightPx - dp(20))));

            View bar = new View(this);
            LinearLayout.LayoutParams barP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, barHeightPx);
            barP.topMargin = dp(2);
            bar.setLayoutParams(barP);
            bar.setBackground(getDrawable(R.drawable.bg_circle_purple));
            int chosenColor = dayCounts[i] == 0 ? barColorEmpty : (i >= 5 ? barColorWknd : barColor);
            bar.setBackgroundTintList(ColorStateList.valueOf(chosenColor));

            col.addView(tvCount);
            col.addView(bar);
            container.addView(col);
        }
    }

    private void setText(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(text);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private int parseColor(String hex, String fallback) {
        try { return Color.parseColor(hex); }
        catch (Exception e) { return Color.parseColor(fallback); }
    }
}