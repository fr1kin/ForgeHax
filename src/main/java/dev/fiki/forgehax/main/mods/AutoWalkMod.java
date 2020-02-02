package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.key.Bindings;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AutoWalkMod extends ToggleMod {
  
  public final Setting<Boolean> stop_at_unloaded_chunks =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("stop_at_unloaded_chunks")
          .description("Stops moving at unloaded chunks")
          .defaultTo(true)
          .build();
  
  private boolean isBound = false;
  
  public AutoWalkMod() {
    super(Category.PLAYER, "AutoWalk", false, "Automatically walks forward");
  }
  
  @Override
  public void onDisabled() {
    if (isBound) {
      Bindings.forward.setPressed(false);
      Bindings.forward.unbind();
      isBound = false;
    }
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (!isBound) {
      Bindings.forward.bind();
      isBound = true;
    }
    if (!Bindings.forward.getBinding().isKeyDown()) {
      Bindings.forward.setPressed(true);
    }
    
    if (stop_at_unloaded_chunks.get()) {
      if (Common.getWorld().isAreaLoaded(Common.getLocalPlayer().getPosition(), 1)) {
        Bindings.forward.setPressed(false);
      }
    }
  }
}
