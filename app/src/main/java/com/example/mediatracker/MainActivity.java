package com.example.mediatracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.mediatracker.activities.BaseActivity;
import com.example.mediatracker.activities.LoginActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences prefs = getSharedPreferences("MediaTrackerPrefs", MODE_PRIVATE);
        
        // Redirect to appropriate activity based on login status
        Intent intent;
        if (prefs.getBoolean("is_logged_in", false)) {
            intent = new Intent(this, com.example.mediatracker.activities.HomeActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}