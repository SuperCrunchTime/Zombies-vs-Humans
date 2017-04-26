package com.example.mangaramu.zombies_vs_humans;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by mangaramu on 4/26/2017.
 */

public class ZombieConversionDialogFragment extends DialogFragment {
    String username="";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getResources().getString(R.string.tag)+ " "+ username)
                .setPositiveButton(R.string.tag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((Converted)getActivity()).convert(username);
                        username="";
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                username="";
                dismiss();

            }
        });

        return super.onCreateDialog(savedInstanceState);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public interface Converted
    {
        public void convert(String user);
    }

}
