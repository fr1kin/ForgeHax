package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@RegisterMod
public class AutoReconnectMod extends ToggleMod {
  private static ServerData lastConnectedServer;

  public static boolean hasAutoLogged =
      false; // used to disable autoreconnecting without disabling the entire mod

  public void updateLastConnectedServer() {
    ServerData data = MC.getCurrentServerData();
    if (data != null) lastConnectedServer = data;
  }

  public final Setting<Double> delay =
      getCommandStub()
          .builders()
          .<Double>newSettingBuilder()
          .name("delay")
          .description("Delay between each reconnect attempt")
          .defaultTo(5.D)
          .build();

  public AutoReconnectMod() {
    super(Category.MISC, "AutoReconnect", false, "Automatically reconnects to server");
  }

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    if (!hasAutoLogged)
      if (event.getGui() instanceof GuiDisconnected
          && !(event.getGui() instanceof GuiDisconnectedOverride)) {
        updateLastConnectedServer();
        GuiDisconnected disconnected = (GuiDisconnected) event.getGui();
        event.setGui(
            new GuiDisconnectedOverride(
                FastReflection.Fields.GuiDisconnected_parentScreen.get(disconnected),
                "connect.failed",
                FastReflection.Fields.GuiDisconnected_message.get(disconnected),
                FastReflection.Fields.GuiDisconnected_reason.get(disconnected),
                delay.get()));
      }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    // we got on the server or stopped joining, now undo queue
    hasAutoLogged = false; // make mod work when you rejoin
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

    public GuiDisconnectedOverride(
        GuiScreen screen,
        String reasonLocalizationKey,
        ITextComponent chatComp,
        String reason,
        double delay) {
      super(screen, reasonLocalizationKey, chatComp);
      parent = screen;
      message = chatComp;
      reconnectTime = System.currentTimeMillis() + (long) (delay * 1000);
      // set variable 'reason' to the previous classes value
      try {
        ReflectionHelper.setPrivateValue(
            GuiDisconnected.class,
            this,
            reason,
            "reason",
            "field_146306_a",
            "a"); // TODO: Find obbed mapping name
      } catch (Exception e) {
        Helper.printStackTrace(e);
      }
      // parse server return text and find queue pos
    }

    public long getTimeUntilReconnect() {
      return reconnectTime - System.currentTimeMillis();
    }

    public double getTimeUntilReconnectInSeconds() {
      return (double) getTimeUntilReconnect() / 1000.D;
    }

    public String getFormattedReconnectText() {
      return String.format("Reconnecting (%.1f)...", getTimeUntilReconnectInSeconds());
    }

    public ServerData getLastConnectedServerData() {
      return lastConnectedServer != null ? lastConnectedServer : MC.getCurrentServerData();
    }

    private void reconnect() {
      ServerData data = getLastConnectedServerData();
      if (data != null) {
        FMLClientHandler.instance().showGuiScreen(new GuiConnecting(parent, MC, data));
      }
    }

    @Override
    public void initGui() {
      super.initGui();
      List<String> multilineMessage =
          fontRenderer.listFormattedStringToWidth(message.getFormattedText(), width - 50);
      int textHeight = multilineMessage.size() * fontRenderer.FONT_HEIGHT;

      if (getLastConnectedServerData() != null) {
        buttonList.add(
            reconnectButton =
                new GuiButton(
                    buttonList.size(),
                    width / 2 - 100,
                    (height / 2 + textHeight / 2 + fontRenderer.FONT_HEIGHT) + 23,
                    getFormattedReconnectText()));
      }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
      super.actionPerformed(button);
      if (button.equals(reconnectButton)) {
        reconnect();
      }
    }

    @Override
    public void updateScreen() {
      super.updateScreen();
      if (reconnectButton != null) reconnectButton.displayString = getFormattedReconnectText();
      if (System.currentTimeMillis() >= reconnectTime) reconnect();
    }
  }
}
