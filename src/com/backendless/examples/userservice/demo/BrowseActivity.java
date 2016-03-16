/*
 * ********************************************************************************************************************
 *  <p/>
 *  BACKENDLESS.COM CONFIDENTIAL
 *  <p/>
 *  ********************************************************************************************************************
 *  <p/>
 *  Copyright 2012 BACKENDLESS.COM. All Rights Reserved.
 *  <p/>
 *  NOTICE: All information contained herein is, and remains the property of Backendless.com and its suppliers,
 *  if any. The intellectual and technical concepts contained herein are proprietary to Backendless.com and its
 *  suppliers and may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret
 *  or copyright law. Dissemination of this information or reproduction of this material is strictly forbidden
 *  unless prior written permission is obtained from Backendless.com.
 *  <p/>
 *  ********************************************************************************************************************
 */

package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.util.FolderPath;
import com.backendless.examples.userservice.demo.util.Validation;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BrowseActivity extends Activity {
    private static int itemWidth;
    private static ImageAdapter imageAdapter;
    private static int padding;
    private String TAG = BrowseActivity.class.getName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse);

        padding = (int) getResources().getDimension(R.dimen.micro_padding);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        itemWidth = (screenWidth - (2 * (int) getResources().getDimension(R.dimen.default_margin))) / 3;

        GridView gridView = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        showToast("Downloading images...");

        String folder = Defaults.DEFAULT_PATH_ROOT;
        String path = (String) getIntent().getExtras().get("folder");
        if(!"".equals(path)) {
            folder = path;
        }

        Log.e(TAG, "-----------------------------------------------" + folder);

        String whereClause = "url LIKE '%" + folder + "%'";
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        Backendless.Persistence.of(ImageEntity.class).find(dataQuery, new AsyncCallback<BackendlessCollection<ImageEntity>>() {
            @Override
            public void handleResponse(final BackendlessCollection<ImageEntity> response) {
                final List<ImageEntity> imageEntities = response.getCurrentPage();

                new Thread() {
                    @Override
                    public void run() {

                        for (ImageEntity imageEntity : imageEntities) {
                            Log.e(TAG, imageEntity.getUrl());
                            Message message = new Message();
                            try {
                                URL url = new URL(imageEntity.getUrl());
                                Log.e(TAG, url.getPath());
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();
                                message.obj = BitmapFactory.decodeStream(input);
                            } catch (Exception e) {
                                message.obj = e;
                            }
                            imagesHandler.sendMessage(message);
                        }
                    }
                }.start();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showToast("Make some upload first");
                Log.e(TAG, fault.getMessage());
            }
        });
    }


    private Handler imagesHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Object result = message.obj;

            if (result instanceof Bitmap)
                imageAdapter.add((Bitmap) result);
            return true;
        }
    });

    private static class ImageAdapter extends BaseAdapter {
        private Context context;
        private List<Bitmap> images = new ArrayList<>();

        public ImageAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        public void add(Bitmap bitmap) {
            images.add(bitmap);
            notifyDataSetChanged();
        }

        public Bitmap getItem(int position) {
            return images.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(itemWidth, itemWidth));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(padding, padding, padding, padding);
            } else
                imageView = (ImageView) convertView;

            imageView.setImageBitmap(getItem(position)); // Load image into ImageView

            return imageView;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(BrowseActivity.this, msg, Toast.LENGTH_SHORT).show();

    }
}