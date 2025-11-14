package com.example.mediatracker.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mediatracker.R;
import com.example.mediatracker.adapters.MovieAdapter;
import com.example.mediatracker.api.ApiClient;
import com.example.mediatracker.api.OMDbService;
import com.example.mediatracker.models.Movie;
import com.example.mediatracker.models.OMDbResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MovieSearchActivity extends BaseActivity {
    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageButton btnClear;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private MovieAdapter adapter;
    private List<Movie> movieList;
    private OMDbService omdbService;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 500; // 500ms delay for auto-complete

    // Popular movie search terms for initial random movies
    private static final String[] POPULAR_SEARCH_TERMS = {
        "action", "comedy", "drama", "thriller", "horror", "sci-fi", 
        "adventure", "romance", "fantasy", "crime", "mystery", "war"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Movies");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        movieList = new ArrayList<>();
        adapter = new MovieAdapter(this, movieList);
        
        // Use GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        omdbService = ApiClient.getOMDbService();

        // Setup auto-complete search with debounce
        setupAutoCompleteSearch();

        // Handle Enter key press
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                searchHandler.removeCallbacks(searchRunnable);
                performSearch();
                return true;
            }
            return false;
        });

        // Manual search button
        btnSearch.setOnClickListener(v -> {
            searchHandler.removeCallbacks(searchRunnable);
            performSearch();
        });

        // Clear button
        btnClear.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.requestFocus();
            loadRandomMovies();
        });

        // Load random popular movies on start
        loadRandomMovies();
    }

    private void setupAutoCompleteSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove any pending search
                searchHandler.removeCallbacks(searchRunnable);
                
                String query = s.toString().trim();
                
                // Show/hide clear button
                if (query.length() > 0) {
                    btnClear.setVisibility(View.VISIBLE);
                } else {
                    btnClear.setVisibility(View.GONE);
                }
                
                if (query.length() >= 2) {
                    // Delay search by 500ms after user stops typing
                    searchRunnable = () -> performSearch();
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else if (query.length() == 0) {
                    // If search is cleared, show random movies again
                    loadRandomMovies();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        String query = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            return;
        }

        searchMovies(query);
    }

    private void searchMovies(String query) {
        progressBar.setVisibility(View.VISIBLE);
        Call<OMDbResponse> call = omdbService.searchMovies(OMDbService.API_KEY, query, "movie");
        
        call.enqueue(new Callback<OMDbResponse>() {
            @Override
            public void onResponse(Call<OMDbResponse> call, Response<OMDbResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    OMDbResponse omdbResponse = response.body();
                    if ("True".equals(omdbResponse.getResponse()) && omdbResponse.getSearch() != null) {
                        movieList.clear();
                        movieList.addAll(omdbResponse.getSearch());
                        adapter.notifyDataSetChanged();
                    } else {
                        if (!TextUtils.isEmpty(query)) {
                            Toast.makeText(MovieSearchActivity.this, 
                                    omdbResponse.getError() != null ? omdbResponse.getError() : "No results found", 
                                    Toast.LENGTH_SHORT).show();
                        }
                        movieList.clear();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(MovieSearchActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OMDbResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MovieSearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRandomMovies() {
        progressBar.setVisibility(View.VISIBLE);
        // Get random search terms
        List<String> searchTerms = new ArrayList<>(Arrays.asList(POPULAR_SEARCH_TERMS));
        Collections.shuffle(searchTerms);
        
        // Search for movies using random popular terms
        List<String> selectedTerms = searchTerms.subList(0, Math.min(3, searchTerms.size()));
        List<Movie> allMovies = new ArrayList<>();
        final int[] completedSearches = {0};
        final int totalSearches = selectedTerms.size();

        for (String term : selectedTerms) {
            Call<OMDbResponse> call = omdbService.searchMovies(OMDbService.API_KEY, term, "movie");
            
            call.enqueue(new Callback<OMDbResponse>() {
                @Override
                public void onResponse(Call<OMDbResponse> call, Response<OMDbResponse> response) {
                    completedSearches[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        OMDbResponse omdbResponse = response.body();
                        if ("True".equals(omdbResponse.getResponse()) && omdbResponse.getSearch() != null) {
                            allMovies.addAll(omdbResponse.getSearch());
                        }
                    }
                    
                    // When all searches complete, shuffle and display
                    if (completedSearches[0] >= totalSearches) {
                        progressBar.setVisibility(View.GONE);
                        Collections.shuffle(allMovies);
                        // Limit to first 20 movies for better performance
                        movieList.clear();
                        movieList.addAll(allMovies.subList(0, Math.min(20, allMovies.size())));
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<OMDbResponse> call, Throwable t) {
                    completedSearches[0]++;
                    if (completedSearches[0] >= totalSearches) {
                        progressBar.setVisibility(View.GONE);
                        if (!allMovies.isEmpty()) {
                            Collections.shuffle(allMovies);
                            movieList.clear();
                            movieList.addAll(allMovies.subList(0, Math.min(20, allMovies.size())));
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        if (searchHandler != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}

