package com.matt.forgehax.mods.services;

import com.matt.forgehax.mods.services.ChatCommandService;
import com.matt.forgehax.util.command.StubBuilder;
import com.matt.forgehax.util.command.callbacks.CallbackData;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

/**
 * Created by OverFloyd
 * may 2020
 */
@RegisterMod
public class PrefixChatService extends ServiceMod {

  public PrefixChatService() {
      super("PrefixChat");
  }

  @Override
  public void onBindPressed(CallbackData cb){
    if (MC.currentScreen == null) {
      char clientPrefix = ChatCommandService.getActivationCharacter();
      MC.displayGuiScreen(new GuiChat("" + clientPrefix));
    }
  }

  @Override
  protected StubBuilder buildStubCommand(StubBuilder builder) {
    return builder
      .kpressed(this::onBindPressed)
      .kdown(this::onBindKeyDown)
      .bind(Keyboard.KEY_COMMA) // default to comma
      ;
  }
}
