package com.example.mangaramu.zombies_vs_humans.Model;

import android.location.Location;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mangaramu on 4/23/2017.
 */

public class PlayerItem {
    String Playername;
    Double latt;
    Double longi;
    String HuorZomb; // should be either nuull, Human, or Zombie

    public PlayerItem(@Nullable String playername, @Nullable Double latt, @Nullable Double longi, @Nullable String huorZomb) {
        Playername = playername;
        this.latt = latt;
        this.longi = longi;
        HuorZomb = huorZomb;
    }

    public PlayerItem() {

    }

    public String getHuorZomb() {
        return HuorZomb;
    }

    public void setHuorZomb(String huorZomb) {
        HuorZomb = huorZomb;
    }

    public String getPlayername() {
        return Playername;
    }

    public void setPlayername(String playername) {
        Playername = playername;
    }

    public Double getLattitude() {
        return this.latt;
    }

    public Double getLongitude() {
        return this.longi;
    }

    public void setLattitude(Double position) {
        this.latt = position;
    }

    public void setLongitude(Double position) {
        this.longi = position;
    }
}
