package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;

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
