package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;
import net.minecraft.potion.Effects;

/**
 * Created on 11/28/2016 by fr1kin
 */
@RegisterMod(
    name = "AntiLevitation",
    description = "No levitation",
    category = Category.PLAYER
)
public class AntiLevitationMod extends ToggleMod {
  @SubscribeListener
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (Common.getLocalPlayer().hasEffect(Effects.LEVITATION)) {
      Common.getLocalPlayer().removeEffect(Effects.LEVITATION);
    }
  }
}
