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
import com.example.mediatracker.adapters.BookAdapter;
import com.example.mediatracker.api.ApiClient;
import com.example.mediatracker.api.GoogleBooksService;
import com.example.mediatracker.models.Book;
import com.example.mediatracker.models.GoogleBooksResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BookSearchActivity extends BaseActivity {
    private EditText etSearch;
    private ImageButton btnSearch;
    private ImageButton btnClear;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private BookAdapter adapter;
    private List<Book> bookList;
    private GoogleBooksService booksService;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 500; // 500ms delay for auto-complete

    // Popular book search terms for initial random books
    private static final String[] POPULAR_SEARCH_TERMS = {
        "fiction", "mystery", "romance", "science fiction", "fantasy", "thriller",
        "biography", "history", "self-help", "philosophy", "adventure", "horror"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search Books");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        bookList = new ArrayList<>();
        adapter = new BookAdapter(this, bookList);
        
        // Use GridLayoutManager with 2 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        booksService = ApiClient.getGoogleBooksService();

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
            loadRandomBooks();
        });

        // Load random popular books on start
        loadRandomBooks();
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
                    // If search is cleared, show random books again
                    loadRandomBooks();
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

        searchBooks(query);
    }

    private void searchBooks(String query) {
        progressBar.setVisibility(View.VISIBLE);
        Call<GoogleBooksResponse> call = booksService.searchBooks(query, 20);
        
        call.enqueue(new Callback<GoogleBooksResponse>() {
            @Override
            public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    GoogleBooksResponse booksResponse = response.body();
                    if (booksResponse.getItems() != null && !booksResponse.getItems().isEmpty()) {
                        bookList.clear();
                        bookList.addAll(booksResponse.getItems());
                        adapter.notifyDataSetChanged();
                    } else {
                        if (!TextUtils.isEmpty(query)) {
                            Toast.makeText(BookSearchActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                        }
                        bookList.clear();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(BookSearchActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BookSearchActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRandomBooks() {
        progressBar.setVisibility(View.VISIBLE);
        // Get random search terms
        List<String> searchTerms = new ArrayList<>(Arrays.asList(POPULAR_SEARCH_TERMS));
        Collections.shuffle(searchTerms);
        
        // Search for books using random popular terms
        List<String> selectedTerms = searchTerms.subList(0, Math.min(3, searchTerms.size()));
        List<Book> allBooks = new ArrayList<>();
        final int[] completedSearches = {0};
        final int totalSearches = selectedTerms.size();

        for (String term : selectedTerms) {
            Call<GoogleBooksResponse> call = booksService.searchBooks(term, 20);
            
            call.enqueue(new Callback<GoogleBooksResponse>() {
                @Override
                public void onResponse(Call<GoogleBooksResponse> call, Response<GoogleBooksResponse> response) {
                    completedSearches[0]++;
                    if (response.isSuccessful() && response.body() != null) {
                        GoogleBooksResponse booksResponse = response.body();
                        if (booksResponse.getItems() != null && !booksResponse.getItems().isEmpty()) {
                            allBooks.addAll(booksResponse.getItems());
                        }
                    }
                    
                    // When all searches complete, shuffle and display
                    if (completedSearches[0] >= totalSearches) {
                        progressBar.setVisibility(View.GONE);
                        Collections.shuffle(allBooks);
                        // Limit to first 20 books for better performance
                        bookList.clear();
                        bookList.addAll(allBooks.subList(0, Math.min(20, allBooks.size())));
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<GoogleBooksResponse> call, Throwable t) {
                    completedSearches[0]++;
                    if (completedSearches[0] >= totalSearches) {
                        progressBar.setVisibility(View.GONE);
                        if (!allBooks.isEmpty()) {
                            Collections.shuffle(allBooks);
                            bookList.clear();
                            bookList.addAll(allBooks.subList(0, Math.min(20, allBooks.size())));
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

