package dev.fiki.forgehax.main.services;

import dev.fiki.forgehax.main.ui.ConsoleInputScreen;
import dev.fiki.forgehax.main.ui.ConsoleInterface;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.events.PreClientTickEvent;
import dev.fiki.forgehax.main.util.key.KeyConflictContexts;
import dev.fiki.forgehax.main.util.key.KeyInputs;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    if(getDisplayScreen() != null
        && getDisplayScreen().getListener() instanceof TextFieldWidget) {
      return;
    }

    cli.onKeyPressed(bind);
  }

  @SubscribeEvent
  public void onGuiInit(GuiScreenEvent.InitGuiEvent.Pre event) {
    cli.onRescale(getScreenWidth(), getScreenHeight());
  }

  @SubscribeEvent
  public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
    if(getDisplayScreen() == null && RenderGameOverlayEvent.ElementType.ALL.equals(event.getType())) {
      cli.onRender();
    }
  }

  @SubscribeEvent
  public void onRenderGui(GuiScreenEvent.DrawScreenEvent.Post event) {
    if(getDisplayScreen() != null) {
      cli.onRender();
    }
  }

  @SubscribeEvent
  public void onTick(PreClientTickEvent event) {
    cli.onTick();
  }

  @SubscribeEvent
  public void onGuiOpen(GuiOpenEvent event) {
    ConsoleInputScreen con = cli.getConsoleScreen();
    if(con != null
        && !con.isClosing()
        && event.getGui() != con
        && cli.isConsoleOpen()) {
      con.setPreviousScreen(event.getGui());
      event.setGui(con);
    }
  }
}
