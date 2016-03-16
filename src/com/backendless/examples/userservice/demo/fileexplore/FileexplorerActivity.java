package com.backendless.examples.userservice.demo.fileexplore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.backendless.examples.userservice.demo.R;

public class FileexplorerActivity extends Activity {

    private static final int REQUEST_PATH = 1;

    private String curFileName;

    private EditText edittext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.functional);
        edittext = (EditText) findViewById(R.id.filePath);
    }

    public void getFile(View view) {
        Intent intent = new Intent(this, FileChooser.class);
        startActivityForResult(intent, REQUEST_PATH);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                curFileName = data.getStringExtra("GetFileName");
                edittext.setText(curFileName);
            }
        }
    }
}
