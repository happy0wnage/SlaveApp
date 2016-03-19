package com.backendless.examples.userservice.demo.fileexplore;

import android.graphics.Bitmap;

/**
 * Created by happy on 16.03.2016.
 */
public class Pic {

    private Bitmap bitmap;

    private String name;

    private String url;

    public Pic(Bitmap bitmap, String name, String url) {
        this.bitmap = bitmap;
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pic pic = (Pic) o;

        return !(bitmap != null ? !bitmap.equals(pic.bitmap) : pic.bitmap != null);

    }

    @Override
    public int hashCode() {
        return bitmap != null ? bitmap.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Pic{" +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
