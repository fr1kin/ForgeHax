package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.mods.net.ClientToServer;
import com.matt.forgehax.mods.net.IServerCallback;
import com.matt.forgehax.mods.net.Server;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiShulkerBox;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.login.client.CPacketEncryptionResponse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public Property sendKickPacket;

    public Property autoDuper;

    private long timeConnected = -1;

    private boolean isThreadActive = false;

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
                ),
                sendKickPacket = configuration.get(getModName(),
                        "force_kick",
                        true,
                        "Force kick yourself"
                ),
                autoDuper = configuration.get(getModName(),
                        "auto_dupe",
                        false,
                        "Auto duper"
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

    private void handleMouseClick(int slotId, int mouseButton, ClickType type) {
        if(MC.currentScreen != null &&
                MC.currentScreen instanceof GuiContainer) {
            MC.playerController.windowClick(
                    ((GuiContainer) MC.currentScreen).inventorySlots.windowId,
                    slotId,
                    mouseButton,
                    type,
                    MC.player
            );
        }
    }

    private void quickMoveSelectedToChest() {
        if(MC.currentScreen instanceof GuiChest ||
                MC.currentScreen instanceof GuiShulkerBox) {
            GuiContainer guiChest = (GuiContainer)MC.currentScreen;
            // find first player inv slot
            int slotStartPlayerInv = -1;
            for(Slot slot : guiChest.inventorySlots.inventorySlots) {
                if(slot.inventory instanceof InventoryPlayer) {
                    slotStartPlayerInv = slot.getSlotIndex();
                    break;
                }
            }
            if(slotStartPlayerInv == -1) {
                MOD.getLog().error("Failed to find player inventory slot starting index");
                return;
            }
            for(int i = slotStartPlayerInv; i < guiChest.inventorySlots.inventorySlots.size(); i++) {
                if(guiChest.inventorySlots.getSlot(i).getHasStack())
                    handleMouseClick(i, 0, ClickType.QUICK_MOVE);
            }
        }
    }

    private void dropAllInventory() {
        if (MC.player != null &&
                MC.playerController != null) {
            for (int i = 9; i < 45; i++) {
                if (!MC.player.inventory.getStackInSlot(i).equals(ItemStack.EMPTY)) {
                    MC.playerController.windowClick(0, i, 1, ClickType.THROW,
                            MC.player);
                }
            }
        }
    }

    private void pauseThread() {
        if(dropDelay.getInt() > 0) {
            try {
                Thread.sleep(dropDelay.getInt());
            } catch (InterruptedException e) {
            }
        }
    }

    private void dupeItems() {
        switch (sendOrder.getString()) {
            case "POST":
            {
                if(sendKickPacket.getBoolean()) getNetworkManager().sendPacket(new CPacketUseEntity(MC.player, EnumHand.MAIN_HAND));
                pauseThread();
                quickMoveSelectedToChest();
                break;
            }
            default:
            case "PRE":
            {
                quickMoveSelectedToChest();
                pauseThread();
                if(sendKickPacket.getBoolean()) getNetworkManager().sendPacket(new CPacketUseEntity(MC.player, EnumHand.MAIN_HAND));
            }
        }
        isThreadActive = false;
    }

    private void createInvDropThread() {
        if(isThreadActive || MC.player == null || MC.world == null) return;
        isThreadActive = true;
        MOD.getLog().info(String.format("Inv drop thread created (%d ms thread pause)", dropDelay.getInt()));
        if(dropDelay.getInt() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
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
        if(autoDuper.getBoolean()) {
            timeConnected = System.currentTimeMillis() + waitDelay.getInt();
        } else {
            timeConnected = -1;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send.Pre event) {
        if(event.getPacket() instanceof CPacketEncryptionResponse) {
            clientToServer.sendDisconnectMessage();
        }
    }

    @SubscribeEvent
    public void onTick(LocalPlayerUpdateEvent event) {
        if(timeConnected != -1 &&
                System.currentTimeMillis() > timeConnected) {
            createInvDropThread();
            timeConnected = -1;
        }
    }

    @SubscribeEvent
    public void onGuiKeyPressed(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if(Keyboard.getEventKey() == Keyboard.KEY_G &&
                Keyboard.isKeyDown(Keyboard.KEY_G)) {
            createInvDropThread();
        }
    }

    @Override
    public void onConnecting() {
        if(talkToClients.getBoolean()) {
            createInvDropThread();
        }
    }

    @Override
    public void onClientConnected() {}
}
