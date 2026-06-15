package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * PartsListActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Now reads from the shared PartsCatalog (single source of truth) instead of
 * its own internal lists. Returns the selected part's id (+ basics) so the
 * scratch build screen can look up full attributes from PartsCatalog.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class PartsListActivity extends AppCompatActivity {

    private String catId;
    private String catLabel;
    private String selectedPartId;
    private double remainingBudget;   // budget available for THIS category

    private LinearLayout llPartsList;
    private List<PartsCatalog.Part> allParts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parts_list);

        catId          = getIntent().getStringExtra("catId");
        catLabel       = getIntent().getStringExtra("catLabel");
        selectedPartId = getIntent().getStringExtra("selectedPartId");
        remainingBudget = getIntent().getDoubleExtra("remainingBudget", Double.MAX_VALUE);

        TextView tvTitle = findViewById(R.id.tvPartsTitle);
        tvTitle.setText(catLabel + "s");

        llPartsList = findViewById(R.id.llPartsList);

        // Only show parts at or under the remaining budget (cheapest first).
        allParts = new ArrayList<>();
        for (PartsCatalog.Part p : PartsRepository.getByCategory(catId)) {
            if (p.price <= remainingBudget + 0.01) allParts.add(p);
        }
        java.util.Collections.sort(allParts, (a, b) -> Double.compare(a.price, b.price));

        if (allParts.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No parts fit your remaining budget (€"
                    + String.format("%.0f", remainingBudget) + ").");
            empty.setTextColor(0xFF94A3B8);
            empty.setTextSize(14);
            empty.setPadding(0, 40, 0, 0);
            llPartsList.addView(empty);
        } else {
            renderParts(allParts);
        }

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterParts(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btnPartsBack).setOnClickListener(v -> finish());
    }

    private void filterParts(String query) {
        if (query.isEmpty()) {
            renderParts(allParts);
            return;
        }
        List<PartsCatalog.Part> filtered = new ArrayList<>();
        for (PartsCatalog.Part p : allParts) {
            if (p.name.toLowerCase().contains(query.toLowerCase())
                    || (p.manufacturer != null && p.manufacturer.toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(p);
            }
        }
        renderParts(filtered);
    }

    private void renderParts(List<PartsCatalog.Part> parts) {
        llPartsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PartsCatalog.Part p : parts) {
            View item = inflater.inflate(R.layout.item_part_card, llPartsList, false);

            TextView tvName      = item.findViewById(R.id.tvPartName);
            TextView tvPrice     = item.findViewById(R.id.tvPartPrice);
            TextView tvSpec      = item.findViewById(R.id.tvPartSpec);
            TextView tvSponsored = item.findViewById(R.id.tvSponsored);
            TextView tvAdd       = item.findViewById(R.id.tvAddBtn);
            ImageView imgThumb   = item.findViewById(R.id.imgPartThumb);
            TextView tvIcon      = item.findViewById(R.id.tvPartIcon);

            // Real photo when the part has one; otherwise fall back to the emoji.
            if (p.imageRes != 0 && p.imageRes != PartsCatalog.PLACEHOLDER_IMG) {
                imgThumb.setImageResource(p.imageRes);
                imgThumb.setVisibility(View.VISIBLE);
                tvIcon.setVisibility(View.GONE);
            } else {
                imgThumb.setVisibility(View.GONE);
                tvIcon.setVisibility(View.VISIBLE);
            }

            // Show manufacturer + name together when available
            String displayName = (p.manufacturer != null && !p.manufacturer.equals("—"))
                    ? p.manufacturer + " " + p.name : p.name;
            tvName.setText(displayName);
            tvPrice.setText(String.format("€%.2f", p.price));
            tvSpec.setText(p.spec);

            tvSponsored.setVisibility(p.sponsored ? View.VISIBLE : View.GONE);

            boolean isAdded = p.id.equals(selectedPartId);
            updateAddButton(tvAdd, isAdded);

            tvAdd.setOnClickListener(v -> {
                boolean nowAdded = p.id.equals(selectedPartId);
                if (nowAdded) {
                    selectedPartId = null;
                    updateAddButton(tvAdd, false);
                    returnResult(null, true);
                } else {
                    selectedPartId = p.id;
                    renderParts(PartsRepository.getByCategory(catId));
                    returnResult(p, false);
                }
            });

            // Tapping the card (not the button) opens the detail page
            item.setOnClickListener(v -> {
                Intent intent = new Intent(this, PartDetailActivity.class);
                intent.putExtra("partId", p.id);
                startActivity(intent);
            });

            llPartsList.addView(item);
        }
    }

    private void updateAddButton(TextView btn, boolean added) {
        if (added) {
            btn.setText("✓  Added");
            btn.setBackgroundResource(R.drawable.bg_button_accent);
            btn.setTextColor(getResources().getColor(R.color.bg_dark));
        } else {
            btn.setText("+  Add to Build");
            btn.setBackgroundResource(R.drawable.bg_add_button_outline);
            btn.setTextColor(getResources().getColor(R.color.accent));
        }
    }

    private void returnResult(PartsCatalog.Part p, boolean removed) {
        Intent result = new Intent();
        result.putExtra("catId",   catId);
        result.putExtra("removed", removed);
        if (p != null) {
            // partId is enough — the scratch screen looks up full data in PartsCatalog.
            result.putExtra("partId",    p.id);
            result.putExtra("partName",  p.name);
            result.putExtra("partPrice", p.price);
            result.putExtra("partWatts", p.watts);
            result.putExtra("partScore", p.score);
        }
        setResult(RESULT_OK, result);
        // Don't finish() — let user keep browsing; parent refreshes on resume
    }
}