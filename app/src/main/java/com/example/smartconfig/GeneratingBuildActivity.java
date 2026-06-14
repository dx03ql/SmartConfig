package com.example.smartconfig;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GeneratingBuildActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Handles BOTH flows:
 *   • Smart  → "profileJson": runs PartsCatalog.selectBuild()
 *   • Scratch → "partsJson"  : a {category: partId} map the user picked
 *
 * Shows a brief pulsing-icon "generating" animation (~1.5s), then reveals the
 * build: photo + name + manufacturer + price per component, tap → detail page.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class GeneratingBuildActivity extends AppCompatActivity {

    private static final long GENERATING_MS = 1500;

    private int budget;
    private boolean isGuest;
    private String buildMode = "smart";
    private Map<String, PartsCatalog.Part> currentBuild;

    private View overlay;
    private TextView genIcon;

    private static final Map<String, String[]> CATEGORY_INFO = new LinkedHashMap<String, String[]>() {{
        put("cpu",     new String[]{"🔲", "Processor (CPU)"});
        put("gpu",     new String[]{"🟪", "Graphics Card (GPU)"});
        put("ram",     new String[]{"🟦", "Memory (RAM)"});
        put("mobo",    new String[]{"🟧", "Motherboard"});
        put("storage", new String[]{"💾", "Storage"});
        put("cooling", new String[]{"❄", "CPU Cooler"});
        put("psu",     new String[]{"⚡", "Power Supply"});
        put("case",    new String[]{"🖥", "Case"});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generating_build);

        budget  = getIntent().getIntExtra("budget", 500);
        isGuest = getIntent().getBooleanExtra("isGuest", false);
        buildMode = getIntent().getStringExtra("buildMode");
        if (buildMode == null) buildMode = "smart";
        String profileJson = getIntent().getStringExtra("profileJson");
        String partsJson   = getIntent().getStringExtra("partsJson");

        overlay = findViewById(R.id.overlayGenerating);
        genIcon = findViewById(R.id.tvGenIcon);

        ((TextView) findViewById(R.id.tvBudget)).setText("€" + budget);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnContinue).setOnClickListener(v -> showSaveDialog());

        // Build the parts map UNDER the overlay so it's ready when we reveal.
        try {
            Map<String, PartsCatalog.Part> chosen;
            if (profileJson != null) {
                chosen = PartsCatalog.selectBuild(rebuildProfile(profileJson));
            } else if (partsJson != null) {
                chosen = parseScratch(partsJson);
            } else {
                Toast.makeText(this, "No build received.", Toast.LENGTH_SHORT).show();
                hideOverlay();
                return;
            }
            currentBuild = chosen;
            renderBuild(chosen);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't generate the build.", Toast.LENGTH_SHORT).show();
        }

        // Play the pulsing animation, then reveal the build.
        startPulse();
        new Handler(Looper.getMainLooper()).postDelayed(this::hideOverlay, GENERATING_MS);
    }

    // ── Animation ──────────────────────────────────────────────────────────────
    private void startPulse() {
        ScaleAnimation pulse = new ScaleAnimation(
                1.0f, 1.15f, 1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        pulse.setDuration(600);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        genIcon.startAnimation(pulse);
    }

    private void hideOverlay() {
        if (overlay == null || overlay.getVisibility() != View.VISIBLE) return;
        overlay.animate()
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    genIcon.clearAnimation();
                    overlay.setVisibility(View.GONE);
                })
                .start();
    }

    // ── Scratch flow: {category: partId} → look up full parts ────────────────────
    private Map<String, PartsCatalog.Part> parseScratch(String json) throws Exception {
        Map<String, PartsCatalog.Part> chosen = new LinkedHashMap<>();
        JSONObject o = new JSONObject(json);
        Iterator<String> keys = o.keys();
        while (keys.hasNext()) {
            String cat = keys.next();
            String partId = o.optString(cat, null);
            if (partId != null) {
                PartsCatalog.Part p = PartsRepository.findById(partId);
                if (p != null) chosen.put(cat, p);
            }
        }
        return chosen;
    }

    private BuildProfile rebuildProfile(String json) throws Exception {
        JSONObject o = new JSONObject(json);
        BuildProfile p = new BuildProfile(o.optInt("budget", budget));
        p.primaryUse      = o.optString("primaryUse", "general");
        p.aesthetic       = o.optString("aesthetic", "minimal_clean");
        p.wantsRgb        = o.optBoolean("wantsRgb", false);
        p.wantsQuiet      = o.optBoolean("wantsQuiet", false);
        p.wantsSmallForm  = o.optBoolean("wantsSmallForm", false);
        p.wantsUpgradable = o.optBoolean("wantsUpgradable", false);

        JSONObject alloc = o.getJSONObject("allocation");
        p.allocation.clear();
        for (String cat : BuildProfile.CATEGORIES) {
            p.allocation.put(cat, alloc.optDouble(cat, 0));
        }
        return p;
    }

    private void renderBuild(Map<String, PartsCatalog.Part> chosen) {
        LinearLayout container = findViewById(R.id.llPartsContainer);
        container.removeAllViews();

        double total = 0;
        for (Map.Entry<String, String[]> entry : CATEGORY_INFO.entrySet()) {
            PartsCatalog.Part part = chosen.get(entry.getKey());
            if (part != null) {
                total += part.price;
                container.addView(buildPartCard(entry.getValue()[1], part));
            }
        }

        TextView tvTotal = findViewById(R.id.tvTotalCost);
        tvTotal.setText(String.format("€%.0f", total));
        tvTotal.setTextColor(total <= budget ? Color.parseColor("#22C55E") : Color.parseColor("#F59E0B"));
    }

    /** A tappable card: [photo] [category label / name / manufacturer] [price]. */
    private View buildPartCard(String categoryLabel, PartsCatalog.Part part) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1E293B"));
        int pad = dp(12);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(10);
        card.setLayoutParams(cardLp);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(dp(54), dp(54));
        imgLp.rightMargin = dp(14);
        img.setLayoutParams(imgLp);
        img.setImageResource(part != null ? part.imageRes : PartsCatalog.PLACEHOLDER_IMG);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        LinearLayout middle = new LinearLayout(this);
        middle.setOrientation(LinearLayout.VERTICAL);
        middle.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(categoryLabel);
        tvLabel.setTextColor(Color.parseColor("#64748B"));
        tvLabel.setTextSize(11);

        TextView tvName = new TextView(this);
        tvName.setText(part != null ? part.name : "Not available");
        tvName.setTextColor(Color.parseColor("#FFFFFF"));
        tvName.setTextSize(14);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);
        tvName.setPadding(0, dp(2), 0, 0);

        TextView tvMaker = new TextView(this);
        tvMaker.setText(part != null && part.manufacturer != null && !part.manufacturer.equals("—")
                ? part.manufacturer : "");
        tvMaker.setTextColor(Color.parseColor("#94A3B8"));
        tvMaker.setTextSize(12);
        tvMaker.setPadding(0, dp(1), 0, 0);

        middle.addView(tvLabel);
        middle.addView(tvName);
        middle.addView(tvMaker);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);
        LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightLp.leftMargin = dp(10);
        right.setLayoutParams(rightLp);

        TextView tvPrice = new TextView(this);
        if (part != null && part.price > 0) {
            tvPrice.setText(String.format("€%.0f", part.price));
        } else if (part != null) {
            tvPrice.setText("Included");
        } else {
            tvPrice.setText("—");
        }
        tvPrice.setTextColor(Color.parseColor("#6EE2F5"));
        tvPrice.setTextSize(15);
        tvPrice.setTypeface(tvPrice.getTypeface(), Typeface.BOLD);
        tvPrice.setGravity(Gravity.END);

        TextView tvChevron = new TextView(this);
        tvChevron.setText("View ›");
        tvChevron.setTextColor(Color.parseColor("#64748B"));
        tvChevron.setTextSize(11);
        tvChevron.setGravity(Gravity.END);
        tvChevron.setPadding(0, dp(2), 0, 0);

        right.addView(tvPrice);
        right.addView(tvChevron);

        card.addView(img);
        card.addView(middle);
        card.addView(right);

        if (part != null) {
            card.setClickable(true);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(this, PartDetailActivity.class);
                intent.putExtra("partId", part.id);
                startActivity(intent);
            });
        }

        return card;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    // ── Save this build (name dialog → SQLite) ───────────────────────────────
    private void showSaveDialog() {
        if (currentBuild == null || currentBuild.isEmpty()) {
            Toast.makeText(this, "Nothing to save yet.", Toast.LENGTH_SHORT).show();
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
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString();
                    saveBuild(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveBuild(String name) {
        try {
            JSONObject json = new JSONObject();
            double total = 0;
            int count = 0;
            for (Map.Entry<String, PartsCatalog.Part> e : currentBuild.entrySet()) {
                json.put(e.getKey(), e.getValue().id);
                total += e.getValue().price;
                count++;
            }
            BuildsRepository.save(name, json.toString(), count, total, buildMode);
            Toast.makeText(this, "✅ Build saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't save the build.", Toast.LENGTH_SHORT).show();
        }
    }
}