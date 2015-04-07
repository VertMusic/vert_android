package com.example.brencodie.vertmusic;

/**
 * Created by Bren Codie on 4/5/2015.
 */
public class Song {

    private String id;
    private String title;
    private String artist;

    public Song(String songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public String getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
