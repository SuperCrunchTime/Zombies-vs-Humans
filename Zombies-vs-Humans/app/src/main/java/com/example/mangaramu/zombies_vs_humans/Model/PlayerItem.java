package com.example.mangaramu.zombies_vs_humans.Model;

import android.support.annotation.Nullable;

public class PlayerItem {
    String playerName;
    Double latt;
    Double longi;
    Boolean isZombie;

    public PlayerItem(@Nullable String playername, @Nullable Double latt, @Nullable Double longi, @Nullable Boolean isZombie) {
        playerName = playername;
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

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playername) {
        playerName = playername;
    }

    public Double getLatitude() {
        return this.latt;
    }

    public Double getLongitude() {
        return this.longi;
    }

    public void setLatitude(Double position) {
        this.latt = position;
    }

    public void setLongitude(Double position) {
        this.longi = position;
    }
}
