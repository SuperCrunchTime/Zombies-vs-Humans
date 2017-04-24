package com.example.mangaramu.zombies_vs_humans;


import android.Manifest;
        import android.app.Activity;
        import android.content.Context;
import android.content.IntentSender;
        import android.content.pm.ActivityInfo;
        import android.content.pm.PackageManager;
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
        import android.util.ArrayMap;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
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
import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.Marker;


import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GameActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{


        ArrayMap<String , Marker> Markers=new ArrayMap<>();
        SupportMapFragment gogmymap;
        GoogleMap mymap=null;
        LocationListener mylocation;
        LocationManager locationmanage;
        Location currlocation;
        LocationRequest mLocationRequest;
        LocationSettingsRequest.Builder builder;
        GoogleApiClient mGoogleApiClient;
        PendingResult<LocationSettingsResult> result;
        Activity myactivity;
        Boolean TrackLocationWithCamera,PinDrop=false;
        Boolean Firstchange=false;
        ArrayMap <String,PlayerItem> gameusers = new ArrayMap<>();
        Activity gamecontext = this;
        PullGamedatathread datagame;
        String LINK = getResources().getString(R.string.URL);

public static final int GPS_FINE_LOCATION_SERVICE=1;
public static final int REQUEST_CHECK_SETTINGS=1;

    Handler peopleupdate = new Handler(){//handle the players being updated here!
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        datagame = new PullGamedatathread(gameusers,peopleupdate);
            datagame.start();
    }};


@Override
protected void onCreate(Bundle savedInstanceState){// should only get called once because the requested orientation is portrait
    gameusers.put(getIntent().getStringExtra("Username"),new PlayerItem()); //sets the username to their playeritem, the first item in gameusers should be the player themselves
    datagame = new PullGamedatathread(gameusers,peopleupdate);

    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// set the app to always be in portrait mode .
        myactivity=this;
        AndroidNetworking.initialize(getApplicationContext());// For android networking!

        mGoogleApiClient=new GoogleApiClient.Builder(this)//setting up a google api client to change the gps settings
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        mGoogleApiClient.connect();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameact);

        locationmanage=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE); // allows you to do location related things if you have the correct permissions
        mylocation=new LocationListener(){
@Override
public void onLocationChanged(Location location){//on location changed takes in a location variable. It will do various tasks related to a google map by variable which are set by buttons.


        currlocation=location;
        LatLng tmp=new LatLng(currlocation.getLatitude(),currlocation.getLongitude());//updates where the camera on the map is relative to where you are.
        CameraUpdate camup2=CameraUpdateFactory.newLatLng(tmp);
        mymap.moveCamera(camup2);
        CameraUpdate camup=CameraUpdateFactory.zoomTo(19.0f);
        mymap.animateCamera(camup);

        AndroidNetworking.initialize(gamecontext);

    ////////////////////////////////////////////////////////////////////////
    JSONObject userupdate= new JSONObject();
    try {
        userupdate.put("Username",gameusers.get(0).getPlayername())
                .put("Latitude",currlocation.getLatitude())
                .put("Longitude",currlocation.getLongitude());

    } catch (JSONException e) {
        e.printStackTrace();
    }
    AndroidNetworking.post(LINK)//send data of user location to server
            .addJSONObjectBody(userupdate)
            .build()
            .getAsJSONObject(new JSONObjectRequestListener() {
                @Override
                public void onResponse(JSONObject response) {

                }

                @Override
                public void onError(ANError anError) {

                }
            });



    ///////////////////////////////////////////////////////////////////////////////////
        }

@Override
public void onStatusChanged(String provider,int status,Bundle extras){

        }

@Override
public void onProviderEnabled(String provider){

        }

@Override
public void onProviderDisabled(String provider){

        }
        };



        //managelocation.requestLocationUpdates("GPS_PROVIDER", 500, .5f, mylocation);

        gogmymap=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.zombhummap);


        GetLocationPermissions();
        gogmymap.getMapAsync(this);

        if(ActivityCompat.checkSelfPermission(myactivity,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(myactivity,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
        //checks if we have th required permisions to use the location manager to get the lastknownposition.

        }
        // currlocation = locationmanage.getLastKnownLocation(locationmanage.GPS_PROVIDER);


    datagame.start();//initial start of the datagame thread
        }

@Override
protected void onStart(){

        super.onStart();
        }

@Override
protected void onStop(){

        super.onStop();
        }

@Override
protected void onDestroy(){
    datagame.norun();// stop the pulling data thread form running
    super.onDestroy();
        }

@Override
public void onBackPressed(){
        super.onBackPressed();
        }

@Override
protected void onPause(){

        super.onPause();
        }

@Override
protected void onResume(){

        super.onResume();
        }

@Override
public void onMapReady(GoogleMap googleMap){
        mymap=googleMap;


        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.
        return;
        }
        mymap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
@Override
public boolean onMyLocationButtonClick(){
        if(ActivityCompat.checkSelfPermission(myactivity,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(myactivity,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

        }
        //currlocation = locationmanage.getLastKnownLocation(locationmanage.GPS_PROVIDER);
        if(currlocation==null){
        Toast.makeText(myactivity,"Please wait a few minuetes for location to update",Toast.LENGTH_SHORT).show();
        }else{

        LatLng tmp=new LatLng(currlocation.getLatitude(),currlocation.getLongitude());
        CameraUpdate camup2=CameraUpdateFactory.newLatLng(tmp);
        mymap.moveCamera(camup2);
        CameraUpdate camup=CameraUpdateFactory.zoomTo(19.0f);
        mymap.moveCamera(camup);
        }

        if(locationmanage.isProviderEnabled(LocationManager.GPS_PROVIDER)&&mLocationRequest!=null){
        // turnGPSOff();
        }else if(!locationmanage.isProviderEnabled(LocationManager.GPS_PROVIDER)){
        turnGPSOn();
        }


        return true;// setting true allows nondefault behavior
        }
        });

        mymap.setMyLocationEnabled(true);

        // mymap.addMarker()


        }

@Override
public void onRequestPermissionsResult(int requestCode,
        String permissions[],int[]grantResults){ //check if the permision was confirmed. If it is confirmed it will try to get the permissions again, else it will stop trying.
        switch(requestCode){
        case GPS_FINE_LOCATION_SERVICE:{
        // If request is cancelled, the result arrays are empty.
        if(grantResults.length>0
        &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
        GetLocationPermissions();
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
        return;
        }

        // permission was granted, yay! Do the

        }else{

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


        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
        // TODO: Consider calling
        //    ActivityCompat#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for ActivityCompat#requestPermissions for more details.


        // Should we show an explanation?
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,//checks to see if it is a good idea to show the rationalle behind the requested permission
        Manifest.permission.ACCESS_FINE_LOCATION)){

        Toast.makeText(this,"Need access to gps in order for application to work correctly!",Toast.LENGTH_LONG).show(); // tries to explain why we need the permission we are asking for

        ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        GPS_FINE_LOCATION_SERVICE);

        }
        else{

        // No explanation needed, we can request the permission.

        ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
        GPS_FINE_LOCATION_SERVICE);

        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        }
        }

        else{
        locationmanage.requestLocationUpdates(LocationManager.GPS_PROVIDER,100,.3f,mylocation);////////////////////////
        }

        }

public void turnGPSOn(){//function to request gps permissions at runtime if they were not already enabled.
        createLocationRequest();
        builder=new LocationSettingsRequest.Builder()
        .addLocationRequest(mLocationRequest);
        result=
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>(){
@Override
public void onResult(LocationSettingsResult result){
final Status status=result.getStatus();
final LocationSettingsStates settingstates=result.getLocationSettingsStates();
        switch(status.getStatusCode()){
        case LocationSettingsStatusCodes.SUCCESS:
        // All location settings are satisfied. The client can initialize location
        // requests here.

        break;
        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
        // Location settings are not satisfied. But could be fixed by showing the user
        // a dialog.
        try{
        // Show the dialog by calling startResolutionForResult(),
        // and check the result in onActivityResult().
        status.startResolutionForResult(myactivity
        ,REQUEST_CHECK_SETTINGS);
        }catch(IntentSender.SendIntentException e){
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
public void turnGPSOff(){
        mLocationRequest.setExpirationTime(-1);
        }

protected void createLocationRequest(){// creates a location request to be put into a location builder and then to a pending result .. which then can be used to prompt the user to change gps settings
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }


@Override
public void onConnectionFailed(@NonNull ConnectionResult connectionResult){

        }

@Override
public void onConnected(@Nullable Bundle bundle){

        }

@Override
public void onConnectionSuspended(int i){

        }

        }