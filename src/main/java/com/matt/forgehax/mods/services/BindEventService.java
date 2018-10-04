package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getGlobalCommand;

import com.matt.forgehax.util.command.CommandStub;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class BindEventService extends ServiceMod {
  public BindEventService() {
    super("BindEventService");
  }

  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    getGlobalCommand()
        .getChildrenDeep()
        .stream()
        .filter(command -> command instanceof CommandStub)
        .map(command -> (CommandStub) command)
        .filter(stub -> stub.getBind() != null)
        .forEach(
            stub -> {
              if (stub.getBind().isPressed()) stub.onKeyPressed();
              if (stub.getBind().isKeyDown()) stub.onKeyDown();
            });
  }
}
