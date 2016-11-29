package com.matt.forgehax.mods.rmi;

import com.matt.forgehax.ForgeHax;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created on 11/16/2016 by fr1kin
 */
public class ClientServer {
    private final Registry registry;
    private String address;

    private String talkAddress;

    private RemoteClientServer remoteClientServer;

    private ICallback callback;

    public ClientServer(Registry registry, String address) {
        this.registry = registry;
        this.address = address;
    }

    public Registry getRegistry() {
        return registry;
    }

    public String getTalkAddress() {
        return talkAddress;
    }

    public String getAddress() {
        return address;
    }

    public RemoteClientServer getRemoteClient() {
        return remoteClientServer;
    }

    public ICallback getCallback() {
        return callback;
    }

    public void startServer() {
        int index = 0;
        for(; index < 2; index++) {
            try {
                registry.lookup(address + String.valueOf(index));
            } catch (NotBoundException e) {
                try {
                    UnicastRemoteObject.exportObject(callback = new CallbackImpl(), 4044);
                    registry.rebind(address + String.valueOf(index), callback);
                    address += String.valueOf(index);
                    talkAddress = address + (index == 0 ? "1" : "0");
                    ForgeHax.instance().getLog().info(String.format("Created new registry '%s'", address));
                    remoteClientServer = new RemoteClientServer(registry, address);
                    return;
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            } catch (AccessException e) {
            } catch (RemoteException e) {
            }
        }
    }
}
