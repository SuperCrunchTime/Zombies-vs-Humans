package com.example.mangaramu.zombies_vs_humans;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ZombieConversionDialogFragment extends Fragment {
    String username = null;
    TextView message;
    Button taggs;
    RelativeLayout grearea;

    public ZombieConversionDialogFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.zombieconversionmenu,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        message= (TextView) getActivity().findViewById(R.id.message);
        taggs= (Button) getActivity().findViewById(R.id.infect);
        grearea = (RelativeLayout) getActivity().findViewById(R.id.greyarea);
        if (username!=null)
        {
            message.setText(getActivity().getResources().getString(R.string.tag)+ " "+ username);
        }
        taggs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Converted)getActivity()).convert(username,true);
            }
        });
        grearea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Converted)getActivity()).convert(username,false);
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public interface Converted {
        public void convert(String user,@Nullable Boolean x);
    }

}
