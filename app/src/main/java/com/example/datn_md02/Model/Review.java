package com.example.datn_md02.Model;

import java.io.Serializable;

public class Review implements Serializable {
    private double rating;
    private String comment;
    private String userId;
    private long timestamp;

    public Review() {}

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
