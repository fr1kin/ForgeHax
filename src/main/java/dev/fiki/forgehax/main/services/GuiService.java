package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.api.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.game.PreGameTickEvent;
import dev.fiki.forgehax.api.events.render.GuiChangedEvent;
import dev.fiki.forgehax.api.events.render.GuiInitializeEvent;
import dev.fiki.forgehax.api.events.render.GuiRenderEvent;
import dev.fiki.forgehax.api.events.render.RenderPlaneEvent;
import dev.fiki.forgehax.api.key.KeyConflictContexts;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.ui.ConsoleInputScreen;
import dev.fiki.forgehax.main.ui.ConsoleInterface;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.settings.KeyBinding;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created by Babbaj on 9/10/2017.
 */
@RegisterMod("GUI")
@RequiredArgsConstructor
public class GuiService extends ServiceMod {
  private final ConsoleInterface cli;

  private final KeyBindingSetting guiBind = newKeyBindingSetting()
      .name("bind")
      .description("Bind to open the gui")
      .keyName("GUI")
      .defaultKeyCategory()
      .key(KeyInputs.KEY_RIGHT_SHIFT)
      .keyPressedListener(this::onBindPressed)
      .keyDownListener(this::onBindPressed)
      .conflictContext(KeyConflictContexts.none())
      .build();

  private void onBindPressed(KeyBinding bind) {
    if (getDisplayScreen() != null
        && getDisplayScreen().getFocused() instanceof TextFieldWidget) {
      return;
    }

    cli.onKeyPressed(bind);
  }

  @SubscribeListener
  public void onGuiInit(GuiInitializeEvent.Pre event) {
    cli.onRescale(getScreenWidth(), getScreenHeight());
  }

  @SubscribeListener
  public void onRenderOverlay(RenderPlaneEvent.Top event) {
    if (getDisplayScreen() == null) {
      cli.onRender();
    }
  }

  @SubscribeListener
  public void onRenderGui(GuiRenderEvent.Post event) {
    if (getDisplayScreen() != null) {
      cli.onRender();
    }
  }

  @SubscribeListener
  public void onTick(PreGameTickEvent event) {
    cli.onTick();
  }

  @SubscribeListener
  public void onGuiOpen(GuiChangedEvent event) {
    ConsoleInputScreen con = cli.getConsoleScreen();
    if (con != null
        && !con.isClosing()
        && event.getGui() != con
        && cli.isConsoleOpen()) {
      con.setPreviousScreen(event.getGui());
      event.setGui(con);
    }
  }
}
