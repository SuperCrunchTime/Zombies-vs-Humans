package com.example.mangaramu.zombies_vs_humans.Model;

import android.support.annotation.Nullable;

/**
 * Created by mangaramu on 4/23/2017.
 */

public class PlayerItem {
    String Playername;
    Double latt;
    Double longi;
    Boolean isZombie;

    public PlayerItem(@Nullable String playername, @Nullable Double latt, @Nullable Double longi, @Nullable Boolean isZombie) {
        Playername = playername;
        this.latt = latt;
        this.longi = longi;
        this.isZombie = isZombie;
    }

    public PlayerItem() {

    }

    public Boolean isZombie() {
        return isZombie;
    }

    public void setIsZombie(Boolean iszombie) {
        isZombie = iszombie;
    }

    public String getPlayername() {
        return Playername;
    }

    public void setPlayername(String playername) {
        Playername = playername;
    }

    public Double getLatitude() {
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
