package com.z_iti_271304_u3_e01;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.z_iti_271304_u3_e01.manual.ManualActivity;


public class InitialActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        Button btnAutomatic = findViewById(R.id.btnAutomatic);
        Button btnManual = findViewById(R.id.btnManual);

        btnAutomatic.setOnClickListener(v -> {
            Intent intent = new Intent(this, DrawingActivity.class);
            startActivity(intent);


        });

        btnManual.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManualActivity.class);
            startActivity(intent);

        });
    }
}