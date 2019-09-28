package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
      if (!getWorld().getChunkFromBlockCoords(getLocalPlayer().getPosition()).isLoaded()) {
        Bindings.forward.setPressed(false);
      }
    }
  }
}
