package com.example.mediatracker.api;

import com.example.mediatracker.models.GoogleBooksResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksService {
    @GET("/books/v1/volumes")
    Call<GoogleBooksResponse> searchBooks(
            @Query("q") String searchQuery,
            @Query("maxResults") int maxResults
    );
}

