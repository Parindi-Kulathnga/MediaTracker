package com.example.mediatracker.models;

public class ListItem {
    private int id;
    private String userId;
    private String itemId;
    private String title;
    private String type; // "movie" or "book"
    private String year;
    private String poster;
    private String description;
    private String listType; // "watchlist" or "toread"

    // Default constructor required for Firebase
    public ListItem() {
    }

    public ListItem(String userId, String itemId, String title, String type, String year, String poster, String description, String listType) {
        this.userId = userId;
        this.itemId = itemId;
        this.title = title;
        this.type = type;
        this.year = year;
        this.poster = poster;
        this.description = description;
        this.listType = listType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }
}

