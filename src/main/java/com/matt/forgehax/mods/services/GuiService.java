package com.matt.forgehax.mods.services;

import com.matt.forgehax.Helper;
import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.util.command.StubBuilder;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import org.lwjgl.input.Keyboard;

/** Created by Babbaj on 9/10/2017. */
@RegisterMod
public class GuiService extends ServiceMod {
  public GuiService() {
    super("GUI");
  }

  @Override
  public void onBindPressed(CallbackData cb) {
    if (Helper.getLocalPlayer() != null) {
      MC.displayGuiScreen(ClickGui.getInstance());
    }
  }

  @Override
  protected StubBuilder buildStubCommand(StubBuilder builder) {
    return builder
        .kpressed(this::onBindPressed)
        .kdown(this::onBindKeyDown)
        .bind(Keyboard.KEY_RSHIFT) // default to right shift
    ;
  }
}
