package dk.au.mad21spring.project.au600963.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import dk.au.mad21spring.project.au600963.location.NearbyShopLocation;
import dk.au.mad21spring.project.au600963.location.NearbyShopLocationLoader;
import java.util.ArrayList;
import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.constants.Constants;

//  Demos used to code MAP-functionality
//  https://www.youtube.com/watch?v=rNYaEFl6Fms&list=PLdHg5T0SNpN3GBUmpGqjiKGMcBaRT2A-m&index=1&t=4s    // GetLocation
//  https://www.youtube.com/watch?v=4eWoXPSpA5Y&list=PLdHg5T0SNpN3GBUmpGqjiKGMcBaRT2A-m&index=2         // Request Location Updates
//  https://www.youtube.com/watch?v=GT-Br9iIqC0&list=PLdHg5T0SNpN3GBUmpGqjiKGMcBaRT2A-m&index=3         // Google Map
//  https://www.youtube.com/watch?v=mBOCAHsGkzs&list=PLdHg5T0SNpN3GBUmpGqjiKGMcBaRT2A-m&index=6         // Get user location in maps
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //array of nearby shops
    ArrayList<NearbyShopLocation> nearbyShopLocations;

    //To use Google play location services API we use FusedLocationProviderClient
    FusedLocationProviderClient fusedLocationProviderClient;
    //To get LocationUpdates
    LocationRequest locationRequest;


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                Log.d(Constants.TAG, "onLocationResult: " + location.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Location updates
        //init
        locationRequest = locationRequest.create();
        //how often to get location updates
        locationRequest.setInterval(4000);
        //should be able to provide updates every 2 seconds
        locationRequest.setFastestInterval(2000);

        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        //Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.map);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.recipes:
                        startActivity(new Intent(getApplicationContext(), ListActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        //used for load in nearby shops
        NearbyShopLocationLoader loader = new NearbyShopLocationLoader(this);
        nearbyShopLocations = loader.getNearbyShopLocationList();
    }

    //Method start locationUpdates
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }

    //Method stop locationUpdates
    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    //check if settings is correct, and start of location updates
    private void checkSettingsAndStartLocationUpdates() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device good -> start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(MapsActivity.this, 1001);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    //Method asking for location permission
    private void askLocationPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, Constants.LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, Constants.LOCATION_REQUEST_CODE);
            }
        }
    }

    //Method calling other methods if permission granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
                checkSettingsAndStartLocationUpdates();
                enableUserLocation();
                zoomToUserLocation();
            } else {
                // permission not granted

            }
        }
    }

    //the new asynch way of getting the map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Setting map type
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            enableUserLocation();
            zoomToUserLocation();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.ACCES_LOCATION_REQUESTCODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.ACCES_LOCATION_REQUESTCODE);
            }
        }

        showNearbyShops();

    }

    //enabling userlocation button
    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    //zoom in on current location
    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            }
        });
    };

    //Code from TheArnieExerciseFinder code demo, L9 Sensors, Location and Maps
    //Method for finding nearby shops by their latitude and longitude
    private void showNearbyShops(){

        //showingExercises = true;
        if(nearbyShopLocations!=null && nearbyShopLocations.size()>0){

            NearbyShopLocation tempLocation;
            LatLngBounds bounds;
            LatLngBounds.Builder allShops = new LatLngBounds.Builder();
            for(int i=0; i<nearbyShopLocations.size(); i++) {
                tempLocation = nearbyShopLocations.get(i);
                //calc bounding box
                allShops.include(new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude()));

                //add markers
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude()))
                        .title(tempLocation.getName())
                        .snippet(tempLocation.getDescription())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.rema1000lille))
                );

            }
            bounds = allShops.build();

            //use bounding box to zoom properly
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
        }
    }


}