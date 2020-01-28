package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

@RegisterMod
public class FastBreak extends ToggleMod {
  
  public FastBreak() {
    super(Category.PLAYER, "FastBreak", false, "Fast break retard");
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getPlayerController() != null) {
      FastReflection.Fields.PlayerControllerMP_blockHitDelay.set(getPlayerController(), 0);
    }
  }
}
