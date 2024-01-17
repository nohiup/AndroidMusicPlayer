package com.example.spotify;

public class MusicFiles {
    String realID;
    String path;
    String title;
    String artist;
    String album;
    String duration;
    String id;
    String language;
    String releaseDate;
    String genre;
    String thumbnailName;

    int like;

    public MusicFiles(String path, String title, String artist, String album, String duration, String id) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.realID = realID;
    }

    public MusicFiles(String path, String title, String artist, String album, String duration, String id, String genre, String language, String releaseDate, String thumbnailName) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.language = language;
        this.genre = genre;
        this.releaseDate = releaseDate;
        this.thumbnailName = thumbnailName;
        this.realID = realID;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public MusicFiles() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public String getthumbnailName() {
        return thumbnailName;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealID() {return this.realID;}
    public void setRealID(String realID){
        this.realID = realID;
    }
}
