package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.HurtCamEffectEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiHurtCamMod extends ToggleMod {
  
  public AntiHurtCamMod() {
    super(Category.PLAYER, "AntiHurtcam", false, "Removes hurt camera effect");
  }
  
  @SubscribeEvent
  public void onHurtCamEffect(HurtCamEffectEvent event) {
    event.setCanceled(true);
  }
}
