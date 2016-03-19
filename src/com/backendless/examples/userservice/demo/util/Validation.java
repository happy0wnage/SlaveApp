package com.backendless.examples.userservice.demo.util;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by happy on 15.03.2016.
 */
public class Validation {

    public static boolean validateEditText(EditText editText) {
        if(editText == null || editText.getText().toString().isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean validateString(String text) {
        if(text == null || text.isEmpty()) {
            return false;
        }
        return true;
    }
}
