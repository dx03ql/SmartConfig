package com.example.smartconfig;

import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtForgot, txtRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        txtForgot  = findViewById(R.id.txtForgot);
        txtRegister = findViewById(R.id.txtRegister);

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save session
                SharedPreferences prefs = getSharedPreferences("smartconfig_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("user_email", email)
                        .apply();

                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // If launched from profile tab, return to the build screen
                if (getIntent().getBooleanExtra("returnToScratch", false)) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            }
        });

        txtForgot.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        );

        txtRegister.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Register page coming soon", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnGuest).setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetActivity.class);
            intent.putExtra("isGuest", true);
            startActivity(intent);
        });
    }
}