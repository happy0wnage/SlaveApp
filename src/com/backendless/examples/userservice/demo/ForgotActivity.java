package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

/**
 * Created by happy on 13.03.2016.
 */
public class ForgotActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.restore);

        final EditText emailField = (EditText) findViewById(R.id.emailRestoreField);

        findViewById(R.id.emailRestoreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                if (email == null || email.equals("")) {
                    showToast("Email cannot be empty");
                    return;
                }
                BackendlessUser user = new BackendlessUser();
                user.setEmail(email);

                Backendless.UserService.restorePassword(email, new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void aVoid) {
                        showToast("Check your email");
                        return;
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        showToast("Error");
                        return;
                    }

                });
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
