package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
    if (Globals.getLocalPlayer().isPotionActive(Effects.LEVITATION)) {
      Globals.getLocalPlayer().removeActivePotionEffect(Effects.LEVITATION);
    }
  }
}
