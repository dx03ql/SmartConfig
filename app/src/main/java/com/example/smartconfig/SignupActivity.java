package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartconfig.LoginActivity;

public class SignupActivity extends AppCompatActivity {

    Button btnSignup;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnSignup = findViewById(R.id.btnSignup);
        txtLogin = findViewById(R.id.txtLogin);

        btnSignup.setOnClickListener(v -> {

            Toast.makeText(
                    SignupActivity.this,
                    "Account Created",
                    Toast.LENGTH_SHORT
            ).show();

        });

        // ALREADY HAVE ACCOUNT -> LOGIN
        txtLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignupActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

        });
        findViewById(R.id.btnGuest).setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetActivity.class);
            intent.putExtra("isGuest", true);   // flag so you can hide save/profile features later
            startActivity(intent);
        });
    }
}