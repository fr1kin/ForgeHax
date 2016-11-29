package com.matt.forgehax.mods.net;

/**
 * Created on 11/16/2016 by fr1kin
 */
public interface IServerCallback {
    int IGNORE = -1;
    int DISCONNECT = 0;
    int CONNECTED = 1;

    void onConnecting();

    void onClientConnected();
}
