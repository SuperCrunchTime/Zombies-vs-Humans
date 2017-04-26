package com.example.mangaramu.zombies_vs_humans;

import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.mangaramu.zombies_vs_humans.Model.PlayerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PullGamedatathread extends Thread {

    ArrayMap<String, PlayerItem> gameusers;
    Boolean running = true;
    String LINK;
    Handler handle;
    Double tmplong, tmplat;
    PullGamedatathread(ArrayMap<String, PlayerItem> x, Handler y, String link) {
        gameusers = x;
        handle = y;
        LINK = link;
    }

    @Override
    public void run() {
        AndroidNetworking.get(LINK + "/{path}")
                .addPathParameter("path","getusers")
                //pull data of locations from server
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                            JSONArray users = response;
                            for (int x = 0; x < users.length(); x++) {
                                try {
                                    String tmpuse = ((JSONObject) users.get(x)).getString("username");
                                    tmplat = ((JSONObject) users.get(x)).getDouble("lat");
                                    tmplong = ((JSONObject) users.get(x)).getDouble("long");
                                    Boolean tmpstatus = ((JSONObject) users.get(x)).getBoolean("iszombie");

                                if (gameusers.containsKey(tmpuse)) { //If the user is already within our array
                                    if (tmpuse.equals(gameusers.keyAt(0))) {
                                        gameusers.get(tmpuse).setIsZombie(tmpstatus);
                                    } else {
                                        if(tmplong!=null && tmplat!=null) {
                                            gameusers.get(tmpuse).setLattitude(tmplat);
                                            gameusers.get(tmpuse).setLongitude(tmplong);
                                        }
                                        gameusers.get(tmpuse).setIsZombie(tmpstatus);
                                    }
                                } else {// if we do not know of the player
                                    gameusers.put(tmpuse, new PlayerItem(tmpuse, tmplat, tmplong, tmpstatus));

                                }
                                } catch (Exception e){

                                }
                            }

                    }

                    @Override
                    public void onError(ANError anError) {

                    }
                });

        android.os.SystemClock.sleep(5000);// sleep 5000 milisecconds

        Message m = Message.obtain();//send empty message prompting
        m.setTarget(handle);
        m.sendToTarget();

    }

    public void norun() {
        running = false;
    }
}
