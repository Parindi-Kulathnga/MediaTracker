package com.example.mediatracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GoogleBooksResponse {
    @SerializedName("items")
    private List<Book> items;
    
    @SerializedName("totalItems")
    private Integer totalItems;

    public GoogleBooksResponse() {
    }

    public List<Book> getItems() {
        return items;
    }

    public void setItems(List<Book> items) {
        this.items = items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
}

