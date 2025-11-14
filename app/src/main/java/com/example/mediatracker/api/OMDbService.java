package com.example.mediatracker.api;

import com.example.mediatracker.models.Movie;
import com.example.mediatracker.models.OMDbResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OMDbService {
    // IMPORTANT: Replace "YOUR_API_KEY" with your actual OMDb API key
    // Get your free API key at: http://www.omdbapi.com/apikey.aspx
    String API_KEY = "8ae18425";

    @GET("/")
    Call<OMDbResponse> searchMovies(
            @Query("apikey") String apiKey,
            @Query("s") String searchQuery,
            @Query("type") String type
    );

    @GET("/")
    Call<Movie> getMovieDetails(
            @Query("apikey") String apiKey,
            @Query("i") String imdbId,
            @Query("plot") String plot
    );
}

