package com.matt.forgehax.mods.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created on 11/16/2016 by fr1kin
 */
public class CallbackImpl implements ICallback {

    private Object callback;

    protected CallbackImpl() throws RemoteException {
        super();
    }


    @Override
    public void setCallback(Object cb) throws RemoteException {
        callback = cb;
    }

    @Override
    public void onConnecting() throws RemoteException {
        if(callback instanceof IRemoteCallback)
            ((IRemoteCallback) callback).onCalled();
        System.out.printf("\n\nCalled onConnecting\n\n");
    }
}
