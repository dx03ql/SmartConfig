package com.example.smartconfig;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * ScratchBuildActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Uses the shared PartsCatalog as the single source of truth. Selected parts
 * are stored as PartsCatalog.Part (looked up by id), so the compatibility
 * engine reads explicit socket / ddr / form-factor / wattage fields — no more
 * string parsing.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class ScratchBuildActivity extends AppCompatActivity {

    private static final int REQ_PARTS = 100;

    private int budget;
    private boolean isGuest;

    private final Map<String, PartsCatalog.Part> selectedParts = new HashMap<>();

    private TextView tvTotalPrice, tvWattage, tvBadgeCount, tvLastUpdated;
    private TextView tvCompatPercent, tvCompatStatus;
    private LinearLayout llPartsContainer, llCompatContainer;

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

        tvTotalPrice      = findViewById(R.id.tvTotalPrice);
        tvWattage         = findViewById(R.id.tvWattage);
        tvBadgeCount      = findViewById(R.id.tvBadgeCount);
        tvLastUpdated     = findViewById(R.id.tvLastUpdated);
        tvCompatPercent   = findViewById(R.id.tvCompatPercent);
        tvCompatStatus    = findViewById(R.id.tvCompatStatus);
        llPartsContainer  = findViewById(R.id.llPartsContainer);
        llCompatContainer = findViewById(R.id.llCompatContainer);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFinalizeBuild).setOnClickListener(v -> finalizeBuild());
        findViewById(R.id.btnSaveBuild).setOnClickListener(v -> showSaveDialog());

        findViewById(R.id.navBuild).setOnClickListener(v -> { /* already here */ });
        findViewById(R.id.navCompare).setOnClickListener(v ->
                Toast.makeText(this, "Compare — coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navCommunity).setOnClickListener(v ->
                Toast.makeText(this, "Community — coming soon", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navSettings).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));

        renderPartRows();
        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

            PartsCatalog.Part selected = selectedParts.get(catId);
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

        // Money already spent on OTHER categories (exclude this category's current pick,
        // so swapping within a category compares fairly).
        double spentElsewhere = 0;
        PartsCatalog.Part sel = selectedParts.get(catId);
        for (Map.Entry<String, PartsCatalog.Part> e : selectedParts.entrySet()) {
            if (!e.getKey().equals(catId)) spentElsewhere += e.getValue().price;
        }
        double remaining = budget - spentElsewhere; // budget available for this category

        intent.putExtra("remainingBudget", remaining);
        if (sel != null) intent.putExtra("selectedPartId", sel.id);

        startActivityForResult(intent, REQ_PARTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PARTS && resultCode == RESULT_OK && data != null) {
            String catId    = data.getStringExtra("catId");
            String partId   = data.getStringExtra("partId");
            boolean removed = data.getBooleanExtra("removed", false);

            if (removed) {
                selectedParts.remove(catId);
            } else if (catId != null && partId != null) {
                PartsCatalog.Part p = PartsRepository.findById(partId);
                if (p != null) selectedParts.put(catId, p);
            }

            renderPartRows();
            updateStats();
        }
    }

    private void updateStats() {
        double total = 0;
        int    draw  = 0;   // power draw excludes the PSU (its watts = capacity)
        int    count = 0;

        for (PartsCatalog.Part p : selectedParts.values()) {
            total += p.price;
            if (!"psu".equals(p.category)) draw += p.watts;
            count++;
        }

        tvTotalPrice.setText(String.format("€%.2f", total));
        tvWattage.setText(draw + "W");
        tvBadgeCount.setText(String.valueOf(count));
        tvLastUpdated.setText("Last updated just now");

        TextView tvFinalizeLabel = findViewById(R.id.tvFinalizeLabel);
        tvFinalizeLabel.setText(count >= 4
                ? "🚀  Finalize Build"
                : "Generate Parts Summary (" + count + "/8 added)");

        renderCompatibility();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  COMPATIBILITY ENGINE — uses PartsCatalog's explicit fields
    // ══════════════════════════════════════════════════════════════════════
    private void renderCompatibility() {
        llCompatContainer.removeAllViews();

        if (selectedParts.isEmpty()) {
            tvCompatPercent.setText("—");
            tvCompatPercent.setTextColor(Color.parseColor("#9E9E9E"));
            tvCompatStatus.setText("Add parts to check compatibility");
            return;
        }

        PartsCatalog.Part cpu     = selectedParts.get("cpu");
        PartsCatalog.Part ram     = selectedParts.get("ram");
        PartsCatalog.Part mobo    = selectedParts.get("mobo");
        PartsCatalog.Part psu     = selectedParts.get("psu");
        PartsCatalog.Part pcase   = selectedParts.get("case");

        int draw = 0;
        for (PartsCatalog.Part p : selectedParts.values())
            if (!"psu".equals(p.category)) draw += p.watts;

        Boolean chkCpuMobo  = (cpu != null && mobo != null)
                ? eq(cpu.socket, mobo.socket) : null;
        Boolean chkRamMobo  = (ram != null && mobo != null)
                ? eq(ram.ddr, mobo.ddr) : null;
        Boolean chkCaseMobo = (pcase != null && mobo != null)
                ? (formRank(pcase.formFactor) >= formRank(mobo.formFactor)) : null;
        Boolean chkPsu      = (psu != null && draw > 0)
                ? (psu.watts >= draw) : null;

        int passed = 0, totalChecks = 0;
        Boolean[] all = { chkCpuMobo, chkRamMobo, chkCaseMobo, chkPsu };
        for (Boolean b : all) { if (b != null) { totalChecks++; if (b) passed++; } }

        int overall;
        if (totalChecks == 0) {
            overall = 100;
            tvCompatStatus.setText("No conflicts yet");
        } else {
            overall = Math.round(passed * 100f / totalChecks);
            int issues = totalChecks - passed;
            tvCompatStatus.setText(issues == 0
                    ? "All parts compatible"
                    : issues + (issues > 1 ? " compatibility issues found" : " compatibility issue found"));
        }
        tvCompatPercent.setText(overall + "%");
        tvCompatPercent.setTextColor(percentColor(overall));

        for (String[] comp : COMPONENTS) {
            String cat = comp[0];
            PartsCatalog.Part part = selectedParts.get(cat);
            if (part == null) continue;

            int[] pt = perPartScore(cat, chkCpuMobo, chkRamMobo, chkCaseMobo, chkPsu);
            int pPass = pt[0], pTotal = pt[1];
            int pct = (pTotal == 0) ? 100 : Math.round(pPass * 100f / pTotal);

            llCompatContainer.addView(buildCompatRow(comp[2], part.name, pct, pTotal == 0));
        }
    }

    private int[] perPartScore(String cat, Boolean cpuMobo, Boolean ramMobo,
                               Boolean caseMobo, Boolean psu) {
        int pass = 0, total = 0;
        switch (cat) {
            case "cpu":
                total += add(cpuMobo);  pass += yes(cpuMobo);
                break;
            case "mobo":
                total += add(cpuMobo);  pass += yes(cpuMobo);
                total += add(ramMobo);  pass += yes(ramMobo);
                total += add(caseMobo); pass += yes(caseMobo);
                break;
            case "ram":
                total += add(ramMobo);  pass += yes(ramMobo);
                break;
            case "case":
                total += add(caseMobo); pass += yes(caseMobo);
                break;
            case "psu":
                total += add(psu);      pass += yes(psu);
                break;
            default: // gpu, storage, cooling — no modeled constraints
                break;
        }
        return new int[]{ pass, total };
    }

    private int add(Boolean b) { return b == null ? 0 : 1; }
    private int yes(Boolean b) { return (b != null && b) ? 1 : 0; }

    private boolean eq(String a, String b) {
        if (a == null || b == null) return true; // unknown → don't flag
        return a.equals(b);
    }

    private int formRank(String f) {
        if (f == null) return 3; // assume ATX
        switch (f) {
            case "ITX":  return 1;
            case "MATX": return 2;
            case "ATX":  return 3;
            case "EATX": return 4;
            default:     return 3;
        }
    }

    private View buildCompatRow(String emoji, String partName, int pct, boolean noChecks) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(8);
        row.setLayoutParams(rowLp);

        TextView tvEmoji = new TextView(this);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(15);
        LinearLayout.LayoutParams emojiLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emojiLp.rightMargin = dp(10);
        tvEmoji.setLayoutParams(emojiLp);

        TextView tvName = new TextView(this);
        String shortName = partName != null && partName.length() > 30
                ? partName.substring(0, 30) + "…" : partName;
        tvName.setText(shortName);
        tvName.setTextColor(Color.parseColor("#CBD5E1"));
        tvName.setTextSize(13);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvName.setLayoutParams(nameLp);

        TextView tvPct = new TextView(this);
        tvPct.setText(noChecks ? "—" : pct + "%");
        tvPct.setTextColor(noChecks ? Color.parseColor("#616161") : percentColor(pct));
        tvPct.setTextSize(13);
        tvPct.setTypeface(tvPct.getTypeface(), Typeface.BOLD);

        row.addView(tvEmoji);
        row.addView(tvName);
        row.addView(tvPct);
        return row;
    }

    private int percentColor(int pct) {
        if (pct >= 90) return Color.parseColor("#22C55E");
        if (pct >= 70) return Color.parseColor("#F59E0B");
        return Color.parseColor("#DC3A3A");
    }

    private void finalizeBuild() {
        try {
            JSONObject json = new JSONObject();
            for (Map.Entry<String, PartsCatalog.Part> entry : selectedParts.entrySet()) {
                json.put(entry.getKey(), entry.getValue().id);   // {category: partId}
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

    // ── Save the current scratch build (name dialog → SQLite) ────────────────
    private void showSaveDialog() {
        if (selectedParts.isEmpty()) {
            Toast.makeText(this, "Add some parts first.", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g. My Gaming Build");
        input.setText("Build " + (BuildsRepository.getBuildCount() + 1));
        int pad = dp(20);
        input.setPadding(pad, pad, pad, pad);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Name your build")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> saveBuild(input.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveBuild(String name) {
        try {
            JSONObject json = new JSONObject();
            double total = 0;
            int count = 0;
            for (Map.Entry<String, PartsCatalog.Part> e : selectedParts.entrySet()) {
                json.put(e.getKey(), e.getValue().id);
                total += e.getValue().price;
                count++;
            }
            BuildsRepository.save(name, json.toString(), count, total, "scratch");
            Toast.makeText(this, "✅ Build saved!", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't save the build.", Toast.LENGTH_SHORT).show();
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}