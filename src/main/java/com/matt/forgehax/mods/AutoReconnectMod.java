package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

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
      if (event.getGui() instanceof DisconnectedScreen
          && !(event.getGui() instanceof GuiDisconnectedOverride)) {
        updateLastConnectedServer();
        DisconnectedScreen disconnected = (DisconnectedScreen) event.getGui();
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

  public static class GuiDisconnectedOverride extends DisconnectedScreen {
    private Screen parent;
    private ITextComponent message;

    // delay * 1000 = seconds to miliseconds
    private long reconnectTime;

    private Button reconnectButton = null;

    public GuiDisconnectedOverride(
        Screen screen,
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
        FastReflection.Fields.GuiDisconnected_reason.set(this, reason);
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
        MC.displayGuiScreen(new ConnectingScreen(parent, MC, data));
      }
    }

    @Override
    public void init() {
      super.init();
      List<String> multilineMessage =
          font.listFormattedStringToWidth(message.getFormattedText(), width - 50);
      int textHeight = multilineMessage.size() * font.FONT_HEIGHT;

      if (getLastConnectedServerData() != null) { // TODO: format better
        buttons.add(reconnectButton =
            new Button(
                width / 2 - 100,
                (height / 2 + textHeight / 2 + font.FONT_HEIGHT) + 23,
                20, 200,
                getFormattedReconnectText(),
                (button) -> reconnect())
        );
      }
    }


    @Override
    public void tick() {
      if (reconnectButton != null) reconnectButton.setMessage(getFormattedReconnectText());
      if (System.currentTimeMillis() >= reconnectTime) reconnect();
    }
  }
}
