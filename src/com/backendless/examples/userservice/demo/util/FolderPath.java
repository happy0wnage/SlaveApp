package com.backendless.examples.userservice.demo.util;

import android.util.Log;
import android.widget.TextView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.Defaults;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.FileInfo;

import org.w3c.dom.Text;

/**
 * Created by happy on 15.03.2016.
 */
public class FolderPath {

    private static String TAG;

    private String path = "";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private String folder;

    public FolderPath(Class clazz, String folder) {
        TAG = clazz.getName();
        this.folder = folder;
    }

    public void createFolderPath() {
        Backendless.Files.listing(Defaults.DEFAULT_PATH_ROOT, "*", true, new AsyncCallback<BackendlessCollection<FileInfo>>() {
            @Override
            public void handleResponse(BackendlessCollection<FileInfo> fileInfo) {
                for (FileInfo info : fileInfo.getData()) {
                    String publicURL = info.getPublicUrl();

                    if (info.getName().equals(folder)) {
                        setPath(publicURL.substring(publicURL.indexOf(Defaults.DEFAULT_PATH_ROOT)));
                        break;
                    }

                }
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(TAG, backendlessFault.getMessage());
            }
        });
    }


}