package com.example.mangaramu.zombies_vs_humans;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mangaramu on 4/20/2017.
 */

public class SignuploginAct extends Activity {

    EditText playernmae;
    Button play;
    SharedPreferences sharedpref;
    SharedPreferences.Editor editor;
    String LINK = getResources().getString(R.string.URL);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());//for android networking!
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// set the app to always be in portrait mode .
        setContentView(R.layout.signuplog);

        sharedpref=getPreferences(Context.MODE_PRIVATE);
        editor = sharedpref.edit();


        if (sharedpref.getString("Name", "").equals("")) {
            playernmae = (EditText) findViewById(R.id.playername);
            play = (Button) findViewById(R.id.playbutt);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String name;

                    name = playernmae.getText().toString();
                    //send the name to the gameactivity if the name exists.
                    //if the name doesent exist, create an entry jSON and do a post request to some server api that will handle things.
                    AndroidNetworking.get(LINK + "/{path}")
                            .addPathParameter("path", getResources().getString(R.string.checkpath))
                            .addQueryParameter("username", name) //TODO - This is what i would want
                            .build()
                            .getAsJSONArray(new JSONArrayRequestListener() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.length() > 0)// if the name already exists on the server.
                                    {
                                        editor.putString("Name", name);
                                        editor.commit();
                                        StartGame(name);
                                    } else // send a name up to the server to create an account! Also server needs to send back down an empty JSON so on response we can save the name to the application
                                    {
                                        AndroidNetworking.post(LINK)
                                                .addUrlEncodeFormBodyParameter("username", name)
                                                .build()
                                                .getAsString(new StringRequestListener() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        editor.putString("Name", name);// saves the name to our editor object on empty JSON response
                                                        editor.commit();
                                                        StartGame(name);
                                                    }

                                                    @Override
                                                    public void onError(ANError anError) {

                                                    }
                                                });
                                        //post request that makes the user
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {

                                }
                            });
                }
            });
        } else {
            String shared;
            shared = sharedpref.getString("Name", "");
            StartGame(shared); //starts the game activity
        }
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

    public void StartGame(String name)// Takes in a string called name. Returns null. Will create an intent to start the GameActivity while also putting the inserted string as an extra to the application.
    {
        Intent start = new Intent(this, GameActivity.class);
        start.putExtra("Username", name);
        startActivity(start);
    }
}
