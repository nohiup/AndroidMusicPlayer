package com.example.spotify.Object;

public class Users {
    private String name;
    private String email;

    private String role;
    private String provider;

    private Album[] albumList; //every element is an albumID

    public Users(String name, String email, String role, String provider) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.provider = provider;
        this.albumList = null;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Album[] getAlbumList() {
        return albumList;
    }

    public void setAlbumList(Album[] albumList) {
        this.albumList = albumList;
    }
}
