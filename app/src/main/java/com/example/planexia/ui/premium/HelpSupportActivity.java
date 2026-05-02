package com.example.planexia.ui.premium;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planexia.R;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupFaq(R.id.faq1Header, R.id.faq1Content, R.id.faq1Arrow);
        setupFaq(R.id.faq2Header, R.id.faq2Content, R.id.faq2Arrow);
        setupFaq(R.id.faq3Header, R.id.faq3Content, R.id.faq3Arrow);
        setupFaq(R.id.faq4Header, R.id.faq4Content, R.id.faq4Arrow);

        findViewById(R.id.btnEmail).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@planexia.app"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Planexia");
            startActivity(Intent.createChooser(intent, "Envoyer un e-mail"));
        });
    }

    private void setupFaq(int headerId, int contentId, int arrowId) {
        View header    = findViewById(headerId);
        View content   = findViewById(contentId);
        TextView arrow = findViewById(arrowId);
        header.setOnClickListener(v -> {
            boolean visible = content.getVisibility() == View.VISIBLE;
            content.setVisibility(visible ? View.GONE : View.VISIBLE);
            arrow.setText(visible ? "▼" : "▲");
        });
    }
}