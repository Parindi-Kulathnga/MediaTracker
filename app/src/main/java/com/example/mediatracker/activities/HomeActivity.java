package com.example.mediatracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import com.example.mediatracker.R;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends BaseActivity {
    private CardView cardMovie, cardBook, cardLists;
    private ImageButton btnSettings;
    private SharedPreferences prefs;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Media Tracker");
        }

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("MediaTrackerPrefs", MODE_PRIVATE);

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        cardMovie = findViewById(R.id.cardMovie);
        cardBook = findViewById(R.id.cardBook);
        cardLists = findViewById(R.id.cardLists);
        btnSettings = findViewById(R.id.btnSettings);

        cardMovie.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MovieSearchActivity.class);
            startActivity(intent);
        });

        cardBook.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookSearchActivity.class);
            startActivity(intent);
        });

        cardLists.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyListsActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> showSettingsPopup(v));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            showSettingsPopup(item);
            return true;
        } else if (item.getItemId() == R.id.menu_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsPopup(View anchorView) {
        // Create PopupMenu anchored to the settings button
        PopupMenu popupMenu = new PopupMenu(this, anchorView, android.view.Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.settings_popup_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.popup_logout) {
                performLogout();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void showSettingsPopup(MenuItem item) {
        // Anchor the popup to the action bar area (top right)
        View anchorView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (anchorView == null) {
            anchorView = getWindow().getDecorView();
        }
        
        // Create PopupMenu anchored to the top right
        PopupMenu popupMenu = new PopupMenu(this, anchorView, android.view.Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.settings_popup_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getItemId() == R.id.popup_logout) {
                performLogout();
                return true;
            }
            return false;
        });
        
        popupMenu.show();
    }

    private void performLogout() {
        // Sign out from Firebase
        mAuth.signOut();
        // Clear SharedPreferences
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

