/*
package com.backendless.examples.userservice.demo.fileexplore;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.R;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.FileInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileChooserBanan extends ListActivity {

    private String currentDir;
    private FileArrayAdapter adapter;
    private String rootDir;
    private static final String TAG = FileChooserBanan.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootDir = Backendless.UserService.CurrentUser().getUserId();
        fill(rootDir);
    }

    private void fill(String dir) {
        Backendless.Files.listing(dir, new AsyncCallback<BackendlessCollection<FileInfo>>() {
            @Override
            public void handleResponse(BackendlessCollection<FileInfo> fileInfoBackendlessCollection) {
                List<Item> dir = new ArrayList<>();
                List<Item> fls = new ArrayList<>();

                for (FileInfo fileInfo : fileInfoBackendlessCollection.getData()) {
                    Log.e(TAG, fileInfo.getName());
                    if (fileInfo.getSize() == null || fileInfo.getSize() == 0) {
                        dir.add(new Item(fileInfo.getName(), fileInfo.getPublicUrl(), "directory_icon"));
                    } else {
                        dir.add(new Item(fileInfo.getName(), fileInfo.getPublicUrl(), "file_icon"));
                    }
                }
                Collections.sort(dir);
                Collections.sort(fls);
                dir.addAll(fls);
//                if (!dir.equals(rootDir))
//                    dir.add(0, new Item("..", "Parent Directory", "", f.getParent(), "directory_up"));
                adapter = new FileArrayAdapter(FileChooserBanan.this, R.layout.file_view, dir);
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(TAG, backendlessFault.getMessage());
            }

        });
        this.setListAdapter(adapter);
        */
/*this.setTitle("Current Dir: " + f.getName());
        List<Item> dir = new ArrayList<Item>();
        List<Item> fls = new ArrayList<Item>();
        try {
            for (File ff : dirs) {
                Date lastModDate = new Date(ff.lastModified());
                DateFormat formater = DateFormat.getDateTimeInstance();
                String date_modify = formater.format(lastModDate);
                if (ff.isDirectory()) {


                    File[] fbuf = ff.listFiles();
                    int buf = 0;
                    if (fbuf != null) {
                        buf = fbuf.length;
                    } else buf = 0;
                    String num_item = String.valueOf(buf);
                    if (buf == 0) num_item = num_item + " item";
                    else num_item = num_item + " items";

                    //String formated = lastModDate.toString();
                    dir.add(new Item(ff.getName(), num_item, date_modify, ff.getAbsolutePath(), "directory_icon"));
                } else {

                    fls.add(new Item(ff.getName(), ff.length() + " Byte", date_modify, ff.getAbsolutePath(), "file_icon"));
                }
            }
        } catch (Exception e) {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if (!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0, new Item("..", "Parent Directory", "", f.getParent(), "directory_up"));
        adapter = new FileArrayAdapter(FileChooserBanan.this, R.layout.file_view, dir);
        this.setListAdapter(adapter);*//*

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Item o = adapter.getItem(position);
        if (o.getImage().equalsIgnoreCase("directory_icon") || o.getImage().equalsIgnoreCase("directory_up")) {
//            currentDir = new File(o.getPath());
            fill(rootDir);
        } else {
            onFileClick(o);
        }
    }

    private void onFileClick(Item o) {
        //Toast.makeText(this, "Folder Clicked: "+ currentDir, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("GetPath", currentDir.toString());
        intent.putExtra("GetFileName", o.getName());
        setResult(RESULT_OK, intent);
        finish();
    }

}
*/
