package com.z_iti_271304_u3_e01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Retraso antes de ir a MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, InitialActivity.class);
            startActivity(intent);
            finish(); // Cierra SplashActivity para que no regrese al presionar "Atrás"
        }, 3000); // 3000 ms = 3 segundos
    }
}
