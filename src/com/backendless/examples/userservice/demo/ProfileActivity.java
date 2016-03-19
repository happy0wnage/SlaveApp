package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.examples.userservice.demo.fileexplore.Pic;
import com.backendless.examples.userservice.demo.util.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Владислав on 17 мар 2016 г..
 */
public class ProfileActivity extends Activity {

    private static final String TAG = ProfileActivity.class.getName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        BackendlessUser user = Backendless.UserService.CurrentUser();
        initFields(user);

        findViewById(R.id.uploadAvatarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, BrowseActivity.class);
                intent.putExtra("folder", "");
                intent.putExtra(Defaults.CODE, Defaults.IMAGE_CODE);
//                startActivityForResult(intent, Defaults.IMAGE_CODE);
                startActivity(intent);
//                final String folder = (String) getIntent().getExtras().get(Defaults.FOLDER);
            }
        });

    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case Defaults.IMAGE_CODE:
                final String path = (String) getIntent().getExtras().get(Defaults.URL);
                try {
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    ImageView avatar = (ImageView) findViewById(R.id.avatarView);
                    avatar.setImageBitmap(bitmap);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
        }

    }*/

    private void initFields(BackendlessUser user) {
        TextView nameField = (TextView) findViewById(R.id.nameProfileLabel);
        nameField.setText(user.getProperty("name").toString());

        TextView emailField = (TextView) findViewById(R.id.emailProfileLabel);
        emailField.setText(user.getEmail());

        TextView countryField = (TextView) findViewById(R.id.countryProfileLabel);
        countryField.setText(user.getProperty("country").toString());

        if (user.getProperty(Defaults.UserDefaults.AVATAR_PATH) != null) {
            final String urlPath = user.getProperty(Defaults.UserDefaults.AVATAR_PATH).toString();

            Log.e(TAG, "Url path: " + urlPath);

            if (Validation.validateString(urlPath)) {
                new Thread() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        try {
//                            Looper.prepare();
                            URL url = new URL(urlPath);
                            Log.e(TAG, "URL path = " + url.getPath());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            message.obj = BitmapFactory.decodeStream(input);
                        } catch (IOException e) {
                            message.obj = e;
                            Log.e(TAG, "Excepton: " + e.getMessage());
                        }
                        imagesHandler.sendMessage(message);
                    }
                }.start();
            } else {
                Log.e(TAG, "Invalid value");
            }
        } else {
            Log.e(TAG, "Null avatar");
        }
    }

    private Handler imagesHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            Object result = message.obj;

            if (result instanceof Bitmap) {
                ImageView avatarView = (ImageView) findViewById(R.id.avatarView);
                int max = findViewById(R.id.inputTags).getHeight();
                final float scale = getResources().getDisplayMetrics().density;
                int padding = (int) (scale * 5);
                max = (int) (scale * 80);
                avatarView.setImageBitmap((Bitmap) result);
                avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                avatarView.getLayoutParams().height = max - padding;
                avatarView.setMaxWidth(max - padding);
                avatarView.setPadding(padding, padding, padding, padding);
                avatarView.requestLayout();
                Log.e(TAG, "avatar uploaded");
            }
            return true;
        }
    });

}
