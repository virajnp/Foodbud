package com.example.foodbud;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent ii = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(ii);
                finish();

            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

