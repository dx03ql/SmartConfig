package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartconfig.MainActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView txtForgot, txtRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);

        txtForgot = findViewById(R.id.txtForgot);
        txtRegister = findViewById(R.id.txtRegister);

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            if(email.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                        LoginActivity.this,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                ).show();

            } else {

                Toast.makeText(
                        LoginActivity.this,
                        "Login Successful",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        LoginActivity.this,
                        MainActivity.class
                );

                startActivity(intent);
            }

        });

        txtForgot.setOnClickListener(v -> {

            Toast.makeText(
                    LoginActivity.this,
                    "Forgot Password clicked",
                    Toast.LENGTH_SHORT
            ).show();

        });

        txtRegister.setOnClickListener(v -> {

            Toast.makeText(
                    LoginActivity.this,
                    "Register page coming soon",
                    Toast.LENGTH_SHORT
            ).show();

        });
        findViewById(R.id.btnGuest).setOnClickListener(v -> {
            Intent intent = new Intent(this, BudgetActivity.class);
            intent.putExtra("isGuest", true);   // flag so you can hide save/profile features later
            startActivity(intent);
        });
    }
}