package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class BuildModeActivity extends BaseActivity {

    private RadioButton radioAI, radioScratch;
    private String selectedMode = "ai"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_mode);

        radioAI      = findViewById(R.id.radioAI);
        radioScratch = findViewById(R.id.radioScratch);

        int budget = getIntent().getIntExtra("budget", 500);

        // Card tap toggles radio + sets mode
        findViewById(R.id.cardModeAI).setOnClickListener(v -> selectMode("ai"));
        findViewById(R.id.cardModeScratch).setOnClickListener(v -> selectMode("scratch"));

        findViewById(R.id.btnBuildModeNext).setOnClickListener(v -> {

            boolean isGuest = getIntent().getBooleanExtra("isGuest", false);

            if (selectedMode.equals("scratch")) {
                Intent intent = new Intent(BuildModeActivity.this, ScratchBuildActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("isGuest", isGuest);
                startActivity(intent);

            } else {
                // "ai" — Smart Generated Build
                Intent intent = new Intent(BuildModeActivity.this, AIQuestionnaireActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("isGuest", isGuest);
                startActivity(intent);
            }
        });
    }

    private void selectMode(String mode) {
        selectedMode = mode;
        radioAI.setChecked(mode.equals("ai"));
        radioScratch.setChecked(mode.equals("scratch"));
    }
}