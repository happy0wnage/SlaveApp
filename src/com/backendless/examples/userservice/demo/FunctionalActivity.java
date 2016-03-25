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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.examples.userservice.demo.fileexplore.FileChooser;
import com.backendless.examples.userservice.demo.fileexplore.Pic;
import com.backendless.examples.userservice.demo.util.Constants;
import com.backendless.examples.userservice.demo.util.FilesUtil;
import com.backendless.examples.userservice.demo.util.Validation;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.files.FileInfo;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class FunctionalActivity extends Activity implements LocationListener {

    private LocationManager locationManager;

    private String provider;

    private Location location;

    private static final int ONE_MINUTE = 1000 * 60;

    private static final int PICKFILE_RESULT_CODE = 1;

    private String TAG = FunctionalActivity.class.getName();

    private Button takePhotoButton;

    private static final int REQUEST_PATH = 6;

    private String curFileName;

    private EditText edittext;

    public static String DEFAULT_PATH_ROOT;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.functional);

        final BackendlessUser user = Backendless.UserService.CurrentUser();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);

        new Thread() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            Log.e(TAG, "Trying to get position");
                            Log.e(TAG, "Provider " + provider + " has been selected.");
                            Log.e(TAG, "Latitude: " + location.getLatitude() + "\tLongtitude: " + location.getLongitude());
                            onLocationChanged(location);
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                            if (geoPoint != null) {
                                user.setProperty(Constants.UserProperty.MY_LOCATION, geoPoint);

                                Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
                                    @Override
                                    public void handleResponse(BackendlessUser backendlessUser) {
                                        Log.e(TAG, "User geoPoint updated");
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault backendlessFault) {
                                        Log.e(TAG, backendlessFault.getMessage());
                                    }
                                });
                            } else {
                                Log.e(TAG, "Geo null");
                            }
                            sleep(ONE_MINUTE);
                        }
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }.start();

        edittext = (EditText) findViewById(R.id.filePath);

        DEFAULT_PATH_ROOT = user.getUserId();

        findViewById(R.id.uploadFileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICKFILE_RESULT_CODE);
                } catch (ActivityNotFoundException ex) {
                    showToast("Please install a File Manager.");
                }
            }
        });

        findViewById(R.id.profileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                finish();
            }
        });

        findViewById(R.id.logoutButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Backendless.UserService.logout(new DefaultCallback<Void>(FunctionalActivity.this) {
                    @Override
                    public void handleResponse(Void response) {
                        super.handleResponse(response);
                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                        finish();
                    }
                });
            }
        });

        findViewById(R.id.downloadFileButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText downloadFilePath = (EditText) findViewById(R.id.downloadFilePath);
                if (Validation.validateEditText(downloadFilePath)) {
                    String path = downloadFilePath.getText().toString();
                    String whereClause = "url LIKE '%" + DEFAULT_PATH_ROOT + "%" + path + "%'";
                    Log.e(TAG, "Search query: url LIKE '%" + DEFAULT_PATH_ROOT + "%" + path + "%'");
                    BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                    dataQuery.setWhereClause(whereClause);

                    Backendless.Persistence.of(ImageEntity.class).find(dataQuery, new AsyncCallback<BackendlessCollection<ImageEntity>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<ImageEntity> response) {
                            final List<ImageEntity> imageEntities = response.getCurrentPage();
                            if (imageEntities.size() == 0) {
                                showToast("No file");
                            }
                            new Thread() {
                                @Override
                                public void run() {
                                    Looper.prepare();
                                    for (ImageEntity imageEntity : imageEntities) {
                                        Log.e(TAG, imageEntity.getUrl());
//                                        Message message = new Message();
                                        try {
                                            URL url = new URL(imageEntity.getUrl());
                                            Log.e(TAG, url.getPath());
                                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                            connection.setDoInput(true);
                                            connection.connect();
                                            InputStream input = connection.getInputStream();
                                            Pic pic = new Pic(BitmapFactory.decodeStream(input), imageEntity.getName(), url.getPath());
                                            FilesUtil.saveImage(pic);
                                            showToast("File uploaded: " + (pic).getName());
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                }
                            }.start();
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            showToast(backendlessFault.getMessage());
                            Log.e(TAG, backendlessFault.getMessage());
                        }
                    });
                }
            }
        });

        findViewById(R.id.shareToUserButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText shareFilePath = (EditText) findViewById(R.id.shareFilePath);
                EditText shareUserEmail = (EditText) findViewById(R.id.shareUserEmail);

                if (!Validation.validateEditText(shareFilePath) || !Validation.validateEditText(shareUserEmail)) {
                    showToast("Empty field");
                    return;
                }

                final String folder = shareFilePath.getText().toString();
                final String email = shareUserEmail.getText().toString();

                String whereClause = "email LIKE '" + email + "'";
                BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause(whereClause);
                Backendless.Persistence.of(BackendlessUser.class).find(dataQuery, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                        BackendlessUser user = response.getCurrentPage().get(0);
                        if (user != null) {
                            final String sharedUserEmail = user.getEmail();
                            final String id = user.getUserId();
                            Log.e(TAG, "Email: " + sharedUserEmail + "\t id: " + id);

                            BackendlessDataQuery dataQuery = new BackendlessDataQuery("url LIKE '%" + DEFAULT_PATH_ROOT + "%" + folder + "%'");
                            Backendless.Persistence.of(ImageEntity.class).find(dataQuery, new AsyncCallback<BackendlessCollection<ImageEntity>>() {
                                @Override
                                public void handleResponse(final BackendlessCollection<ImageEntity> response) {
                                    final List<ImageEntity> imageEntities = response.getCurrentPage();
                                    showToast(imageEntities.size() + " files");
                                    if (imageEntities.size() == 0) {
                                        showToast("No images found");
                                        return;
                                    }

                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Looper.prepare();
                                            for (ImageEntity imageEntity : imageEntities) {
                                                try {
                                                    Log.e(TAG, imageEntity.getUrl());

                                                    String fileName = imageEntity.getName() + ".txt";
                                                    String url = imageEntity.getUrl();

                                                    final ShareFile shareFile = new ShareFile(url, sharedUserEmail, fileName);
                                                    showToast("URI: " + url + "\tUser email: " + sharedUserEmail);

                                                    File file = FilesUtil.generateFileOnSD(fileName, url);
                                                    Backendless.Files.upload(file, id + "/" + Defaults.DEFAULT_SHARED, new AsyncCallback<BackendlessFile>() {
                                                        @Override
                                                        public void handleResponse(BackendlessFile backendlessFile) {
                                                            Backendless.Persistence.save(shareFile, new AsyncCallback<Object>() {
                                                                @Override
                                                                public void handleResponse(Object o) {
                                                                    showToast("File shared");
                                                                    Log.e(TAG, "File shared");
                                                                }

                                                                @Override
                                                                public void handleFault(BackendlessFault backendlessFault) {
                                                                    Log.e(TAG, backendlessFault.getMessage());
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void handleFault(BackendlessFault backendlessFault) {
                                                            showToast(backendlessFault.getMessage());
                                                            Log.e(TAG, backendlessFault.getMessage());
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    Log.e(TAG, e.getMessage());
                                                }
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
                        } else {
                            showToast("No such email");
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(TAG, backendlessFault.getMessage());
                    }
                });


            }
        });

        findViewById(R.id.browseUploadedButton).setOnClickListener(new View.OnClickListener() {
                                                                       @Override
                                                                       public void onClick(View view) {
                                                                           EditText browseFolderText = (EditText) findViewById(R.id.browseFolderText);

                                                                           String folderName = "";
                                                                           if (Validation.validateEditText(browseFolderText)) {
                                                                               folderName = browseFolderText.getText().toString();
                                                                           }

                                                                           Intent intent = new Intent(FunctionalActivity.this, BrowseActivity.class);
                                                                           intent.putExtra("folder", folderName);
                                                                           intent.putExtra(Defaults.CODE, 0);
                                                                           startActivity(intent);


                                                                       }
                                                                   }
        );

        findViewById(R.id.deleteServerFolder)

                .

                        setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   final EditText deleteFolderText = (EditText) findViewById(R.id.deleteFolderText);
                                                   if (Validation.validateEditText(deleteFolderText)) {
                                                       Backendless.Files.listing(DEFAULT_PATH_ROOT, "*", true, new AsyncCallback<BackendlessCollection<FileInfo>>() {
                                                           @Override
                                                           public void handleResponse(BackendlessCollection<FileInfo> fileInfo) {
                                                               boolean flag = false;
                                                               for (FileInfo info : fileInfo.getData()) {
                                                                   String publicURL = info.getPublicUrl();
                                                                   if (info.getName().equals(deleteFolderText.getText().toString())) {
                                                                       String newPath = publicURL.substring(publicURL.indexOf(DEFAULT_PATH_ROOT));
                                                                       Log.e(TAG, newPath);
                                                                       Backendless.Files.removeDirectory(newPath, new AsyncCallback<Void>() {
                                                                           @Override
                                                                           public void handleResponse(Void aVoid) {
                                                                               showToast("Directory has been deleted");
                                                                               Log.e(TAG, "Directory has been deleted");
                                                                           }

                                                                           @Override
                                                                           public void handleFault(BackendlessFault backendlessFault) {
                                                                               showToast(backendlessFault.getMessage());
                                                                               Log.e(TAG, backendlessFault.getMessage());
                                                                           }
                                                                       });
                                                                       flag = true;
                                                                       break;
                                                                   }
                                                               }
                                                               if (!flag) {
                                                                   showToast("No such directory");
                                                               }
                                                           }

                                                           @Override
                                                           public void handleFault(BackendlessFault backendlessFault) {
                                                               Log.e(TAG, backendlessFault.getMessage());
                                                               showToast(backendlessFault.getMessage());
                                                           }
                                                       });
                                                   } else {
                                                       showToast("Select folder");
                                                       Log.e(TAG, "Select folder");
                                                   }
                                               }
                                           }

                        );


        takePhotoButton = (Button)

                findViewById(R.id.takePhotoButton);

        takePhotoButton.setOnClickListener(new View.OnClickListener()

                                           {
                                               @Override
                                               public void onClick(View view) {
                                                   Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                                   startActivityForResult(cameraIntent, Defaults.CAMERA_REQUEST);
                                               }
                                           }

        );
    }

    public void getFile(View view) {
        Intent intent = new Intent(this, FileChooser.class);
        startActivityForResult(intent, REQUEST_PATH);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_PATH:
                curFileName = data.getStringExtra("GetFileName");
                edittext.setText(curFileName);
                break;
            case PICKFILE_RESULT_CODE:
                Uri uri = data.getData();
                String path = FilesUtil.getRealPathFromURI(FunctionalActivity.this, uri);

                TextView textView = (TextView) findViewById(R.id.filePath);
                final File file = new File(path);
                textView.setText("Uploaded file: " + file.getPath());

                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if (bitmap == null) {
                    finishActivity(RESULT_CANCELED);
                    return;
                }

                StringBuilder folder = new StringBuilder("/uploaded");
                EditText uploadFolderName = (EditText) findViewById(R.id.uploadFolderText);
                if (Validation.validateEditText(uploadFolderName)) {
                    folder.append("/").append(uploadFolderName.getText());
                }

                Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.PNG, 100, file.getName(), DEFAULT_PATH_ROOT + folder, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile backendlessFile) {
                        ImageEntity entity = new ImageEntity(System.currentTimeMillis(), backendlessFile.getFileURL(), file.getName());
                        Log.e(TAG, entity.toString());

                        Backendless.Persistence.save(entity, new BackendlessCallback<ImageEntity>() {
                            @Override
                            public void handleResponse(ImageEntity imageEntity) {
                                Intent data = new Intent();
                                data.putExtra(Defaults.DATA_TAG, imageEntity.getUrl());
                                setResult(RESULT_OK, data);
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                showToast(fault.getMessage());
                            }
                        });
                        showToast("Uploaded");
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(TAG, backendlessFault.getMessage());
                        showToast(backendlessFault.toString());
                    }
                });
                break;
            case Defaults.CAMERA_REQUEST:
                data.setClass(getBaseContext(), UploadingActivity.class);
                startActivityForResult(data, Defaults.URL_REQUEST);
                break;
            case Defaults.URL_REQUEST:
                takePhotoButton.setText(getResources().getText(R.string.takeAnotherPhoto));
                break;
        }
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, ONE_MINUTE, 10, this);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }


    @Override
    public void onLocationChanged(Location loc) {
        location = loc;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        showToast("Enabled new provider " + provider);
        checkEnabled();
    }

    @Override
    public void onProviderDisabled(String provider) {
        showToast("Disabled provider " + provider);
        checkEnabled();
    }

    private void checkEnabled() {
        Log.e(TAG, "Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        Log.e(TAG, "Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }
}