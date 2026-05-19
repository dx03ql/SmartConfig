package com.example.smartconfig;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnGaming, btnCreation, btnOffice, btnDev;
    Button btnNext, btnSelect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGaming = findViewById(R.id.btnGaming);
        btnCreation = findViewById(R.id.btnCreation);
        btnOffice = findViewById(R.id.btnOffice);
        btnDev = findViewById(R.id.btnDev);

        btnNext = findViewById(R.id.btnNext);
        btnSelect = findViewById(R.id.btnSelect);

        btnGaming.setOnClickListener(v ->
                Toast.makeText(this, "Gaming sélectionné", Toast.LENGTH_SHORT).show());

        btnCreation.setOnClickListener(v ->
                Toast.makeText(this, "Création sélectionnée", Toast.LENGTH_SHORT).show());

        btnOffice.setOnClickListener(v ->
                Toast.makeText(this, "Bureautique sélectionnée", Toast.LENGTH_SHORT).show());

        btnDev.setOnClickListener(v ->
                Toast.makeText(this, "Dev / IA sélectionné", Toast.LENGTH_SHORT).show());

        btnNext.setOnClickListener(v ->
                Toast.makeText(this, "Étape suivante", Toast.LENGTH_SHORT).show());

        btnSelect.setOnClickListener(v ->
                Toast.makeText(this,
                        "Configuration Performance sélectionnée",
                        Toast.LENGTH_LONG).show());
    }
}