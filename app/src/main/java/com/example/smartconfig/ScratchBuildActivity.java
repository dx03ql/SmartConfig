package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ScratchBuildActivity extends AppCompatActivity {

    private static final int REQ_PARTS = 100;

    private int budget;
    private boolean isGuest;

    private final Map<String, Part> selectedParts = new HashMap<>();

    private TextView tvTotalPrice, tvWattage, tvScore, tvBadgeCount, tvLastUpdated;
    private LinearLayout llPartsContainer;

    private static final String[][] COMPONENTS = {
            {"case",    "Case",         "🖥"},
            {"cpu",     "CPU",          "🔲"},
            {"mobo",    "Motherboard",  "🟧"},
            {"gpu",     "GPU",          "🟪"},
            {"ram",     "RAM",          "🟦"},
            {"psu",     "Power Supply", "⚡"},
            {"storage", "Storage",      "💾"},
            {"cooling", "CPU Cooler",   "❄"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scratch_build);

        budget  = getIntent().getIntExtra("budget", 500);
        isGuest = getIntent().getBooleanExtra("isGuest", false);

        tvTotalPrice     = findViewById(R.id.tvTotalPrice);
        tvWattage        = findViewById(R.id.tvWattage);
        tvScore          = findViewById(R.id.tv3DMarkScore);
        tvBadgeCount     = findViewById(R.id.tvBadgeCount);
        tvLastUpdated    = findViewById(R.id.tvLastUpdated);
        llPartsContainer = findViewById(R.id.llPartsContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFinalizeBuild).setOnClickListener(v -> finalizeBuild());

        // --- Bottom nav ---
        findViewById(R.id.navBuild).setOnClickListener(v -> {
            // Already here — do nothing
        });

        findViewById(R.id.navCompare).setOnClickListener(v ->
                Toast.makeText(this, "Compare — coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.navCommunity).setOnClickListener(v ->
                Toast.makeText(this, "Community — coming soon", Toast.LENGTH_SHORT).show()
        );

        // Settings and Profile are now merged into AccountActivity
        findViewById(R.id.navSettings).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        renderPartRows();
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh parts and stats every time we come back (e.g. from PartsListActivity)
        renderPartRows();
        updateStats();
    }

    private void renderPartRows() {
        llPartsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (String[] comp : COMPONENTS) {
            String catId    = comp[0];
            String catLabel = comp[1];
            String catIcon  = comp[2];

            View row = inflater.inflate(R.layout.item_part_row, llPartsContainer, false);

            TextView tvIcon      = row.findViewById(R.id.tvPartRowIcon);
            TextView tvName      = row.findViewById(R.id.tvPartRowName);
            TextView tvAdded     = row.findViewById(R.id.tvPartRowAdded);
            TextView tvPartName  = row.findViewById(R.id.tvSelectedPartName);
            TextView tvPartPrice = row.findViewById(R.id.tvSelectedPartPrice);
            View previewView     = row.findViewById(R.id.layoutSelectedPreview);

            tvIcon.setText(catIcon);
            tvName.setText(catLabel);

            Part selected = selectedParts.get(catId);
            if (selected != null) {
                tvAdded.setVisibility(View.VISIBLE);
                tvAdded.setText("Added");
                previewView.setVisibility(View.VISIBLE);
                tvPartName.setText(selected.name.length() > 48
                        ? selected.name.substring(0, 48) + "…"
                        : selected.name);
                tvPartPrice.setText(String.format("€%.2f", selected.price));
            } else {
                tvAdded.setVisibility(View.GONE);
                previewView.setVisibility(View.GONE);
            }

            row.setOnClickListener(v -> openPartsScreen(catId, catLabel));
            llPartsContainer.addView(row);
        }
    }

    private void openPartsScreen(String catId, String catLabel) {
        Intent intent = new Intent(this, PartsListActivity.class);
        intent.putExtra("catId", catId);
        intent.putExtra("catLabel", catLabel);
        intent.putExtra("budget", budget);
        intent.putExtra("isGuest", isGuest);

        Part sel = selectedParts.get(catId);
        if (sel != null) intent.putExtra("selectedPartId", sel.id);

        startActivityForResult(intent, REQ_PARTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PARTS && resultCode == RESULT_OK && data != null) {
            String catId     = data.getStringExtra("catId");
            String partId    = data.getStringExtra("partId");
            String partName  = data.getStringExtra("partName");
            double partPrice = data.getDoubleExtra("partPrice", 0);
            int    partWatts = data.getIntExtra("partWatts", 0);
            int    partScore = data.getIntExtra("partScore", 0);
            boolean removed  = data.getBooleanExtra("removed", false);

            if (removed) {
                selectedParts.remove(catId);
            } else if (catId != null && partId != null) {
                selectedParts.put(catId, new Part(partId, partName, partPrice, partWatts, partScore));
            }

            renderPartRows();
            updateStats();
        }
    }

    private void updateStats() {
        double total = 0; int watts = 0, score = 0, count = 0;
        for (Part p : selectedParts.values()) {
            total += p.price;
            watts += p.watts;
            if (p.score > score) score = p.score;
            count++;
        }

        tvTotalPrice.setText(String.format("€%.2f", total));
        tvWattage.setText(watts + "W");
        tvScore.setText(score > 0 ? String.valueOf(score) : "N/A");
        tvBadgeCount.setText(String.valueOf(count));
        tvLastUpdated.setText("Last updated just now");

        TextView tvFinalizeLabel = findViewById(R.id.tvFinalizeLabel);
        tvFinalizeLabel.setText(count >= 4
                ? "🚀  Finalize Build"
                : "Generate Parts Summary (" + count + "/8 added)");
    }

    private void finalizeBuild() {
        try {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, Part> entry : selectedParts.entrySet()) {
                JSONObject partObj = new JSONObject();
                partObj.put("name",  entry.getValue().name);
                partObj.put("price", entry.getValue().price);
                json.put(entry.getKey(), partObj);
            }
            Intent intent = new Intent(this, GeneratingBuildActivity.class);
            intent.putExtra("budget",    budget);
            intent.putExtra("buildMode", "scratch");
            intent.putExtra("isGuest",   isGuest);
            intent.putExtra("partsJson", json.toString());
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static class Part {
        public String id, name;
        public double price;
        public int watts, score;

        public Part(String id, String name, double price, int watts, int score) {
            this.id    = id;
            this.name  = name;
            this.price = price;
            this.watts = watts;
            this.score = score;
        }
    }
}