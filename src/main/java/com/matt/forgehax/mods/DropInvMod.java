package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.mods.net.ClientToServer;
import com.matt.forgehax.mods.net.IServerCallback;
import com.matt.forgehax.mods.net.Server;
import com.matt.forgehax.mods.rmi.ICallback;
import com.matt.forgehax.mods.rmi.IRemoteCallback;
import com.matt.forgehax.mods.rmi.ClientServer;
import com.matt.forgehax.mods.rmi.RemoteClientServer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.login.client.CPacketLoginStart;
import net.minecraft.network.login.server.SPacketEnableCompression;
import net.minecraft.network.login.server.SPacketLoginSuccess;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created on 11/15/2016 by fr1kin
 */
public class DropInvMod extends ToggleMod implements IServerCallback {
    private static final int STARTING_PORT = 4044;

    // this mods server instance
    private Server server;

    // the server this mod will be talking to
    private ClientToServer clientToServer;

    private boolean isConnectedToServer = false;

    public Property time;
    public Property autoCalibrator;
    public Property calibrationAmount;

    public long calibrationValue = 100;

    private ItemStack droppedItem = null;

    public DropInvMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
        initializeServer();
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                time = configuration.get(getModName(),
                        "time",
                        50,
                        "Thread sleep in ms"
                ),
                autoCalibrator = configuration.get(getModName(),
                        "auto_calibrate",
                        true,
                        "Automatically calibrate"
                ),
                calibrationAmount = configuration.get(getModName(),
                        "calibration_amount",
                        10,
                        "Calibration amount"
                )
        );
    }

    public void initializeServer() {
        int port = Server.findOpenPort(STARTING_PORT, STARTING_PORT + 10);
        if(port == -1) {
            MOD.getLog().warn("Failed to find open port");
            return;
        }
        MOD.getLog().info(String.format("Found open port at '%d'", port));

        // start server
        server = new Server(port, this);
        server.startServerThreaded();

        int talkingPort = Server.getTalkPort(port);
        MOD.getLog().info(String.format("Talking to port '%d'", talkingPort));

        clientToServer = new ClientToServer(talkingPort);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketEncryptionResponse) {
            clientToServer.sendDisconnectMessage();
        }
    }

    @SubscribeEvent
    public void onPacketRecieve(PacketEvent.Received.Pre event) {
         if(event.getPacket() instanceof SPacketDisconnect) {
             if(droppedItem != null &&
                     MC.theWorld != null) {
                 MC.theWorld.loadedEntityList.contains(droppedItem);
             }
             droppedItem = null;
        } else if(event.getPacket() instanceof SPacketSetSlot) {
             ItemStack stack = ((SPacketSetSlot) event.getPacket()).getStack();
             if(stack != null &&
                     droppedItem != null &&
                     stack.equals(droppedItem)) {

             }
         }
    }

    @Override
    public void onConnecting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time.getLong());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(MC.thePlayer != null) {
                    droppedItem = MC.thePlayer.getHeldItemMainhand();
                    MC.thePlayer.dropItem(true);
                } else {

                }
            }
        }).start();
        MOD.getLog().info("onConnecting called");
    }
}
