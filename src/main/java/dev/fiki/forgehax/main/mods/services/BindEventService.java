package dev.fiki.forgehax.main.mods.services;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.command.CommandStub;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class BindEventService extends ServiceMod {
  
  public BindEventService() {
    super("BindEventService");
  }
  
  @SubscribeEvent
  public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    Common.GLOBAL_COMMAND.getChildrenDeep().stream()
        .filter(command -> command instanceof CommandStub)
        .map(command -> (CommandStub) command)
        .filter(stub -> stub.getBind() != null)
        .forEach(
            stub -> {
              if (stub.getBind().isPressed()) {
                stub.onKeyPressed();
              }
              if (stub.getBind().isKeyDown()) {
                stub.onKeyDown();
              }
            });
  }
}
