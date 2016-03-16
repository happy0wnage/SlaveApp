package com.backendless.examples.userservice.demo.fileexplore;

import android.graphics.Bitmap;

/**
 * Created by happy on 16.03.2016.
 */
public class Pic {

    private Bitmap bitmap;

    private String name;

    public Pic(Bitmap bitmap, String name) {
        this.bitmap = bitmap;
        this.name = name;
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
}
