package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.main.util.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.main.util.reflection.FastReflection;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiEffectsMod extends ToggleMod {

  public final BooleanSetting no_particles = newBooleanSetting()
      .name("no-particles")
      .description("Stops the particle effect from rendering on other entities")
      .defaultTo(true)
      .build();

  public AntiEffectsMod() {
    super(Category.RENDER, "AntiPotionEffects", false, "Removes potion effects");
  }

  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    event.getEntity().setInvisible(false);
    event.getEntityLiving().removePotionEffect(Effects.NAUSEA);
    event.getEntityLiving().removePotionEffect(Effects.INVISIBILITY);
    event.getEntityLiving().removePotionEffect(Effects.BLINDNESS);

    // removes particle effect
    FastReflection.Methods.LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (no_particles.getValue()) {
      event.getEntity().setInvisible(false);
      FastReflection.Methods.LivingEntity_resetPotionEffectMetadata.invoke(event.getEntityLiving());
    }
  }
}
