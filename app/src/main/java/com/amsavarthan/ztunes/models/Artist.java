package com.amsavarthan.ztunes.models;

public class Artist extends UserId {

    private String name,photo_link;

    public Artist() {
    }

    public Artist(String name, String photo_link) {
        this.name = name;
        this.photo_link = photo_link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_link() {
        return photo_link;
    }

    public void setPhoto_link(String photo_link) {
        this.photo_link = photo_link;
    }
}
