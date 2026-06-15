package com.example.smartconfig;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PartDetailActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Receives a partId, looks it up in PartsCatalog, and shows full details:
 * photo, name, manufacturer, price, spec list, and store search buttons.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class PartDetailActivity extends AppCompatActivity {

    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<String, String>() {{
        put("cpu", "PROCESSOR");
        put("gpu", "GRAPHICS CARD");
        put("ram", "MEMORY");
        put("mobo", "MOTHERBOARD");
        put("storage", "STORAGE");
        put("cooling", "CPU COOLER");
        put("psu", "POWER SUPPLY");
        put("case", "CASE");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String partId = getIntent().getStringExtra("partId");
        PartsCatalog.Part part = partId != null ? PartsRepository.findById(partId) : null;

        if (part == null) {
            Toast.makeText(this, "Part not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Photo
        ((ImageView) findViewById(R.id.imgPart)).setImageResource(part.imageRes);

        // Category chip
        String catName = CATEGORY_NAMES.getOrDefault(part.category, part.category.toUpperCase());
        ((TextView) findViewById(R.id.tvCategory)).setText(catName);

        // Name + manufacturer
        ((TextView) findViewById(R.id.tvName)).setText(part.name);
        TextView tvMaker = findViewById(R.id.tvManufacturer);
        if (part.manufacturer != null && !part.manufacturer.equals("—")) {
            tvMaker.setText("by " + part.manufacturer);
        } else {
            tvMaker.setText("");
        }

        // Price
        TextView tvPrice = findViewById(R.id.tvPrice);
        if (part.price > 0) {
            tvPrice.setText(String.format("€%.0f", part.price));
        } else {
            tvPrice.setText("Included / Free");
        }

        // Specs
        renderSpecs(part);

        // Store buttons — search by the full part name
        String query = (part.manufacturer != null && !part.manufacturer.equals("—")
                ? part.manufacturer + " " : "") + part.name;

        findViewById(R.id.btnAmazon).setOnClickListener(v ->
                openUrl("https://www.amazon.com/s?k=" + encode(query)));
        findViewById(R.id.btnNewegg).setOnClickListener(v ->
                openUrl("https://www.newegg.com/p/pl?d=" + encode(query)));
        findViewById(R.id.btnPcpp).setOnClickListener(v ->
                openUrl("https://pcpartpicker.com/search/?q=" + encode(query)));
    }

    private void renderSpecs(PartsCatalog.Part part) {
        LinearLayout container = findViewById(R.id.llSpecs);
        container.removeAllViews();

        addSpec(container, "Manufacturer", part.manufacturer != null ? part.manufacturer : "—");
        if (part.spec != null && !part.spec.isEmpty()) addSpec(container, "Details", part.spec);
        if (part.socket != null) addSpec(container, "Socket", part.socket);
        if (part.ddr != null)    addSpec(container, "Memory type", part.ddr);

        if ("psu".equals(part.category)) {
            addSpec(container, "Capacity", part.watts + "W");
        } else if (part.watts > 0) {
            addSpec(container, "Power draw", "~" + part.watts + "W");
        }

        // Feature flags
        StringBuilder features = new StringBuilder();
        if (part.rgb)   features.append("RGB lighting  ");
        if (part.quiet) features.append("Quiet  ");
        if (part.sff)   features.append("Small-form-factor  ");
        if (part.igpu)  features.append("Integrated graphics  ");
        if (features.length() > 0) addSpec(container, "Features", features.toString().trim());

        addSpec(container, "Price", part.price > 0 ? String.format("€%.0f", part.price) : "Included");
    }

    private void addSpec(LinearLayout container, String label, String value) {
        LinearLayout line = new LinearLayout(this);
        line.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        line.setLayoutParams(lp);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(Color.parseColor("#94A3B8"));
        tvLabel.setTextSize(14);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tvLabel.setLayoutParams(labelLp);

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(Color.parseColor("#FFFFFF"));
        tvValue.setTextSize(14);
        tvValue.setGravity(Gravity.END);
        LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        tvValue.setLayoutParams(valueLp);

        line.addView(tvLabel);
        line.addView(tvValue);
        container.addView(line);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't open the store.", Toast.LENGTH_SHORT).show();
        }
    }

    private String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s.replace(" ", "+");
        }
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}