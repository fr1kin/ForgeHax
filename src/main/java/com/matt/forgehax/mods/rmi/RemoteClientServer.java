package com.matt.forgehax.mods.rmi;

import javax.rmi.PortableRemoteObject;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

/**
 * Created on 11/16/2016 by fr1kin
 */
public class RemoteClientServer {
    private final Registry registry;
    private final String address;

    private ICallback callback;

    public RemoteClientServer(Registry registry, String address) {
        this.registry = registry;
        this.address = address;
    }

    private ICallback attemptLookup() {
        if(callback == null) {
            Object o = null;
            try {
                o = (Object)registry.lookup(address);
                callback = (ICallback)PortableRemoteObject.narrow(o, ICallback.class);
            } catch (RemoteException e) {
            } catch (NotBoundException e) {
            }
        }
        return callback;
    }

    public String getAddress() {
        return address;
    }

    public Registry getRegistry() {
        return registry;
    }

    public ICallback getCallback() {
        return attemptLookup();
    }

    public void onConnect() {
        if(attemptLookup() != null) {
            try {
                callback.onConnecting();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
