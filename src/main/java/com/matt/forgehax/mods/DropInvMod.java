package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.net.ClientToServer;
import com.matt.forgehax.mods.net.IServerCallback;
import com.matt.forgehax.mods.net.Server;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/**
 * Created on 11/15/2016 by fr1kin
 */
public class DropInvMod extends ToggleMod implements IServerCallback {
    private static final int STARTING_PORT = 4044;

    // this mods server instance
    private Server server;

    // the server this mod will be talking to
    private ClientToServer clientToServer;

    public Property talkToClients;
    public Property dropDelay;

    public Property waitDelay;

    public Property sendOrder;

    private long timeConnected = -1;

    private static final String[] ORDERS = {"PRE", "POST"};

    public DropInvMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
        initializeServer();
    }

    @Override
    public void onEnabled() {
        timeConnected = -1;
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                talkToClients = configuration.get(getModName(),
                        "talk_to_clients",
                        false,
                        "Talk to other clients"
                ),
                dropDelay = configuration.get(getModName(),
                        "drop_delay",
                        100,
                        "Delay to drop items"
                ),
                waitDelay = configuration.get(getModName(),
                        "wait_delay",
                        10000,
                        "Delay to wait to autodupe"
                ),
                sendOrder = configuration.get(getModName(),
                        "send_order",
                        ORDERS[0],
                        "When to send the kick packet",
                        ORDERS
                )
        );
    }

    public void initializeServer() {
        int port = Server.findOpenPort(STARTING_PORT, STARTING_PORT + 10);
        if (port == -1) {
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

    private void dupeItems() {
        switch (sendOrder.getString()) {
            case "PRE":
            {
                getNetworkManager().sendPacket(new CPacketUseEntity(MC.thePlayer, EnumHand.MAIN_HAND));
                for(int i = 9; i < 45; i++) {
                    MC.playerController.windowClick(0, i, 1, ClickType.THROW,
                            MC.thePlayer);
                }
                break;
            }
            default:
            case "POST":
            {
                for(int i = 9; i < 45; i++) {
                    MC.playerController.windowClick(0, i, 1, ClickType.THROW,
                            MC.thePlayer);
                }
                getNetworkManager().sendPacket(new CPacketUseEntity(MC.thePlayer, EnumHand.MAIN_HAND));
            }
        }
    }

    private void dropInv() {
        if(dropDelay.getInt() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(dropDelay.getLong());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    dupeItems();
                }
            }).start();
        } else {
            dupeItems();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        clientToServer.sendConnectedMessage();
        timeConnected = System.currentTimeMillis() + waitDelay.getInt();
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketEncryptionResponse) {
            clientToServer.sendDisconnectMessage();
            MOD.getLog().info("Sent disconnect msg");
        }
    }

    @SubscribeEvent
    public void onTick(LocalPlayerUpdateEvent event) {
        if(timeConnected != -1 &&
                System.currentTimeMillis() > timeConnected) {
            dropInv();
            timeConnected = -1;
        }
    }

    @SubscribeEvent
    public void onKeyPressed(InputEvent.KeyInputEvent event) {
        if(MC.gameSettings.keyBindDrop.isPressed()) {
            dropInv();
        }
    }

    @Override
    public void onConnecting() {
        if(talkToClients.getBoolean()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dropInv();
                }
            }).start();
        }
    }

    @Override
    public void onClientConnected() {
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(dropDelay.getLong());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(MC.currentScreen != null)
                    connectToServer = true;
            }
        }).start();*/
    }
}
