package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

public class BuildModeActivity extends AppCompatActivity {

    private RadioButton radioAI, radioModify, radioScratch;
    private String selectedMode = "ai"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_mode);

        radioAI = findViewById(R.id.radioAI);
        radioModify = findViewById(R.id.radioModify);
        radioScratch = findViewById(R.id.radioScratch);

        int budget = getIntent().getIntExtra("budget", 500);

        // Card tap toggles radio + sets mode
        findViewById(R.id.cardModeAI).setOnClickListener(v -> selectMode("ai"));
        findViewById(R.id.cardModeModify).setOnClickListener(v -> selectMode("modify"));
        findViewById(R.id.cardModeScratch).setOnClickListener(v -> selectMode("scratch"));

        findViewById(R.id.btnBuildModeNext).setOnClickListener(v -> {

            boolean isGuest = getIntent().getBooleanExtra("isGuest", false);

            if (selectedMode.equals("scratch")) {
                Intent intent = new Intent(BuildModeActivity.this, ScratchBuildActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("isGuest", isGuest);
                startActivity(intent);

            } else if (selectedMode.equals("ai")) {
                Intent intent = new Intent(BuildModeActivity.this, AIQuestionnaireActivity.class);
                intent.putExtra("budget", budget);
                intent.putExtra("isGuest", isGuest);
                startActivity(intent);

            } else {
                // "modify" — template mode, implement later
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
        radioModify.setChecked(mode.equals("modify"));
        radioScratch.setChecked(mode.equals("scratch"));
    }
}
