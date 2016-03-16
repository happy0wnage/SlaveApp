package com.backendless.examples.userservice.demo.util;

import android.util.Log;
import android.widget.Toast;

import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.Defaults;
import com.backendless.examples.userservice.demo.FunctionalActivity;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.FileInfo;

/**
 * Created by happy on 15.03.2016.
 */
public class MakePath implements AsyncCallback<BackendlessCollection<FileInfo>> {

    private static final String TAG = MakePath.class.getName();

    private String path = "";

    private String folderName;

    public MakePath(String folderName) {
        this.folderName = folderName;
    }

    @Override
    public void handleResponse(BackendlessCollection<FileInfo> fileInfo) {
        boolean flag = false;
        for (FileInfo info : fileInfo.getData()) {
            String publicURL = info.getPublicUrl();
            if (info.getName().equals(folderName)) {
                path = publicURL.substring(publicURL.indexOf(FunctionalActivity.DEFAULT_PATH_ROOT));
                Log.e(TAG, "-----------------------" + path);
                flag = true;
                break;
            }
        }
        if(!flag) {
            Log.e(TAG, "No such directory");
        }
    }

    @Override
    public void handleFault(BackendlessFault backendlessFault) {
        Log.e(TAG, backendlessFault.getMessage());
    }
}
