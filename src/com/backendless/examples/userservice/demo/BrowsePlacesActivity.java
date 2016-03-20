package com.backendless.examples.userservice.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.examples.userservice.demo.util.Place;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.Arrays;
import java.util.List;

/**
 * Created by happy on 20.03.2016.
 */
public class BrowsePlacesActivity extends Activity {

    private ListView myPlacesList;

    private List<Place> places;

    private static final String TAG = BrowsePlacesActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.places);

        initUI();

        BackendlessUser user = Backendless.UserService.CurrentUser();
        BackendlessDataQuery dataQuery = new BackendlessDataQuery("userEmail LIKE '" + user.getEmail() + "'");

        Backendless.Persistence.of(Place.class).find(dataQuery, new AsyncCallback<BackendlessCollection<Place>>() {
            @Override
            public void handleResponse(BackendlessCollection<Place> response) {
                places = response.getCurrentPage();
                Log.e(TAG, "Places size: " + places.size());

                String[] stringPlaces = new String[places.size()];
                for (int i = 0; i < places.size(); i++) {
                    stringPlaces[i] = places.get(i).toStringGeo();
                }

                myPlacesList = (ListView) findViewById(R.id.myPlacesList);
                Log.e(TAG, "Places: " + Arrays.toString(stringPlaces));
                ListAdapter adapter = new ArrayAdapter<>(BrowsePlacesActivity.this, R.layout.list_item_with_arrow, R.id.itemName, stringPlaces);
                myPlacesList.setAdapter(adapter);
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Log.e(TAG, backendlessFault.getMessage());
            }
        });

        findViewById(R.id.toMyPlace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BrowsePlacesActivity.this, PlacesActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initUI() {
        myPlacesList = (ListView) findViewById(R.id.myPlacesList);

        myPlacesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(BrowsePlacesActivity.this);
                alert.setTitle("Delete?");
                final Place place = places.get(position);
                alert.setMessage("Are you sure you want to delete " + place.toStringGeo());
                alert.setNegativeButton("Cancel", null);
                alert.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {


                        Backendless.Persistence.of(Place.class).remove(place, new AsyncCallback<Long>() {
                            @Override
                            public void handleResponse(Long aLong) {

                                BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
                                geoQuery.setLatitude(place.getLatitude());
                                geoQuery.setLongitude(place.getLongitude());

                                Backendless.Geo.getPoints(geoQuery, new AsyncCallback<BackendlessCollection<GeoPoint>>() {
                                    @Override
                                    public void handleResponse(BackendlessCollection<GeoPoint> response) {
                                        List<GeoPoint> geoPoints = response.getCurrentPage();
                                        for (GeoPoint geoPoint : geoPoints) {
                                            Log.e(TAG, "Object id: " + geoPoint.getObjectId());
                                            if (geoPoint.getObjectId().equals(place.getGeoPointId())) {
                                                Backendless.Geo.removePoint(geoPoint, new AsyncCallback<Void>() {
                                                    @Override
                                                    public void handleResponse(Void aVoid) {
                                                        Log.e(TAG, "Geopoint removed");
                                                        showToast("Geopoint removed");
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault backendlessFault) {
                                                        Log.e(TAG, backendlessFault.getMessage());
                                                    }
                                                });
                                            }
                                        }
                                        startActivity(getIntent());
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault backendlessFault) {

                                    }
                                });
                            }

                            @Override
                            public void handleFault(BackendlessFault backendlessFault) {
                                Log.e(TAG, backendlessFault.getMessage());
                            }
                        });
                    }
                });
                alert.show();
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(BrowsePlacesActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
