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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.examples.userservice.demo.fileexplore.FileChooser;
import com.backendless.examples.userservice.demo.util.FilesUtil;
import com.backendless.examples.userservice.demo.util.FolderPath;
import com.backendless.examples.userservice.demo.util.MakePath;
import com.backendless.examples.userservice.demo.util.Validation;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.backendless.files.FileInfo;

import java.io.File;
import java.util.UUID;

public class FunctionalActivity extends Activity {

    private static final int PICKFILE_RESULT_CODE = 1;

    private String TAG = FunctionalActivity.class.getName();

    private Button takePhotoButton;

    private static final int REQUEST_PATH = 6;

    private String curFileName;

    private EditText edittext;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.functional);
        edittext = (EditText) findViewById(R.id.filePath);

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

        findViewById(R.id.browseUploadedButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText browseFolderText = (EditText) findViewById(R.id.browseFolderText);
                final String folderName = browseFolderText.getText().toString();
                if (Validation.validateEditText(browseFolderText)) {
                    Backendless.Files.listing(Defaults.DEFAULT_PATH_ROOT, "*", true, new AsyncCallback<BackendlessCollection<FileInfo>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<FileInfo> fileInfo) {
                            String path = "";
                            boolean flag = false;
                            for (FileInfo info : fileInfo.getData()) {
                                String publicURL = info.getPublicUrl();
                                if (info.getName().equals(folderName)) {
                                    path = publicURL.substring(publicURL.indexOf(Defaults.DEFAULT_PATH_ROOT));
                                    flag = true;
                                    break;
                                }
                            }
                            if (!flag) {
                                Log.e(TAG, "No such directory");
                            }
                            Log.e(TAG, "++++++++++++++++++++++++++++" + path);
                            Intent intent = new Intent(FunctionalActivity.this, BrowseActivity.class);
                            intent.putExtra("folder", path);
                            startActivity(intent);
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            Log.e(TAG, backendlessFault.getMessage());
                        }
                    });

                } else {
                    Log.e(TAG, "ERROR");
                    Intent intent = new Intent(FunctionalActivity.this, BrowseActivity.class);
                    intent.putExtra("folder", "");
                    startActivity(intent);
                }


            }
        });

        findViewById(R.id.deleteServerFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText deleteFolderText = (EditText) findViewById(R.id.deleteFolderText);
                if (Validation.validateEditText(deleteFolderText)) {
                    Backendless.Files.listing(Defaults.DEFAULT_PATH_ROOT, "*", true, new AsyncCallback<BackendlessCollection<FileInfo>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<FileInfo> fileInfo) {
                            boolean flag = false;
                            for (FileInfo info : fileInfo.getData()) {
                                String publicURL = info.getPublicUrl();
                                if (info.getName().equals(deleteFolderText.getText().toString())) {
                                    String newPath = publicURL.substring(publicURL.indexOf(Defaults.DEFAULT_PATH_ROOT));
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
        });


        takePhotoButton = (Button) findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, Defaults.CAMERA_REQUEST);
            }
        });
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

                TextView textView = (TextView) findViewById(R.id.filePathField);
                File file = new File(path);
                textView.setText("Uploaded file: " + file.getPath());

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                if (bitmap == null) {
                    finishActivity(RESULT_CANCELED);
                    return;
                }
                /*Drawable drawable = new BitmapDrawable(bitmap);
                drawable.setAlpha(50);
*/
                String name = UUID.randomUUID().toString() + ".png";

                StringBuilder folder = new StringBuilder("/uploaded");
                EditText uploadFolderName = (EditText) findViewById(R.id.uploadFolderText);
                if (Validation.validateEditText(uploadFolderName)) {
                    folder.append("/").append(uploadFolderName.getText());
                }

                Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.PNG, 10, name, Defaults.DEFAULT_PATH_ROOT + folder, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile backendlessFile) {
                        ImageEntity entity = new ImageEntity(System.currentTimeMillis(), backendlessFile.getFileURL());
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
}