package com.amsavarthan.ztunes.models;

public class Album {

    private String name,art;

    public Album() {
    }

    public Album(String name, String art) {
        this.name = name;
        this.art = art;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArt() {
        return art;
    }

    public void setArt(String art) {
        this.art = art;
    }
}
