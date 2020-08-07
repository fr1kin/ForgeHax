package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.ClientWorldEvent;
import dev.fiki.forgehax.main.util.cmd.settings.DoubleSetting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AutoReconnect",
    description = "Automatically reconnects to server",
    category = Category.MISC
)
public class AutoReconnectMod extends ToggleMod {

  private static ServerData lastConnectedServer;

  public static boolean hasAutoLogged =
      false; // used to disable autoreconnecting without disabling the entire mod

  public void updateLastConnectedServer() {
    ServerData data = Common.MC.getCurrentServerData();
    if (data != null) {
      lastConnectedServer = data;
    }
  }

  public final DoubleSetting delay = newDoubleSetting()
      .name("delay")
      .description("Delay between each reconnect attempt")
      .defaultTo(5.D)
      .build();

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    if (!hasAutoLogged && event.getGui() instanceof DisconnectedScreen) {
      DisconnectedScreen screen = (DisconnectedScreen) event.getGui();
      // TODO: 1.15 add button to screen using reflection
    }

//    if (!hasAutoLogged) {
//      if (event.getGui() instanceof DisconnectedScreen
//          && !(event.getGui() instanceof GuiDisconnectedOverride)) {
//        updateLastConnectedServer();
//        DisconnectedScreen disconnected = (DisconnectedScreen) event.getGui();
//        event.setGui(
//            new GuiDisconnectedOverride(
//                FastReflection.Fields.GuiDisconnected_parentScreen.get(disconnected),
//                "connect.failed",
//                FastReflection.Fields.GuiDisconnected_message.get(disconnected),
//                FastReflection.Fields.GuiDisconnected_reason.get(disconnected),
//                delay.get()));
//      }
//    }
  }

  @SubscribeEvent
  public void onWorldLoad(ClientWorldEvent.Load event) {
    // we got on the server or stopped joining, now undo queue
    hasAutoLogged = false; // make mod work when you rejoin
  }

  @SubscribeEvent
  public void onWorldUnload(ClientWorldEvent.Unload event) {
    updateLastConnectedServer();
  }

  /*
  public static class GuiDisconnectedOverride extends DisconnectedScreen {
    
    private DisconnectedScreen parent;
    private ITextComponent message;
    
    // delay * 1000 = seconds to miliseconds
    private long reconnectTime;
    
    private Button reconnectButton = null;
    
    public GuiDisconnectedOverride(DisconnectedScreen screen,
        String reasonLocalizationKey,
        ITextComponent chatComp,
        String reason,
        double delay) {
      super(screen, reasonLocalizationKey, chatComp);
      parent = screen;
      message = chatComp;
      reconnectTime = System.currentTimeMillis() + (long) (delay * 1000);
      // set variable 'reason' to the previous classes value
      FastReflection.Fields.GuiDisconnected_reason.set(this, reason);
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
      if (reconnectButton != null) {
        reconnectButton.displayString = getFormattedReconnectText();
      }
      if (System.currentTimeMillis() >= reconnectTime) {
        reconnect();
      }
    }
  }*/
}
