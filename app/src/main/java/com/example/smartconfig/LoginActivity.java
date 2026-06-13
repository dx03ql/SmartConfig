package com.example.smartconfig;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtForgot, txtRegister;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        txtForgot   = findViewById(R.id.txtForgot);
        txtRegister = findViewById(R.id.txtRegister);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            setLoading(true);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        saveSession(email);
                        setLoading(false);

                        if (getIntent().getBooleanExtra("returnToScratch", false)) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            startActivity(new Intent(this, BudgetActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        txtForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(this, "Reset email sent!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        txtRegister.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );

        findViewById(R.id.btnGuest).setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetActivity.class);
            intent.putExtra("isGuest", true);
            startActivity(intent);
        });
    }

    private void saveSession(String email) {
        SharedPreferences prefs = getSharedPreferences("smartconfig_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_email", email)
                .apply();
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}