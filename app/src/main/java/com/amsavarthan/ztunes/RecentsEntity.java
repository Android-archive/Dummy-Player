package com.amsavarthan.ztunes;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "recents")
public class RecentsEntity {

    public RecentsEntity() {
    }

    @Ignore
    public RecentsEntity(int id, String name, String album, String artist, String link, String genre, String art) {
        this.id = id;
        this.name = name;
        this.album = album;
        this.artist = artist;
        this.link = link;
        this.genre = genre;
        this.art = art;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="album")
    private String album;

    @ColumnInfo(name="artist")
    private String artist;

    @ColumnInfo(name="link")
    private String link;

    @ColumnInfo(name="genre")
    private String genre;

    @ColumnInfo(name="art")
    private String art;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }
}
