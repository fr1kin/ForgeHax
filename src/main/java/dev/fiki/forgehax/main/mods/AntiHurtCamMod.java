package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.asm.events.render.HurtCamEffectEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AntiHurtcam",
    description = "Removes hurt camera effect",
    category = Category.PLAYER
)
public class AntiHurtCamMod extends ToggleMod {
  @SubscribeEvent
  public void onHurtCamEffect(HurtCamEffectEvent event) {
    event.setCanceled(true);
  }
}
