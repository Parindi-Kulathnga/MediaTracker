package com.example.mediatracker.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("Title")
    private String title;
    
    @SerializedName("Year")
    private String year;
    
    @SerializedName("imdbID")
    private String imdbID;
    
    @SerializedName("Type")
    private String type;
    
    @SerializedName("Poster")
    private String poster;
    
    @SerializedName("Plot")
    private String plot;
    
    @SerializedName("Director")
    private String director;
    
    @SerializedName("Actors")
    private String actors;
    
    @SerializedName("Genre")
    private String genre;
    
    @SerializedName("imdbRating")
    private String imdbRating;

    public Movie() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActors() {
        return actors;
    }

    public void setActors(String actors) {
        this.actors = actors;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating) {
        this.imdbRating = imdbRating;
    }
}

