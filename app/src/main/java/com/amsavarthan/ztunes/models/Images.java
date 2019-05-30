package com.amsavarthan.ztunes.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Images {

    private String name,og_path;
    private Uri uri;
    private long id;

    public Images(String name, String og_path, Uri uri, long id) {
        this.name = name;
        this.og_path = og_path;
        this.uri = uri;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOg_path() {
        return og_path;
    }

    public void setOg_path(String og_path) {
        this.og_path = og_path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
