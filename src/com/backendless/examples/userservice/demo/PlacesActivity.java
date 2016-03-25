package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.util.Constants;
import com.backendless.examples.userservice.demo.util.Place;
import com.backendless.examples.userservice.demo.util.Validation;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoCategory;
import com.backendless.geo.GeoPoint;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by happy on 19.03.2016.
 */
public class PlacesActivity extends Activity {

    private static final String TAG = PlacesActivity.class.getName();

    private List<GeoCategory> geoCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myplaces);

        findViewById(R.id.newCategoryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText categoryNameField = (EditText) findViewById(R.id.categoryNameField);
                if (Validation.validateEditText(categoryNameField)) {
                    final String category = categoryNameField.getText().toString();
                    Backendless.Geo.addCategory(category, new AsyncCallback<GeoCategory>() {
                        @Override
                        public void handleResponse(GeoCategory geoCategory) {
                            showToast("Category added: " + category);
                            finish();
                            startActivity(getIntent());
                        }

                        @Override
                        public void handleFault(BackendlessFault backendlessFault) {
                            showToast(backendlessFault.getMessage());
                        }
                    });
                }
            }
        });

        findViewById(R.id.myPlacesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, BrowsePlacesActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.addGeoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final GeoPoint geoPoint = new GeoPoint();
                Map<String, GeoPoint> geoPointMap = (Map<String, GeoPoint>) Backendless.UserService.CurrentUser().getProperty(Constants.UserProperty.MY_LOCATION);
                for (Map.Entry<String, GeoPoint> geo : geoPointMap.entrySet()) {
                    switch (geo.getKey()) {
                        case "latitude":
                            geoPoint.setLatitude(Double.parseDouble(geo.getValue() + ""));
                            break;
                        case "longitude":
                            geoPoint.setLongitude(Double.parseDouble(geo.getValue() + ""));
                            break;

                    }
                }

                final BackendlessUser user = Backendless.UserService.CurrentUser();

                final Place place = new Place(user.getEmail());
                EditText editText = (EditText) findViewById(R.id.geoDescriptionText);

                Set<String> hashTag = new HashSet<>();

                String description = String.valueOf(editText.getText());
                if(!Validation.validateString(description)) {
                    showToast("Description is empty");
                    return;
                }

                Log.e(TAG, "Description: " + description);
                String[] words = description.split(" ");
                Log.e(TAG, Arrays.toString(words));

                for (String word : words) {
                    if (word.startsWith("#")) {
                        hashTag.add(word);
                    }
                }

                Log.e(TAG, "Hash tags: " + hashTag);

                place.setDescription(description);
                place.setAddDate(new Date());

                final Spinner categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
                String category = (String) categorySpinner.getSelectedItem();
                final List<String> categories = Arrays.asList(category);

                geoPoint.setCategories(categories);

                final Map<String, Object> meta = new HashMap<>();
                meta.put("HashTag", hashTag);
                geoPoint.setMetadata(meta);
                place.setLatitude(geoPoint.getLatitude());
                place.setLongitude(geoPoint.getLongitude());

                String imageUrl;
                if ((imageUrl = getIntent().getStringExtra(Defaults.URL)) != null) {
                    place.setUrlPath(imageUrl);
                }

                Log.e(TAG, "Place object: " + place);

                final GeoPoint geo = new GeoPoint(place.getLatitude(), place.getLongitude(), categories, meta);
                Backendless.Geo.savePoint(geo, new AsyncCallback<GeoPoint>() {
                    @Override
                    public void handleResponse(GeoPoint geoPoint) {
                        showToast("GeoPoint to user: " + user.getEmail() + " uploaded");

                        Backendless.Persistence.save(place, new AsyncCallback<Place>() {
                            @Override
                            public void handleResponse(Place place) {
                                showToast("New place uploaded (DataObject)");

                                GeoPoint geo = new GeoPoint(place.getLatitude(), place.getLongitude(), categories, meta);
                                Log.e(TAG, "New geopoint: " + geo.toString());

                            }

                            @Override
                            public void handleFault(BackendlessFault backendlessFault) {
                                Log.e(TAG, backendlessFault.getMessage());
                                showToast(backendlessFault.getMessage());
                                Backendless.Geo.removePoint(geo, new AsyncCallback<Void>() {
                                    @Override
                                    public void handleResponse(Void aVoid) {
                                        Log.e(TAG, "Saved geopoint removed");
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault backendlessFault) {
                                        Log.e(TAG, backendlessFault.getMessage());
                                    }
                                });
                            }
                        });

                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.e(TAG, backendlessFault.getMessage());
                        showToast(backendlessFault.getMessage());

                    }
                });

        }
    }

    );

    final Spinner geoUnitsSpinner = (Spinner) findViewById(R.id.categorySpinner);

    Backendless.Geo.getCategories(new DefaultCallback<List<GeoCategory>>(this)

    {
        @Override
        public void handleResponse (List < GeoCategory > response) {
        geoCategories = response;
        String[] geoCategoriesStringArray = new String[geoCategories.size()];
        for (int i = 0; i < geoCategories.size(); i++) {
            geoCategoriesStringArray[i] = geoCategories.get(i).getName();
        }
        ArrayAdapter<CharSequence> geoUnitsSpinnerAdapter = new ArrayAdapter<CharSequence>(PlacesActivity.this, android.R.layout.simple_spinner_item, geoCategoriesStringArray);
        geoUnitsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        geoUnitsSpinner.setAdapter(geoUnitsSpinnerAdapter);

        super.handleResponse(response);
    }
    }

    );


    final Button browseImageGeoButton = (Button) findViewById(R.id.browseImageGeoButton);
    final CheckBox checkBox = (CheckBox) findViewById(R.id.availableCheckBox);
    checkBox.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View v){
        if (checkBox.isChecked()) {
            browseImageGeoButton.setEnabled(true);
        } else {
            browseImageGeoButton.setEnabled(false);
        }
    }
    }

    );


    browseImageGeoButton.setOnClickListener(new View.OnClickListener()

    {
        @Override
        public void onClick (View v){
        Intent intent = new Intent(PlacesActivity.this, BrowseActivity.class);
        intent.putExtra(Constants.Extra.FOLDER, "");
        intent.putExtra(Defaults.CODE, Defaults.GEO_CODE);
        startActivity(intent);
    }
    }

    );

    findViewById(R.id.goBackToProfile)

    .

    setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick (View v){
            Intent intent = new Intent(PlacesActivity.this, ProfileActivity.class);
            startActivity(intent);
        }
    }

    );
}

    private void showToast(String msg) {
        Toast.makeText(PlacesActivity.this, msg, Toast.LENGTH_SHORT).show();
    }


}
