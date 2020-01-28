package com.matt.forgehax.mods;

import com.matt.forgehax.asm.events.WorldCheckLightForEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 2/10/2018 by fr1kin
 */
// TODO: 1.15 might have changed
public class NoSkylightUpdates extends ToggleMod {
  
  public NoSkylightUpdates() {
    super(Category.RENDER, "NoSkylightUpdates", false, "Prevents skylight updates");
  }
  
//  @SubscribeEvent
//  public void onLightingUpdate(WorldCheckLightForEvent event) {
//
//    if (event.getEnumSkyBlock() == EnumSkyBlock.SKY) {
//      event.setCanceled(true);
//    }
//  }
}
