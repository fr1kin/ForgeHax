package com.matt.forgehax.mods;

import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.matt.forgehax.Globals.*;

/**
 * Created on 11/28/2016 by fr1kin
 */
@RegisterMod
public class AntiLevitationMod extends ToggleMod {
  
  public AntiLevitationMod() {
    super(Category.PLAYER, "AntiLevitation", false, "No levitation");
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getLocalPlayer().isPotionActive(Effects.LEVITATION)) {
      getLocalPlayer().removeActivePotionEffect(Effects.LEVITATION);
    }
  }
}
