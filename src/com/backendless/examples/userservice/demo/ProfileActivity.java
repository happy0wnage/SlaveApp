package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.util.Constants;
import com.backendless.examples.userservice.demo.util.Validation;
import com.backendless.exceptions.BackendlessFault;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
                intent.putExtra(Defaults.CODE, Defaults.AVATAR_CODE);
                startActivity(intent);
            }
        });

        findViewById(R.id.toFunctionActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, FunctionalActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.trackLocationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, PlacesActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.updateInfoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameProfileLabel = (EditText) findViewById(R.id.nameProfileLabel);
                String name = null;
                if(Validation.validateEditText(nameProfileLabel)) {
                    name = nameProfileLabel.getText().toString();
                }

                EditText emailProfileLabel = (EditText) findViewById(R.id.emailProfileLabel);
                String email = null;
                if(Validation.validateEditText(emailProfileLabel)) {
                    email = emailProfileLabel.getText().toString();
                }

                EditText countryProfileLabel = (EditText) findViewById(R.id.countryProfileLabel);
                String country = null;
                if(Validation.validateEditText(countryProfileLabel)) {
                    country = countryProfileLabel.getText().toString();
                }

                BackendlessUser user = Backendless.UserService.CurrentUser();
                user.setProperty(Constants.UserProperty.NAME, name);
                user.setEmail(email);
                user.setProperty(Constants.UserProperty.COUNTRY, country);

                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser backendlessUser) {
                        showToast("User updated");
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        showToast(backendlessFault.getMessage());
                    }
                });
            }
        });
    }

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
                max = (int) (scale * 110);
                avatarView.setImageBitmap((Bitmap) result);
                avatarView.setScaleType(ImageView.ScaleType.CENTER);
                avatarView.getLayoutParams().height = max;
                avatarView.setMaxWidth(max);
                avatarView.requestLayout();
                Log.e(TAG, "avatar uploaded");
            }
            return true;
        }
    });

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
