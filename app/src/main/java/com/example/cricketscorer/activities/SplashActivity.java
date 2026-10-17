package com.example.cricketscorer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cricketscorer.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500L;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable openDashboard = () -> {

        Intent intent = new Intent(
                SplashActivity.this,
                DashboardActivity.class
        );

        startActivity(intent);

        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        handler.postDelayed(
                openDashboard,
                SPLASH_DELAY
        );
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(openDashboard);
        super.onDestroy();
    }
}