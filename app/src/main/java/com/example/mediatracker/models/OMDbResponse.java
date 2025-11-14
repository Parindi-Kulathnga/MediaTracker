package com.example.mediatracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OMDbResponse {
    @SerializedName("Search")
    private List<Movie> search;
    
    @SerializedName("totalResults")
    private String totalResults;
    
    @SerializedName("Response")
    private String response;
    
    @SerializedName("Error")
    private String error;

    public OMDbResponse() {
    }

    public List<Movie> getSearch() {
        return search;
    }

    public void setSearch(List<Movie> search) {
        this.search = search;
    }

    public String getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(String totalResults) {
        this.totalResults = totalResults;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

