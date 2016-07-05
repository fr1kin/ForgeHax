package com.matt.forgehax.mods;

import com.matt.forgehax.util.ServerQueueManager;
import com.matt.forgehax.util.Utils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AutoReconnectMod extends ToggleMod {
    private static ServerData lastConnectedServer;

    private static ServerQueueManager queueManager;

    public void updateLastConnectedServer() {
        ServerData data = MC.getCurrentServerData();
        if(data != null)
            lastConnectedServer = data;
    }

    public void updateQueueManager() {
        ServerData data = MC.getCurrentServerData();
        if(queueManager == null || !queueManager.equals(data))
            queueManager = new ServerQueueManager(data);
    }

    public Property delayTime;
    public Property mode2b2t;

    public AutoReconnectMod(String modName, boolean defaultValue, String description) {
        super(modName, defaultValue, description, -1);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        super.loadConfig(configuration);
        addSettings(
                delayTime = configuration.get(getModCategory().getName(),
                        "delay",
                        5,
                        "Delay between each connect"
                ),
                mode2b2t = configuration.get(getModCategory().getName(),
                        "2b2t mode",
                        true,
                        "Shows ETA wait time in 2b2t queue, and other things"
                )
        );
    }

    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiDisconnected) {
            updateLastConnectedServer();
            updateQueueManager();
            GuiDisconnected disconnected = (GuiDisconnected)event.getGui();
            event.setGui(new GuiDisconnectedOverride(
                    disconnected.parentScreen,
                    "connect.failed",
                    disconnected.message,
                    disconnected.reason,
                    delayTime.getDouble()
            ));
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        // we got on the server or stopped joining, now undo queue
        queueManager = null;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        updateLastConnectedServer();
    }

    public static class GuiDisconnectedOverride extends GuiDisconnected {
        private GuiScreen parent;
        private ITextComponent message;

        // delay * 1000 = seconds to miliseconds
        private long reconnectTime;

        private GuiButton reconnectButton = null;

        public GuiDisconnectedOverride(GuiScreen screen, String reasonLocalizationKey, ITextComponent chatComp, String reason, double delay) {
            super(screen, reasonLocalizationKey, chatComp);
            parent = screen;
            message = chatComp;
            reconnectTime = System.currentTimeMillis() + (long)(delay * 1000);
            // set variable 'reason' to the previous classes value
            ReflectionHelper.setPrivateValue(GuiDisconnected.class, this, reason, "reason"); // TODO: Find obbed mapping name
            // parse server return text and find queue pos
            handleQueue();
        }

        public long getTimeUntilReconnect() {
            return reconnectTime - System.currentTimeMillis();
        }

        public double getTimeUntilReconnectInSeconds() {
            return (double)getTimeUntilReconnect() / 1000.D;
        }

        public String getFormattedReconnectText() {
            return String.format("Reconnecting (%.1f)...", getTimeUntilReconnectInSeconds());
        }

        public ServerData getLastConnectedServerData() {
            return lastConnectedServer != null ? lastConnectedServer : MC.getCurrentServerData();
        }

        public void handleQueue() {
            Matcher matcher = Pattern.compile("POSITION\\s(\\d+)\\sOUT\\sOF\\s(\\d+)").matcher(message.getFormattedText());
            if(matcher.find()) {
                if(queueManager != null) {
                    queueManager.setPos(Integer.parseInt(matcher.group(1)));
                }
            }
        }

        private void reconnect() {
            ServerData data = getLastConnectedServerData();
            if(data != null) {
                FMLClientHandler.instance().showGuiScreen(new GuiConnecting(parent, MC, data));
            }
        }

        @Override
        public void initGui() {
            super.initGui();
            List<String> multilineMessage = this.fontRendererObj.listFormattedStringToWidth(this.message.getFormattedText(), this.width - 50);
            int textHeight = multilineMessage.size() * this.fontRendererObj.FONT_HEIGHT;

            if(getLastConnectedServerData() != null) {
                this.buttonList.add(reconnectButton = new GuiButton(buttonList.size(),
                        this.width / 2 - 100,
                        (this.height / 2 + textHeight / 2 + this.fontRendererObj.FONT_HEIGHT) + 23,
                        getFormattedReconnectText()
                ));
            }
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException {
            super.actionPerformed(button);
            if(button.equals(reconnectButton)) {
                reconnect();
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            if(reconnectButton != null)
                reconnectButton.displayString = getFormattedReconnectText();
            if(System.currentTimeMillis() >= reconnectTime)
                reconnect();
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            if(queueManager != null) {
                // TODO: fix
                double seconds = queueManager.getEstimatedTime() / 1000.D;
                double minutes = seconds / 60.D;
                double hours = seconds / 60.D;
                MC.fontRendererObj.drawStringWithShadow(
                        String.format("Pos: %d [ETA: %.2f hours, %.2f minutes, %.2f seconds]",
                                queueManager.getPos(),
                                hours, minutes, seconds
                        ), 10, 10, Utils.toRGBA(255, 255, 255, 255));
            }
        }
    }
}
