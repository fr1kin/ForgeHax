package com.matt.forgehax.mods.rmi;

import com.matt.forgehax.mods.DropInvMod;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created on 11/16/2016 by fr1kin
 */
public interface ICallback extends Remote, Serializable {
    void setCallback(Object cb) throws RemoteException;
    void onConnecting() throws RemoteException;
}
