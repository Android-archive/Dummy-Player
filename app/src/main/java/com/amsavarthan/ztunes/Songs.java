package com.amsavarthan.ztunes;

public class Songs {

    private String name,artist,genre,link,album,new_release,art;

    public Songs() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getNew_release() {
        return new_release;
    }

    public void setNew_release(String new_release) {
        this.new_release = new_release;
    }

    public Songs(String name, String artist, String genre, String link, String album, String new_release, String art) {
        this.name = name;
        this.artist = artist;
        this.genre = genre;
        this.link = link;
        this.album = album;
        this.new_release = new_release;
        this.art = art;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }
}
