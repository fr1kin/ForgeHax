package com.matt.forgehax.mods.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created on 11/16/2016 by fr1kin
 */
public class ClientToServer {
    private final int port;

    public ClientToServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void sendDisconnectMessage() {
        sendMessage(IServerCallback.DISCONNECT);
    }

    public void sendConnectedMessage() {
        sendMessage(IServerCallback.CONNECTED);
    }

    private void sendMessage(int message) {
        Socket socket = null;
        try {
            socket = new Socket("localhost", port);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(message);
        } catch (UnknownHostException e) {
        } catch (IOException e) {
        } finally {
            try {
                if(socket != null) socket.close();
            } catch (IOException e) {
            }
        }
    }
}
