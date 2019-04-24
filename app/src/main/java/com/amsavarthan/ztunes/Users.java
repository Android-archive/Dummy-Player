package com.amsavarthan.ztunes;

public class Users extends UserId{

    private String account_type,date_of_creation,name,picture,last_login,online;

    public Users() {
    }

    public Users(String account_type, String date_of_creation, String name, String picture, String last_login, String online) {
        this.account_type = account_type;
        this.date_of_creation = date_of_creation;
        this.name = name;
        this.picture = picture;
        this.last_login = last_login;
        this.online = online;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }

    public String getDate_of_creation() {
        return date_of_creation;
    }

    public void setDate_of_creation(String date_of_creation) {
        this.date_of_creation = date_of_creation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLast_login() {
        return last_login;
    }

    public void setLast_login(String last_login) {
        this.last_login = last_login;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}
