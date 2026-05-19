package com.example.smartconfig;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartconfig.LoginActivity;

public class LandingActivity extends AppCompatActivity {

    Button btnStart;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        btnStart = findViewById(R.id.btnStart);
        txtLogin = findViewById(R.id.txtLogin);

        // GET STARTED -> SIGN UP PAGE
        btnStart.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LandingActivity.this,
                    SignupActivity.class
            );

            startActivity(intent);

        });

        // LOGIN TEXT -> LOGIN PAGE
        txtLogin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LandingActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

        });
    }
}