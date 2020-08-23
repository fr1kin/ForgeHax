package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mapper.MethodMapping;
import dev.fiki.forgehax.main.util.SimpleTimer;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.LongSetting;
import dev.fiki.forgehax.main.util.events.ClientWorldEvent;
import dev.fiki.forgehax.main.util.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionField;
import dev.fiki.forgehax.main.util.reflection.types.ReflectionMethod;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.IBidiRenderer;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getDisplayScreen;
import static dev.fiki.forgehax.main.Common.setDisplayScreen;

@RegisterMod(
    name = "AutoReconnect",
    description = "Automatically reconnects to server",
    category = Category.MISC,
    flags = EnumFlag.HIDDEN
)
@RequiredArgsConstructor
public class AutoReconnectMod extends ToggleMod {
  @MethodMapping(parentClass = Screen.class, value = "addButton")
  private final ReflectionMethod<Widget> Screen_addButton;
  @FieldMapping(parentClass = ConnectingScreen.class, value = "previousGuiScreen")
  private final ReflectionField<Screen> ConnectingScreen_previousGuiScreen;

  public final LongSetting delay = newLongSetting()
      .name("delay")
      .description("Delay between each reconnect attempt (in seconds)")
      .defaultTo(5)
      .build();

  private final SimpleTimer timer = new SimpleTimer();

  @Setter
  private boolean forceDisconnected = false;
  private ServerData lastServer = null;
  private Screen previousScreen = null;
  private Button button = null;

  private String reconnectMessage() {
    String time = "";
    long ms = delay.intValue() * 1000 - timer.getTimeElapsed();

    // minutes
    if (ms >= 1000 * 60) {
      long mins = ms / (1000*60);
      time = mins + "m";
      ms -= mins * (1000*60);
    }

    // seconds
    if (ms >= 1000) {
      long sec = ms / 1000;
      time += sec + "s";
      ms -= sec * 1000;
    }

    // miliseconds
    if (ms > 0) {
      time += String.format("%03dms", ms);
    } else {
      return "Reconnecting now";
    }

    return "Reconnecting in " + time;
  }

  private void reconnect() {
    if (previousScreen != null && lastServer != null) {
      timer.reset();
      button = null;
      setDisplayScreen(new ConnectingScreen(previousScreen, MC, lastServer));
    }
  }

  @Override
  protected void onDisabled() {
    timer.reset();
    button = null;
    previousScreen = null;
    forceDisconnected = false;
  }

  @SubscribeEvent
  public void onGuiOpened(GuiOpenEvent event) {
    if (event.getGui() instanceof ConnectingScreen) {
      // in the connecting screen constructor the server data is set
      lastServer = MC.getCurrentServerData();
      forceDisconnected = false;
      previousScreen = ConnectingScreen_previousGuiScreen.get(event.getGui());
    } else if (!forceDisconnected && event.getGui() instanceof DisconnectedScreen) {
      timer.start();
    } else {
      timer.reset();
      button = null;
    }
  }

  @SubscribeEvent
  public void onTick(PreClientTickEvent event) {
    if (lastServer == null) {
      lastServer = MC.getCurrentServerData();
    }

    if (timer.isStarted() && getDisplayScreen() instanceof DisconnectedScreen) {
      if (button != null) {
        button.setMessage(new StringTextComponent(reconnectMessage()));
      } else {
        DisconnectedScreen screen = (DisconnectedScreen) getDisplayScreen();

        int textHeight = IBidiRenderer.field_243257_a.func_241862_a() * 9;
        button = new Button(
            screen.width / 2 - 100,
            Math.min(screen.height / 2 + textHeight / 2 + 9, screen.height - 30) + 33,
            200, 20,
            new StringTextComponent(reconnectMessage()),
            b -> reconnect());

        Screen_addButton.invoke(screen, button);
      }

      if (timer.getTimeElapsed() >= delay.intValue() * 1000) {
        reconnect();
      }
    }
  }

  @SubscribeEvent
  public void onWorldLoad(ClientWorldEvent.Load event) {
    forceDisconnected = false;
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
