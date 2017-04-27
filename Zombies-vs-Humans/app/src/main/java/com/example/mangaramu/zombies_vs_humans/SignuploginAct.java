package com.example.mangaramu.zombies_vs_humans;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignUpLoginAct extends Activity {

    EditText playerName;
    TextView graveText;
    Button play;
//    SharedPreferences sharedPref;
//    SharedPreferences.Editor editor;
    String LINK;
    MediaPlayer cackleSound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());//for android networking!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// set the app to always be in portrait mode .
        setContentView(R.layout.signuplog);

//        sharedPref = getPreferences(Context.MODE_PRIVATE);
//        editor = sharedPref.edit();
        LINK = getResources().getString(R.string.URL);
        //sounds
        cackleSound = MediaPlayer.create(this, R.raw.cackle3);

        /*if (sharedPref.getString("Name", "").equals("")) {*/

        playerName = (EditText) findViewById(R.id.playername);
        play = (Button) findViewById(R.id.playbutt);//Tee hee, you said butt
        graveText = (TextView) findViewById(R.id.graveText);


        //Finds the dimensions of the devices windows and stores them in height and width variables
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;

        //For animating the grave writing
        playerName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (screenWidth > 1023 && screenHeight > 1023) { //Screen is probably a tablet
                    graveText.setText("R.I.P \n \n \n " + String.valueOf(s));
                } else { //Screen is probably a phone
                    graveText.setText("R.I.P \n " + String.valueOf(s));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Click listener for the play button
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name;
                name = playerName.getText().toString();
                cackleSound.start();

                Log.d("Name", name);

                //send the name to the gameactivity if the name exists.
                //if the name doesent exist, create an entry jSON and do a post request to some server api that will handle things.
                AndroidNetworking.get(LINK + "/{path}")
                        .addPathParameter("path", getResources().getString(R.string.checkpath))
                        .addQueryParameter("username", name) //TODO - This is what i would want
                        .build()
                        .getAsJSONArray(new JSONArrayRequestListener() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("On response", "JSONArray");
                                if (response.length() > 0)// if the name already exists on the server.
                                {
//                                    editor.putString("Name", name);
//                                    editor.commit();
                                    // Count the number of zombies and humans
//                                    int zombieCount = 0, humanCount = 0;
//                                    for (int i = 0; i < response.length(); i++) {
//                                        try {
//                                            if (((JSONObject) response.get(i)).getBoolean("iszombie")) {
//                                                zombieCount++;
//                                            } else {
//                                                humanCount++;
//                                            }
//                                        } catch (Exception e) {
//                                            Log.e("SignUpLoginAct", "Counting # of zombies and humans: " + e.toString());
//                                        }
//                                    }
//                                    if (zombieCount >= 1) {
//                                        StartGame(name, 0.0, 0.0, false);
//                                    } else {
//                                        StartGame(name, 0.0, 0.0, true);
//                                    }
//                                    Log.d("Server", "Name Already Exists");
                                    StartGame(name, 0.0, 0.0, false);
                                } else // send a name up to the server to create an account! Also server needs to send back down an empty JSON so on response we can save the name to the application
                                {
                                    Log.d("Client", "Send Name to Server");
                                    AndroidNetworking.post(LINK + "/{path}")
                                            .addPathParameter("path", "updateuser")
                                            .addUrlEncodeFormBodyParameter("username", name)
                                            .addUrlEncodeFormBodyParameter("lat", "0")
                                            .addUrlEncodeFormBodyParameter("long", "0")
                                            .addUrlEncodeFormBodyParameter("iszombie", "false")
                                            .build()
                                            .getAsString(new StringRequestListener() {
                                                @Override
                                                public void onResponse(String response) {
                                                    Log.d("On response", "StringRequestListener");
//                                                    editor.putString("Name", name);// saves the name to our editor object on empty JSON response
//                                                    editor.commit();
                                                    StartGame(name, 0.0, 0.0, false);
                                                }

                                                @Override
                                                public void onError(ANError anError) {
                                                    Log.e("onError", "StringRequestListener", anError);
                                                }
                                            });
                                    //post request that makes the user
                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                Log.d("onError", "JSONArray");
                            }
                        });
            }
        });
//        } else {
//            String shared;
//            shared = sharedPref.getString("Name", "");
//            StartGame(shared, 0.0, 0.0, false); //starts the game activity
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void StartGame(String name, Double lat, Double lng, Boolean isZombie)// Takes in a string called name. Returns null. Will create an intent to start the GameActivity while also putting the inserted string as an extra to the application.
    {

        Intent start = new Intent(this, GameActivity.class);
        start.putExtra("Username", name);
        start.putExtra("Latitude", lat);
        start.putExtra("Longitude", lng);
        start.putExtra("isZombie", isZombie);
        startActivity(start);
    }
}
