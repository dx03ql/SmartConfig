package com.example.smartconfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccountActivity extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        prefs = getSharedPreferences("smartconfig_prefs", MODE_PRIVATE);

        renderState();
        setupSettings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-render in case user just logged in or saved a build
        renderState();
    }

    private void renderState() {
        boolean loggedIn = prefs.getBoolean("is_logged_in", false);
        String email = prefs.getString("user_email", "");

        TextView tvName         = findViewById(R.id.tvProfileName);
        TextView tvEmail        = findViewById(R.id.tvProfileEmail);
        LinearLayout guestBtns  = findViewById(R.id.layoutGuestButtons);
        LinearLayout statsBar   = findViewById(R.id.layoutStats);
        LinearLayout myBuilds   = findViewById(R.id.layoutMyBuilds);
        LinearLayout logoutRow  = findViewById(R.id.layoutLogout);

        if (loggedIn) {
            // Show logged-in profile
            tvName.setText(email.contains("@") ? email.split("@")[0].toUpperCase() : "User");
            tvEmail.setText(email);
            tvEmail.setVisibility(View.VISIBLE);

            guestBtns.setVisibility(View.GONE);
            statsBar.setVisibility(View.VISIBLE);
            myBuilds.setVisibility(View.VISIBLE);
            logoutRow.setVisibility(View.VISIBLE);

            // Counters + saved builds list (from SQLite)
            loadBuilds();

            // Logout
            findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        } else {
            // Show guest profile
            tvName.setText("Guest User");
            tvEmail.setVisibility(View.GONE);

            guestBtns.setVisibility(View.VISIBLE);
            statsBar.setVisibility(View.GONE);
            myBuilds.setVisibility(View.GONE);
            logoutRow.setVisibility(View.GONE);

            // Sign in
            findViewById(R.id.btnSignIn).setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("returnToScratch", false);
                startActivity(intent);
            });

            // Register
            findViewById(R.id.btnRegister).setOnClickListener(v ->
                    startActivity(new Intent(this, SignupActivity.class))
            );
        }
    }

    // ── Saved builds: counters + list ────────────────────────────────────────
    private void loadBuilds() {
        List<BuildEntity> builds = BuildsRepository.getAll();

        // Counters
        TextView tvBuildCount = findViewById(R.id.tvBuildCount);
        TextView tvPartsSaved = findViewById(R.id.tvPartsSaved);
        int totalParts = 0;
        for (BuildEntity b : builds) totalParts += b.partCount;
        tvBuildCount.setText(String.valueOf(builds.size()));
        tvPartsSaved.setText(String.valueOf(totalParts));

        // List
        LinearLayout container = findViewById(R.id.llBuildsContainer);
        container.removeAllViews();

        if (builds.isEmpty()) {
            container.addView(emptyPlaceholder());
            return;
        }

        for (BuildEntity b : builds) {
            container.addView(buildRow(b));
        }
    }

    private View emptyPlaceholder() {
        TextView tv = new TextView(this);
        tv.setText("🖥  No builds saved yet");
        tv.setTextColor(Color.parseColor("#64748B"));
        tv.setTextSize(14);
        int pad = dp(16);
        tv.setPadding(pad, pad, pad, pad);
        return tv;
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
        tvMeta.setText(b.partCount + " parts · " + date);
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

    private void confirmDelete(BuildEntity b) {
        new AlertDialog.Builder(this)
                .setTitle("Delete build")
                .setMessage("Delete \"" + b.name + "\"?")
                .setPositiveButton("Delete", (d, w) -> {
                    BuildsRepository.delete(b.id);
                    loadBuilds();   // refresh counters + list
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupSettings() {
        // No preference rows currently.
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> {
                    // Sign out from Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Clear local session
                    prefs.edit()
                            .putBoolean("is_logged_in", false)
                            .remove("user_email")
                            .remove("user_name")
                            .apply();

                    renderState();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}