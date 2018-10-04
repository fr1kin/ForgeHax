package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 9/4/2016 by fr1kin */
@RegisterMod
public class FastPlaceMod extends ToggleMod {
  public FastPlaceMod() {
    super(Category.PLAYER, "FastPlace", false, "Fast place");
  }

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    FastReflection.Fields.Minecraft_rightClickDelayTimer.set(MC, 0);
  }
}
