package com.matt.forgehax.mods.net;

import com.matt.forgehax.ForgeHax;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created on 11/16/2016 by fr1kin
 */
public class Server {private final int port;
    private final IServerCallback callback;

    public Server(int port, IServerCallback callback) {
        this.port = port;
        this.callback = callback;
    }

    public void startServerThreaded() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startServer();
            }
        }).start();
    }

    public void startServer() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(reader);

                // task id
                switch (scanner.nextInt()) {
                    case IServerCallback.IGNORE:
                        break;
                    case IServerCallback.DISCONNECT:
                        callback.onConnecting();
                        break;
                    case IServerCallback.CONNECTED:
                        callback.onClientConnected();
                        break;
                    default:
                        break;
                }

                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getPort() {
        return port;
    }

    public static int findOpenPort(int startingPort, int maxPort) {
        for(int i = startingPort; i < maxPort + 1; i++) {
            Socket socket = null;
            try {
                socket = new Socket("localhost", i);

                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                writer.println(IServerCallback.IGNORE);
            } catch (Exception e) {
                return i;
            } finally {
                try {
                    if(socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public static int getTalkPort(int port) {
        return port % 2 == 0 ? port + 1 : port - 1;
    }
}
