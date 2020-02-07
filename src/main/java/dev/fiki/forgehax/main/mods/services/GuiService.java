package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.gui.ClickGui;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

import static dev.fiki.forgehax.main.Common.*;

/**
 * Created by Babbaj on 9/10/2017.
 */
@RegisterMod
public class GuiService extends ServiceMod {

  private final KeyBindingSetting guiBind = newKeyBindingSetting()
      .name("bind")
      .description("Bind to open the gui")
      .keyName("GUI")
      .keyCategory("ForgeHax")
      .keyCode(GLFW.GLFW_KEY_RIGHT_SHIFT)
      .keyPressedListener(this::onBindPressed)
      .build();
  
  public GuiService() {
    super("GUI");
  }
  
  private void onBindPressed(KeyBinding bind) {
    if (getLocalPlayer() != null) {
      MC.displayGuiScreen(ClickGui.getInstance());
    }
  }
}
