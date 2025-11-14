package com.example.mediatracker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mediatracker.R;
import com.example.mediatracker.adapters.ListItemAdapter;
import com.example.mediatracker.models.ListItem;
import com.example.mediatracker.utils.FirebaseDatabaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class MyListsActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private ListItemAdapter adapter;
    private SharedPreferences prefs;
    private TabLayout tabLayout;
    private String currentListType = "watchlist";
    private FirebaseDatabaseHelper firebaseHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Lists");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseHelper = FirebaseDatabaseHelper.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MediaTrackerPrefs", MODE_PRIVATE);

        recyclerView = findViewById(R.id.recyclerView);
        tabLayout = findViewById(R.id.tabLayout);

        String userId = prefs.getString("user_id", null);
        if (userId == null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        adapter = new ListItemAdapter(this, null, userId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Watchlist"));
        tabLayout.addTab(tabLayout.newTab().setText("To-Read List"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentListType = tab.getPosition() == 0 ? "watchlist" : "toread";
                loadListItems();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadListItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListItems();
    }

    private void loadListItems() {
        String userId = prefs.getString("user_id", null);
        if (userId == null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }
        
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firebaseHelper.loadListItems(userId, currentListType, new FirebaseDatabaseHelper.OnListLoadListener() {
            @Override
            public void onListLoaded(List<ListItem> items) {
                adapter.updateList(items);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lists_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_export) {
            exportToSheets();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportToSheets() {
        String userId = prefs.getString("user_id", null);
        if (userId == null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }
        
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseHelper.loadAllListItems(userId, new FirebaseDatabaseHelper.OnListLoadListener() {
            @Override
            public void onListLoaded(List<ListItem> allItems) {
                if (allItems.isEmpty()) {
                    Toast.makeText(MyListsActivity.this, "No items to export", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create CSV content
                StringBuilder csv = new StringBuilder();
                csv.append("Title,Type,Year,List Type,Description\n");
                for (ListItem item : allItems) {
                    csv.append("\"").append(item.getTitle()).append("\",");
                    csv.append("\"").append(item.getType()).append("\",");
                    csv.append("\"").append(item.getYear() != null ? item.getYear() : "").append("\",");
                    csv.append("\"").append(item.getListType()).append("\",");
                    csv.append("\"").append(item.getDescription() != null ? item.getDescription().replace("\"", "\"\"") : "").append("\"\n");
                }

                // Share via Intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/csv");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Media Tracker Export");
                shareIntent.putExtra(Intent.EXTRA_TEXT, csv.toString());
                startActivity(Intent.createChooser(shareIntent, "Export to Sheets"));
            }
        });
    }
}

