package com.example.mediatracker.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static final String OMDb_BASE_URL = "https://www.omdbapi.com";
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com";

    private static Retrofit omdbRetrofit;
    private static Retrofit googleBooksRetrofit;

    public static Retrofit getOMDbClient() {
        if (omdbRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            omdbRetrofit = new Retrofit.Builder()
                    .baseUrl(OMDb_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return omdbRetrofit;
    }

    public static Retrofit getGoogleBooksClient() {
        if (googleBooksRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            googleBooksRetrofit = new Retrofit.Builder()
                    .baseUrl(GOOGLE_BOOKS_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return googleBooksRetrofit;
    }

    public static OMDbService getOMDbService() {
        return getOMDbClient().create(OMDbService.class);
    }

    public static GoogleBooksService getGoogleBooksService() {
        return getGoogleBooksClient().create(GoogleBooksService.class);
    }
}

