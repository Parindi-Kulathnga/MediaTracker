package com.example.mediatracker.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Book {
    @SerializedName("id")
    private String id;
    
    @SerializedName("volumeInfo")
    private VolumeInfo volumeInfo;

    public Book() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public static class VolumeInfo {
        @SerializedName("title")
        private String title;
        
        @SerializedName("authors")
        private List<String> authors;
        
        @SerializedName("publishedDate")
        private String publishedDate;
        
        @SerializedName("description")
        private String description;
        
        @SerializedName("imageLinks")
        private ImageLinks imageLinks;
        
        @SerializedName("publisher")
        private String publisher;
        
        @SerializedName("pageCount")
        private Integer pageCount;
        
        @SerializedName("categories")
        private List<String> categories;
        
        @SerializedName("averageRating")
        private Double averageRating;
        
        @SerializedName("ratingsCount")
        private Integer ratingsCount;

        public VolumeInfo() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getAuthors() {
            return authors;
        }

        public void setAuthors(List<String> authors) {
            this.authors = authors;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ImageLinks getImageLinks() {
            return imageLinks;
        }

        public void setImageLinks(ImageLinks imageLinks) {
            this.imageLinks = imageLinks;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        public Integer getPageCount() {
            return pageCount;
        }

        public void setPageCount(Integer pageCount) {
            this.pageCount = pageCount;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public void setAverageRating(Double averageRating) {
            this.averageRating = averageRating;
        }

        public Integer getRatingsCount() {
            return ratingsCount;
        }

        public void setRatingsCount(Integer ratingsCount) {
            this.ratingsCount = ratingsCount;
        }
    }

    public static class ImageLinks {
        @SerializedName("thumbnail")
        private String thumbnail;
        
        @SerializedName("smallThumbnail")
        private String smallThumbnail;

        public ImageLinks() {
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getSmallThumbnail() {
            return smallThumbnail;
        }

        public void setSmallThumbnail(String smallThumbnail) {
            this.smallThumbnail = smallThumbnail;
        }
    }
}

