package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetActivity extends BaseActivity {

    private TextView tvBudgetDisplay;
    private SeekBar seekBarBudget;

    private final int[] budgetValues = {200, 300, 400, 500, 600, 750, 1000, 1500, 2000, 5000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        tvBudgetDisplay = findViewById(R.id.tvBudgetDisplay);
        seekBarBudget = findViewById(R.id.seekBarBudget);

        // Show the starting value so the number matches the slider position.
        tvBudgetDisplay.setText("€" + budgetValues[seekBarBudget.getProgress()]);

        seekBarBudget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBudgetDisplay.setText("€" + budgetValues[progress]);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Quick-select cards
        int[] cardIds = {R.id.cardBudgetEntry, R.id.cardBudgetMid, R.id.cardBudgetHigh, R.id.cardBudgetEnthusiast};
        int[] seekPositions = {2, 5, 7, 9}; // maps to €400, €750, €1500, €5000

        for (int i = 0; i < cardIds.length; i++) {
            final int pos = seekPositions[i];
            findViewById(cardIds[i]).setOnClickListener(v -> {
                seekBarBudget.setProgress(pos);
                tvBudgetDisplay.setText("€" + budgetValues[pos]);
            });
        }

        findViewById(R.id.btnBudgetNext).setOnClickListener(v -> {
            int selectedBudget = budgetValues[seekBarBudget.getProgress()];
            Intent intent = new Intent(BudgetActivity.this, BuildModeActivity.class);
            intent.putExtra("budget", selectedBudget);
            startActivity(intent);
        });
    }
}