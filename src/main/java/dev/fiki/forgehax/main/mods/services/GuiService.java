package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.gui.ClickGui;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.command.StubBuilder;
import dev.fiki.forgehax.main.util.command.callbacks.CallbackData;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Babbaj on 9/10/2017.
 */
@RegisterMod
public class GuiService extends ServiceMod {
  
  public GuiService() {
    super("GUI");
  }
  
  @Override
  public void onBindPressed(CallbackData cb) {
    if (Globals.getLocalPlayer() != null) {
      Globals.MC.displayGuiScreen(ClickGui.getInstance());
    }
  }
  
  @Override
  protected StubBuilder buildStubCommand(StubBuilder builder) {
    return builder
      .kpressed(this::onBindPressed)
      .kdown(this::onBindKeyDown)
      .bind(GLFW.GLFW_KEY_RIGHT_SHIFT) // default to right shift
      ;
  }
}
