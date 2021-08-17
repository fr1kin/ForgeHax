package dev.fiki.forgehax.main.mods.misc;

import dev.fiki.forgehax.api.SimpleTimer;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.asm.MapMethod;
import dev.fiki.forgehax.api.cmd.flag.EnumFlag;
import dev.fiki.forgehax.api.cmd.settings.LongSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.events.render.GuiChangedEvent;
import dev.fiki.forgehax.api.events.world.WorldLoadEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.api.reflection.types.ReflectionMethod;
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
  @MapMethod(parentClass = Screen.class, value = "addButton")
  private final ReflectionMethod<Widget> Screen_addButton;
  @MapField(parentClass = ConnectingScreen.class, value = "parent")
  private final ReflectionField<Screen> ConnectingScreen_parent;

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
      long mins = ms / (1000 * 60);
      time = mins + "m";
      ms -= mins * (1000 * 60);
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

  @SubscribeListener
  public void onGuiOpened(GuiChangedEvent event) {
    if (event.getGui() instanceof ConnectingScreen) {
      // in the connecting screen constructor the server data is set
      lastServer = MC.getCurrentServer();
      forceDisconnected = false;
      previousScreen = ConnectingScreen_parent.get(event.getGui());
    } else if (!forceDisconnected && event.getGui() instanceof DisconnectedScreen) {
      timer.start();
    } else {
      timer.reset();
      button = null;
    }
  }

  @SubscribeListener
  public void onTick(PreGameTickEvent event) {
    if (lastServer == null) {
      lastServer = MC.getCurrentServer();
    }

    if (timer.isStarted() && getDisplayScreen() instanceof DisconnectedScreen) {
      if (button != null) {
        button.setMessage(new StringTextComponent(reconnectMessage()));
      } else {
        DisconnectedScreen screen = (DisconnectedScreen) getDisplayScreen();

        int textHeight = IBidiRenderer.EMPTY.getLineCount() * 9;
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

  @SubscribeListener
  public void onWorldLoad(WorldLoadEvent event) {
    forceDisconnected = false;
  }
}
