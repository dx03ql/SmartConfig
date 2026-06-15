package com.example.smartconfig;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * BaseActivity
 * ─────────────────────────────────────────────────────────────────────────
 * Centers the action-bar title and blends the bar into the dark app
 * background. Any screen that should show a centered "SmartConfig PC"
 * header just changes:
 *     extends AppCompatActivity   →   extends BaseActivity
 * Screens with no action bar are unaffected (it safely no-ops).
 * ─────────────────────────────────────────────────────────────────────────
 */
public class BaseActivity extends AppCompatActivity {

    private static final int APP_DARK = Color.parseColor("#0F172A");

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        if (ab == null) return;   // no action bar on this screen → nothing to do

        // Blend the bar into the app background (flat, no shadow).
        ab.setBackgroundDrawable(new ColorDrawable(APP_DARK));
        ab.setElevation(0f);
        getWindow().setStatusBarColor(APP_DARK);

        // Centered title via a custom view.
        TextView title = new TextView(this);
        title.setText(getTitle());
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(title, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Remove the default left inset so the title is truly centered.
        if (title.getParent() instanceof androidx.appcompat.widget.Toolbar) {
            androidx.appcompat.widget.Toolbar tb =
                    (androidx.appcompat.widget.Toolbar) title.getParent();
            tb.setContentInsetsAbsolute(0, 0);
            tb.setPadding(0, 0, 0, 0);
        }
    }
}