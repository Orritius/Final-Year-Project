package albumcredibilityapplication.core;

import java.util.*;

public class Album {
    private int albumID;
    private String albumName;
    private String albumGenre;
    private double avgRating;
    private Date releaseDate;
    private String artist;
    private String parentalAdvisory;
    private String albumLanguage;

    public Album(){
        // The assignment of the variables for album is handled by the databasequery method findAlbumInfo
    }

    public String getAlbumGenre() {
        return albumGenre;
    }

    public void setAlbumGenre(String albumGenre) {
        this.albumGenre = albumGenre;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(double avgRating) {
        this.avgRating = avgRating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getArtist() {
        return artist;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getParentalAdvisory() {
        return parentalAdvisory;
    }

    public void setParentalAdvisory(String parentalAdvisory) {
        this.parentalAdvisory = parentalAdvisory;
    }

    public String getAlbumLanguage() {
        return albumLanguage;
    }

    public void setAlbumLanguage(String albumLanguage) {
        this.albumLanguage = albumLanguage;
    }

    public int getAlbumID() {
        return albumID;
    }

    public void setAlbumID(int albumID) {
        this.albumID = albumID;
    }
}
