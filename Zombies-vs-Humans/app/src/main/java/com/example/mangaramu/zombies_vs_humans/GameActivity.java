package com.example.mangaramu.zombies_vs_humans;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.arsy.maps_library.MapRipple;
import com.example.mangaramu.zombies_vs_humans.Model.PlayerItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, ZombieConversionDialogFragment.Converted {

    ArrayMap<String, Marker> Markers = new ArrayMap<>();
    SupportMapFragment gogmymap;
    GoogleMap myMap = null;
    LocationListener locationListener;
    LocationManager locationManager;
    Location currlocation;
    LocationRequest mLocationRequest;
    LocationSettingsRequest.Builder builder;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    Activity myActivity;
    Boolean TrackLocationWithCamera, PinDrop = false;
    Boolean Firstchange = false;
    ArrayMap<String, PlayerItem> gameUsers = new ArrayMap<>();
    Activity gameContext = this;
    PullGameDataThread pullDataThread;
    FragmentTransaction fragtra;
    FragmentManager fragma;
    int powerDistance = 20;
    String LINK;
    ZombieConversionDialogFragment dialog = new ZombieConversionDialogFragment();
    MapRipple mapRipple;
    private String myUsername;


    public static final int GPS_FINE_LOCATION_SERVICE = 1;
    public static final int REQUEST_CHECK_SETTINGS = 1;

    Handler peopleUpdate = new Handler() {//handle the players being updated here!
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MarkerOptions markerOptions;
            BitmapDescriptor bd;

            if (myMap != null) {
                for (Map.Entry<String, PlayerItem> entry : gameUsers.entrySet()) {
                    // If it's not you
                    if (!entry.getKey().equals(myUsername)) {
                        bd = getColorBasedOnDistance(gameUsers.get(myUsername), entry.getValue());

                        if (Markers.containsKey(entry.getKey())) {
                            Markers.get(entry.getKey()).setPosition(new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude()));
                            Markers.get(entry.getKey()).setIcon(bd);
                        } else {
                            markerOptions = new MarkerOptions()
                                    .position(new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude()))
                                    .title(entry.getKey())
                                    .icon(bd);
                            Markers.put(entry.getKey(), myMap.addMarker(markerOptions));
                        }
                    }
                }

                if (gameUsers.get(myUsername).isZombie()) {
                    if (gameUsers.get(myUsername).getLatitude() != null) {
                        if (getSupportFragmentManager().findFragmentById(android.R.id.content) instanceof ZombieConversionDialogFragment&&dialog.username!=null)//checks to see if we already have a dialog up and are still in range
                        {
                            float[] tmp2 = new float[1];
                            Location.distanceBetween(
                                    gameUsers.get(myUsername).getLatitude(),
                                    gameUsers.get(myUsername).getLongitude(),
                                    gameUsers.get(dialog.getUsername()).getLatitude(),
                                    gameUsers.get(dialog.getUsername()).getLongitude(),
                                    tmp2);
                            if (tmp2[0] <= powerDistance) {
                                pullDataThread = new PullGameDataThread(myUsername, gameUsers, peopleUpdate, LINK);
                                pullDataThread.start();
                                return;
                            }
                            else
                            {
                                removedialog();
                            }
                        }

                        ArrayList<String> humans = new ArrayList<>(); // if we didn't have a dialog up we create one for the first person who is within range to be able to tag
                        for (Map.Entry<String, PlayerItem> entry : gameUsers.entrySet()) {
                            if (!entry.getValue().isZombie()) {
                                humans.add(entry.getKey());
                            }
                        }

                        for (String playerName : humans) {
                            float[] tmp = new float[1];
                            Location.distanceBetween(
                                    gameUsers.get(myUsername).getLatitude(),
                                    gameUsers.get(myUsername).getLongitude(),
                                    gameUsers.get(playerName).getLatitude(),
                                    gameUsers.get(playerName).getLongitude(),
                                    tmp);
                            if (tmp[0] <= powerDistance) {
                                dialog.setUsername(playerName);
                                showdialog();
                                break;
                            }
                        }
                    }
                }
            }

            pullDataThread = new PullGameDataThread(myUsername, gameUsers, peopleUpdate, LINK);
            pullDataThread.start();
        }
    };

    BitmapDescriptor getColorBasedOnDistance(PlayerItem local, PlayerItem other) {
        BitmapDescriptor bd;
        float[] results = new float[1];

        // Calculate distance between local user and other player
        if (local.getLatitude() != null) {
            Location.distanceBetween(local.getLatitude(), local.getLongitude(),
                    other.getLatitude(), other.getLongitude(), results);
        } else {
            results[0] = 30;
        }

        // Cool hues for Humans
        if (!other.isZombie()) {
            if (results[0] <= 15) {
                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            } else if ((results[0] > 15) && (results[0] <= 30)) {
                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            } else {
                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            }
            // Warm hues for Zombies
        } else if (other.isZombie()) {
            if (results[0] <= 15) {
                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            } else if ((results[0] > 15) && (results[0] <= 30)) {
//                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                bd = BitmapDescriptorFactory.defaultMarker(15);
            } else {
//                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }
            // Green for error anomalies (errors)
        } else {
            bd = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        }

        return bd;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {// should only get called once because the requested orientation is portrait
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameact);

        LINK = getResources().getString(R.string.URL);
        myUsername = getIntent().getStringExtra("Username");
        gameUsers.put(getIntent().getStringExtra("Username"),
                new PlayerItem(
                        getIntent().getStringExtra("Username"),
                        getIntent().getDoubleExtra("Latitude", 0),
                        getIntent().getDoubleExtra("Longitude", 0),
                        getIntent().getBooleanExtra("isZombie", false))); //sets the username to their playeritem, the first item in gameUsers should be the player themselves
        pullDataThread = new PullGameDataThread(myUsername, gameUsers, peopleUpdate, LINK);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// set the app to always be in portrait mode .
        myActivity = this;
        AndroidNetworking.initialize(getApplicationContext());// For android networking!

        mGoogleApiClient = new GoogleApiClient.Builder(this)//setting up a google api client to change the gps settings
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); // allows you to do location related things if you have the correct permissions
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {//on location changed takes in a location variable. It will do various tasks related to a google map by variable which are set by buttons.
//                try {
//                    currlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//                } catch (SecurityException e) {
//                    Log.e("Security Exception", e.toString());
//                }

                currlocation = location;
                gameUsers.get(myUsername).setLatitude(currlocation.getLatitude());
                gameUsers.get(myUsername).setLongitude(currlocation.getLongitude());
                LatLng tmp = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());//updates where the camera on the map is relative to where you are.

                if (mapRipple == null) {
                    mapRipple = new MapRipple(myMap, tmp, getApplicationContext());
                    mapRipple.withDistance(10);
                    mapRipple.withRippleDuration(5000);
//                    mapRipple.withNumberOfRipples(3);
//                    if (!gameUsers.valueAt(0).isZombie()) {
//                        mapRipple.withFillColor(Color.BLUE);
//                    } else {
//                        mapRipple.withFillColor(Color.RED);
//                    }
//                    mapRipple.withStrokeColor(Color.BLACK);
//                    mapRipple.withStrokewidth(10);      // 10dp
//                    mapRipple.withDistance(10);      // 10 metres radius
//                    mapRipple.withRippleDuration(10000);    // 10000ms
//                    mapRipple.withTransparency(0.8f);
//
//                    if (!mapRipple.isAnimationRunning()) {
//                        mapRipple.startRippleMapAnimation();
//                    }
                    mapRipple.startRippleMapAnimation();
                } else {
                    mapRipple.withLatLng(tmp);
                }

                if (myMap != null) {
                    CameraUpdate camup = CameraUpdateFactory.newLatLngZoom(tmp, 18.0f);
                    myMap.animateCamera(camup);
                    //myMap.moveCamera(camup);
                }

                AndroidNetworking.initialize(gameContext);

                ////////////////////////////////////////////////////////////////////////
                Log.d("GameActivity", gameUsers.get(myUsername).getPlayerName());
                AndroidNetworking.post(LINK + "/{path}")//send data of user location to server
                        .addPathParameter("path", "updateuser")
                        .addUrlEncodeFormBodyParameter("username", myUsername)
                        //.addUrlEncodeFormBodyParameter("username", gameUsers.get(gameUsers.keyAt(0)).getPlayerName())
                        .addUrlEncodeFormBodyParameter("lat", Double.toString(currlocation.getLatitude()))
                        .addUrlEncodeFormBodyParameter("long", Double.toString(currlocation.getLongitude()))
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("GameActivity updateUser", "onResponse");
                            }

                            @Override
                            public void onError(ANError anError) {
                                Log.d("GameActivity updateUser", "onError");
                            }
                        });
                ///////////////////////////////////////////////////////////////////////////////////
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //managelocation.requestLocationUpdates("GPS_PROVIDER", 500, .5f, locationListener);

        gogmymap = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.zombhummap);

        GetLocationPermissions();
        gogmymap.getMapAsync(this);

        if (ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //checks if we have th required permisions to use the location manager to get the lastknownposition.

        }
        // currlocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);

        pullDataThread.start();//initial start of the pullDataThread thread
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapRipple != null) {
            if (!mapRipple.isAnimationRunning()) {
                mapRipple.startRippleMapAnimation();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapRipple != null) {
            if (mapRipple.isAnimationRunning()) {
                mapRipple.stopRippleMapAnimation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        pullDataThread.norun();// stop the pulling data thread form running
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentById(android.R.id.content) instanceof ZombieConversionDialogFragment)//removes the dialog fragment from view
        {
            removedialog();

        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;

        myMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        myMap.getUiSettings().setMapToolbarEnabled(false);
        myMap.getUiSettings().setZoomControlsEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        myMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(myActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                }
                //Uncommented to make location updates work - Jimmy
                currlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (currlocation == null) {
                    Toast.makeText(myActivity, "Please wait a few minuetes for location to update", Toast.LENGTH_SHORT).show();
                } else {

                    LatLng tmp = new LatLng(currlocation.getLatitude(), currlocation.getLongitude());
                    CameraUpdate camup = CameraUpdateFactory.newLatLngZoom(tmp, 18.0f);
                    myMap.animateCamera(camup);
//                    myMap.moveCamera(camup);
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && mLocationRequest != null) {
                    // turnGPSOff();
                } else if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    turnGPSOn();
                }

                return true;// setting true allows nondefault behavior
            }
        });
        myMap.setMyLocationEnabled(true);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) { //check if the permision was confirmed. If it is confirmed it will try to get the permissions again, else it will stop trying.
        switch (requestCode) {
            case GPS_FINE_LOCATION_SERVICE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GetLocationPermissions();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    // permission was granted, yay! Do the

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void GetLocationPermissions()//function that tries to obtain location permissions.
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,//checks to see if it is a good idea to show the rationalle behind the requested permission
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "Need access to gps in order for application to work correctly!", Toast.LENGTH_LONG).show(); // tries to explain why we need the permission we are asking for

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        GPS_FINE_LOCATION_SERVICE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        GPS_FINE_LOCATION_SERVICE);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, .3f, locationListener);////////////////////////
            currlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        }
    }

    public void turnGPSOn() {//function to request gps permissions at runtime if they were not already enabled.
        createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates settingstates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(myActivity
                                    , REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    // automatic turn off the gps
    public void turnGPSOff() {
        mLocationRequest.setExpirationTime(-1);
    }

    protected void createLocationRequest() {// creates a location request to be put into a location builder and then to a pending result .. which then can be used to prompt the user to change gps settings
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void convert(String user,Boolean x) {
        if(x) {
            dialog.taggs.setClickable(false);
            AndroidNetworking.post(LINK + "/{path}")//send data of user location to server
                    .addPathParameter("path", "updateuser")
                    .addUrlEncodeFormBodyParameter("username", user)
                    .addUrlEncodeFormBodyParameter("iszombie", "true")
                    .build()
                    .getAsString(new StringRequestListener() {
                @Override
                public void onResponse(String response) {
                    Log.d("convertresponse", response);
                    removedialog();
                }

                @Override
                public void onError(ANError anError) {
                    dialog.taggs.setClickable(true);
                    Log.d("converterror", anError.toString());
                }
            });

        }
        else
        {
            removedialog();
        }
    }
    public void showdialog()
    {
        fragma=getSupportFragmentManager();
        fragtra=fragma.beginTransaction();
        fragtra.replace(android.R.id.content,dialog);
        fragtra.commit();
        fragma.executePendingTransactions();
    }
    public void removedialog()
    {
        dialog.taggs.setClickable(true);
        dialog.setUsername(null);
        fragma=getSupportFragmentManager();
        fragtra=fragma.beginTransaction();
        fragtra.remove(dialog);
        fragtra.commit();
        fragma.executePendingTransactions();
    }
}