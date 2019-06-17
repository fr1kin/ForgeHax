package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiEffectsMod extends ToggleMod {
  public final Setting<Boolean> no_particles =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("no_particles")
          .description("Stops the particle effect from rendering on other entities")
          .defaultTo(true)
          .build();

  public AntiEffectsMod() {
    super(Category.RENDER, "AntiPotionEffects", false, "Removes potion effects");
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    LivingEntity living = event.getEntityLiving();
    if (living.equals(MC.player)) {
      living.setInvisible(false);
      living.removePotionEffect(Effects.field_76431_k); // nausea
      living.removePotionEffect(Effects.field_76441_p); // invisibility
      living.removePotionEffect(Effects.field_76440_q); // blindness
      // removes particle effect
      FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
    } else if (no_particles.get()) {
      living.setInvisible(false);
      FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(living);
    }
  }
}
