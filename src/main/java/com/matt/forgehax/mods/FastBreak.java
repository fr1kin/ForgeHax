package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class FastBreak extends ToggleMod {
  
  public FastBreak() {
    super(Category.PLAYER, "FastBreak", false, "Fast break retard");
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (MC.playerController != null) {
      FastReflection.Fields.PlayerControllerMP_blockHitDelay.set(MC.playerController, 0);
    }
  }
}
