package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.modloader.RegisterMod;
import net.minecraft.potion.Effects;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Created on 11/28/2016 by fr1kin
 */
@RegisterMod(
    name = "AntiLevitation",
    description = "No levitation",
    category = Category.PLAYER
)
public class AntiLevitationMod extends ToggleMod {
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (Common.getLocalPlayer().isPotionActive(Effects.LEVITATION)) {
      Common.getLocalPlayer().removeActivePotionEffect(Effects.LEVITATION);
    }
  }
}
