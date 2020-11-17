package dev.fiki.forgehax.main.mods.render;

import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.asm.events.render.HurtCamEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod(
    name = "AntiHurtcam",
    description = "Removes hurt camera effect",
    category = Category.RENDER
)
public class AntiHurtCamMod extends ToggleMod {
  @SubscribeEvent
  public void onHurtCamEffect(HurtCamEffectEvent event) {
    event.setCanceled(true);
  }
}
