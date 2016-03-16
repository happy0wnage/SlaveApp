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
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.fileexplore.Pic;
import com.backendless.examples.userservice.demo.util.FilesUtil;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.FileInfo;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BrowseActivity extends Activity {
    private static int itemWidth;
    private static ImageAdapter imageAdapter;
    private static int padding;
    private String TAG = BrowseActivity.class.getName();
    private String path;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse);

        padding = (int) getResources().getDimension(R.dimen.micro_padding);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        itemWidth = (screenWidth - (2 * (int) getResources().getDimension(R.dimen.default_margin))) / 3;

        final GridView gridView = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        showToast("Downloading images...");

        final String folder = (String) getIntent().getExtras().get(Defaults.FOLDER);

        String whereClause;
        if (folder.isEmpty()) {
            whereClause = "url LIKE '%" + FunctionalActivity.DEFAULT_PATH_ROOT + "%'";
        } else {
            whereClause = "url LIKE '%" + FunctionalActivity.DEFAULT_PATH_ROOT + "%" +folder + "%'";
            Log.e(TAG, "Selected folder: " + folder);
        }

        if (folder.equals(Defaults.DEFAULT_SHARED)) {
            Backendless.Persistence.of(UserFile.class).find(new AsyncCallback<BackendlessCollection<UserFile>>() {
                @Override
                public void handleResponse(BackendlessCollection<UserFile> response) {
                    final List<UserFile> userFiles = response.getCurrentPage();
                    showToast(userFiles.size() + " files");
                    if (userFiles.size() == 0) {
                        showToast("First make uploads");
                        return;
                    }
                    new Thread() {
                        @Override
                        public void run() {
                            for (UserFile userFile : userFiles) {
                                Message message = new Message();
                                try {
                                    Log.e(TAG, userFile.getUrl());
                                    URL url = new URL(userFile.getUrl());
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
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });
        } else {
            Backendless.Persistence.of(ImageEntity.class).find(new BackendlessDataQuery(whereClause), new AsyncCallback<BackendlessCollection<ImageEntity>>() {
                        @Override
                        public void handleResponse(final BackendlessCollection<ImageEntity> response) {
                            final List<ImageEntity> imageEntities = response.getCurrentPage();
                            showToast(imageEntities.size() + " files");
                            if (imageEntities.size() == 0) {
                                showToast("First make uploads");
                                return;
                            }
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
                    }
            );
        }
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
            final ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(itemWidth, itemWidth));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(padding, padding, padding, padding);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BitmapDrawable btmpDr = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bmp = btmpDr.getBitmap();
                        Random random = new Random(1000);
                        FilesUtil.saveImage(new Pic(bmp, ("image" + random + ".png")));
                        FilesUtil.showToast("File saved to Downloads", v.getContext());
                    }
                });
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