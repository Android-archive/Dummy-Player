package com.amsavarthan.ztunes;

public class Feed extends PostId{

    private String user_id,user_name,user_photo,timestamp,song_link,image,caption,likes,shares,color,type;

    public Feed() {
    }

    public Feed(String user_id, String user_name, String user_photo, String timestamp, String song_link, String image, String caption, String likes, String shares, String color, String type) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_photo = user_photo;
        this.timestamp = timestamp;
        this.song_link = song_link;
        this.image = image;
        this.caption = caption;
        this.likes = likes;
        this.shares = shares;
        this.color = color;
        this.type = type;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSong_link() {
        return song_link;
    }

    public void setSong_link(String song_link) {
        this.song_link = song_link;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
