package com.example.smartconfig;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BuildsActivity
 * ─────────────────────────────────────────────────────────────────────────
 * The "Builds" tab. Lists every build the user has saved (from the SQLite
 * builds table via BuildsRepository). Tapping a build reopens it; each row
 * has a Delete link. Built entirely in code so it needs no extra XML/drawables.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class BuildsActivity extends AppCompatActivity {

    private LinearLayout listContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBuilds();   // refresh whenever we come back
    }

    // ── Screen scaffold ───────────────────────────────────────────────────
    private View buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0F172A"));
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        int hp = dp(16);
        header.setPadding(hp, hp, hp, hp);

        TextView back = new TextView(this);
        back.setText("←");
        back.setTextColor(Color.WHITE);
        back.setTextSize(22);
        back.setPadding(0, 0, dp(14), 0);
        back.setOnClickListener(v -> finish());

        TextView title = new TextView(this);
        title.setText("My Builds");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);

        header.addView(back);
        header.addView(title);
        root.addView(header);

        // Scrollable list (takes all space above the nav)
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        int lp = dp(16);
        listContainer.setPadding(lp, dp(4), lp, lp);
        scroll.addView(listContainer);
        root.addView(scroll);

        // Divider + bottom nav
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#1E293B"));
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
        root.addView(divider);
        root.addView(bottomNav());

        return root;
    }

    // ── Saved builds ──────────────────────────────────────────────────────
    private void loadBuilds() {
        if (listContainer == null) return;
        listContainer.removeAllViews();

        List<BuildEntity> builds = BuildsRepository.getAll();
        if (builds.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("🖥  No builds saved yet.\nCreate one from the Build tab and tap Save.");
            empty.setTextColor(Color.parseColor("#64748B"));
            empty.setTextSize(14);
            int pad = dp(20);
            empty.setPadding(pad, pad, pad, pad);
            listContainer.addView(empty);
            return;
        }
        for (BuildEntity b : builds) listContainer.addView(buildRow(b));
    }

    private View buildRow(BuildEntity b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(Color.parseColor("#1E293B"));
        int pad = dp(14);
        row.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(10);
        row.setLayoutParams(rowLp);

        // Tapping the row reopens the saved build
        row.setOnClickListener(v -> openBuild(b));

        // Left: name + meta
        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvName = new TextView(this);
        tvName.setText(b.name);
        tvName.setTextColor(Color.parseColor("#FFFFFF"));
        tvName.setTextSize(15);
        tvName.setTypeface(tvName.getTypeface(), Typeface.BOLD);

        TextView tvMeta = new TextView(this);
        String date = new SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                .format(new Date(b.createdAt));
        boolean isScratch = "scratch".equalsIgnoreCase(b.mode);
        String modeLabel = isScratch ? "Scratch · tap to edit" : "Smart · tap to view";
        tvMeta.setText(b.partCount + " parts · " + date + "\n" + modeLabel);
        tvMeta.setTextColor(Color.parseColor("#94A3B8"));
        tvMeta.setTextSize(12);
        tvMeta.setPadding(0, dp(3), 0, 0);

        left.addView(tvName);
        left.addView(tvMeta);

        // Right: price + delete
        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(String.format(Locale.getDefault(), "€%.0f", b.totalPrice));
        tvPrice.setTextColor(Color.parseColor("#6EE2F5"));
        tvPrice.setTextSize(15);
        tvPrice.setTypeface(tvPrice.getTypeface(), Typeface.BOLD);
        tvPrice.setGravity(Gravity.END);

        TextView tvDelete = new TextView(this);
        tvDelete.setText("Delete");
        tvDelete.setTextColor(Color.parseColor("#DC3A3A"));
        tvDelete.setTextSize(12);
        tvDelete.setPadding(0, dp(4), 0, 0);
        tvDelete.setGravity(Gravity.END);
        tvDelete.setOnClickListener(v -> confirmDelete(b));

        right.addView(tvPrice);
        right.addView(tvDelete);

        row.addView(left);
        row.addView(right);
        return row;
    }

    private void openBuild(BuildEntity b) {
        boolean isScratch = "scratch".equalsIgnoreCase(b.mode);
        if (isScratch) {
            // Editable: open the scratch editor preloaded with this build.
            Intent intent = new Intent(this, ScratchBuildActivity.class);
            intent.putExtra("editBuildId", b.id);
            intent.putExtra("editBuildName", b.name);
            intent.putExtra("partsJson", b.partsJson);
            // No original budget is stored, so allow plenty of headroom while editing.
            intent.putExtra("budget", 100000);
            startActivity(intent);
        } else {
            // Smart builds are view-only.
            Intent intent = new Intent(this, GeneratingBuildActivity.class);
            intent.putExtra("partsJson", b.partsJson);
            intent.putExtra("buildMode", b.mode != null ? b.mode : "smart");
            startActivity(intent);
        }
    }

    private void confirmDelete(BuildEntity b) {
        new AlertDialog.Builder(this)
                .setTitle("Delete build")
                .setMessage("Delete \"" + b.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    BuildsRepository.delete(b.id);
                    loadBuilds();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Bottom nav (Build · Builds · Settings · Profile) ──────────────────
    private View bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setBackgroundColor(Color.parseColor("#0F172A"));
        nav.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(60)));

        nav.addView(navItem("🔧", "Build", false, v ->
                startActivity(new Intent(this, ScratchBuildActivity.class))));
        nav.addView(navItem("📋", "Builds", true, v -> { /* already here */ }));
        nav.addView(navItem("⚙", "Settings", false, v ->
                startActivity(new Intent(this, AccountActivity.class))));
        nav.addView(navItem("👤", "Profile", false, v ->
                startActivity(new Intent(this, AccountActivity.class))));
        return nav;
    }

    private View navItem(String icon, String label, boolean active, View.OnClickListener onClick) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        item.setOnClickListener(onClick);

        TextView ic = new TextView(this);
        ic.setText(icon);
        ic.setTextSize(20);
        ic.setGravity(Gravity.CENTER);

        TextView tx = new TextView(this);
        tx.setText(label);
        tx.setTextSize(10);
        tx.setGravity(Gravity.CENTER);
        tx.setTextColor(active ? Color.parseColor("#6EE2F5") : Color.parseColor("#94A3B8"));

        item.addView(ic);
        item.addView(tx);
        return item;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}