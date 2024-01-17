package com.example.spotify.Object;

import java.util.Vector;

public class Album {
    private String albumName;
    private String albumID;
    private Vector<String> songList;

    public Album(String albumID, String albumName, Vector<String> songList) {
        this.albumID = albumID;
        this.albumName = albumName;
        this.songList = songList;
    }

    public void addSong (String songID){
        if (!isDuplicate(songID)) this.songList.add(songID);
    }

    public boolean isDuplicate(String songID) {
        //empty
        if (this.songList.isEmpty()) return false;
        //duplicated
        for (String obj: this.songList) {
            if (obj.equals(songID)) return true;
        }
        //not duplicated
        return false;
    }
}
