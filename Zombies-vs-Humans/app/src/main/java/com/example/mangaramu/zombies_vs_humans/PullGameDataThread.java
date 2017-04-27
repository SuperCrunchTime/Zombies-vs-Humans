package com.example.mangaramu.zombies_vs_humans;

import android.os.Handler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.example.mangaramu.zombies_vs_humans.Model.PlayerItem;

import org.json.JSONArray;
import org.json.JSONObject;


public class PullGameDataThread extends Thread {

    String myUsername;
    ArrayMap<String, PlayerItem> gameUsers;
    Boolean running = true;
    String LINK;
    Handler handle;
    String tmpuse;
    Double tmplong, tmplat;
    Boolean tmpstatus;

    PullGameDataThread(String mUsername, ArrayMap<String, PlayerItem> x, Handler y, String link) {
        myUsername = mUsername;
        gameUsers = x;
        handle = y;
        LINK = link;
    }

    @Override
    public void run() {
        AndroidNetworking.get(LINK + "/{path}")
                .addPathParameter("path", "getusers")
                //pull data of locations from server
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        JSONArray users = response;
                        for (int x = 0; x < users.length(); x++) {
                            try {
                                tmpuse = ((JSONObject) users.get(x)).getString("username");
                                tmplat = ((JSONObject) users.get(x)).getDouble("lat");
                                tmplong = ((JSONObject) users.get(x)).getDouble("long");
                                tmpstatus = ((JSONObject) users.get(x)).getBoolean("iszombie");

                                if (gameUsers.containsKey(tmpuse)) { //If the user is already within our array
                                    if (tmpuse.equals(myUsername)) {
                                        gameUsers.get(tmpuse).setIsZombie(tmpstatus);
                                    } else {
                                        gameUsers.get(tmpuse).setPlayerName(tmpuse);
                                        gameUsers.get(tmpuse).setLatitude(tmplat);
                                        gameUsers.get(tmpuse).setLongitude(tmplong);
                                        gameUsers.get(tmpuse).setIsZombie(tmpstatus);
                                    }
                                } else {// if we do not know of the player
                                    gameUsers.put(tmpuse, new PlayerItem(tmpuse, tmplat, tmplong, tmpstatus));
                                }
                            } catch (Exception e) {
                                Log.e("PullGameDataThread", e.toString());
                            }
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("PullGameDataThread", anError.toString());
                    }
                });

        android.os.SystemClock.sleep(300);// sleep 100 milisecconds

        Message m = Message.obtain();//send empty message prompting
        m.setTarget(handle);
        m.sendToTarget();

    }

    public void norun() {
        running = false;
    }
}
