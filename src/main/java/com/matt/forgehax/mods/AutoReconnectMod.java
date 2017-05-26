package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.Minecraft;
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

@RegisterMod
public class AutoReconnectMod extends ToggleMod {
    private static ServerData lastConnectedServer;

    public void updateLastConnectedServer() {
        ServerData data = MC.getCurrentServerData();
        if(data != null)
            lastConnectedServer = data;
    }

    public Property delayTime;
    public Property delayConnectTime;

    public AutoReconnectMod() {
        super("AutoReconnect", false, "Automatically reconnects to server");
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
                delayConnectTime = configuration.get(getModCategory().getName(),
                        "delay_connect",
                        5,
                        "Login delay"
                )
        );
    }

    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiDisconnected &&
                !(event.getGui() instanceof GuiDisconnectedOverride)) {
            updateLastConnectedServer();
            GuiDisconnected disconnected = (GuiDisconnected)event.getGui();
            event.setGui(new GuiDisconnectedOverride(
                    FastReflection.Fields.GuiDisconnected_parentScreen.get(disconnected),
                    "connect.failed",
                    FastReflection.Fields.GuiDisconnected_message.get(disconnected),
                    FastReflection.Fields.GuiDisconnected_reason.get(disconnected),
                    delayTime.getDouble()
            ));
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        // we got on the server or stopped joining, now undo queue
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
            try {
                ReflectionHelper.setPrivateValue(GuiDisconnected.class, this, reason, "reason", "field_146306_a", "a"); // TODO: Find obbed mapping name
            } catch (Exception e) {
                MOD.getLog().error(e.getMessage());
            }
            // parse server return text and find queue pos
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

        private void reconnect() {
            ServerData data = getLastConnectedServerData();
            if(data != null) {
                FMLClientHandler.instance().showGuiScreen(new GuiConnecting(parent, MC, data));
            }
        }

        @Override
        public void initGui() {
            super.initGui();
            List<String> multilineMessage = fontRendererObj.listFormattedStringToWidth(message.getFormattedText(), width - 50);
            int textHeight = multilineMessage.size() * fontRendererObj.FONT_HEIGHT;

            if(getLastConnectedServerData() != null) {
                buttonList.add(reconnectButton = new GuiButton(buttonList.size(),
                        width / 2 - 100,
                        (height / 2 + textHeight / 2 + fontRendererObj.FONT_HEIGHT) + 23,
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
    }
}
