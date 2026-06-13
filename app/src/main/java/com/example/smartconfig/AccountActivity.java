package com.example.smartconfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

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
        // Re-render in case user just logged in
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

    private void setupSettings() {
        findViewById(R.id.rowRegion).setOnClickListener(v ->
                Toast.makeText(this, "Region settings — coming soon", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.rowTheme).setOnClickListener(v ->
                Toast.makeText(this, "Theme settings — coming soon", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.rowNotifications).setOnClickListener(v ->
                Toast.makeText(this, "Notification settings — coming soon", Toast.LENGTH_SHORT).show()
        );
        findViewById(R.id.rowFeedback).setOnClickListener(v ->
                Toast.makeText(this, "Feedback — coming soon", Toast.LENGTH_SHORT).show()
        );
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
}