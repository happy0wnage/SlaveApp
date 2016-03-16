package com.backendless.examples.userservice.demo.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.backendless.examples.userservice.demo.Defaults;
import com.backendless.examples.userservice.demo.fileexplore.Pic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

/**
 * Created by happy on 14.03.2016.
 */
public class FilesUtil {

    private static final String TAG = FilesUtil.class.getName();

    public static void saveImage(Pic pic) {
        Bitmap bitmap = pic.getBitmap();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + Defaults.DEFAULT_SDCARD_FOLDER);
        if (!myDir.exists()) {
            myDir.mkdir();
        }
        myDir.mkdirs();
        Log.e(TAG, pic.getName());
        File file = new File(myDir, pic.getName());
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Log.e(TAG, "Ok");


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String path = cursor.getString(column_index);
                return path;
            } else {
                Log.e(TAG, "Error");
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static File generateFileOnSD(String sFileName, String sBody) {
        File root = new File(Environment.getExternalStorageDirectory(), "shared_with_me");
        try {
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            return gpxfile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void showToast(String msg, Context context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
