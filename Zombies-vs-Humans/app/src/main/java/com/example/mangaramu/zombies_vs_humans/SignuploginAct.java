package com.example.mangaramu.zombies_vs_humans;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by mangaramu on 4/20/2017.
 */

public class SignuploginAct extends Activity {

    EditText playernmae;
    Button play;
    SharedPreferences sharedpref= getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedpref.edit();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuplog);
if(sharedpref.getString("Name","").equals("")) {
    playernmae = (EditText) findViewById(R.id.playername);
    play = (Button) findViewById(R.id.playbutt);
    play.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name;

            name = playernmae.getText().toString();
            editor.putString("Name",name);
            editor.commit();
//send the name to the gameactivity
        }
    });
}
else
{
    //send the name to the gameactivity if it is already there
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
}
