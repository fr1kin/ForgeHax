package com.matt.forgehax.mods;

import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class AntiEffectsMod extends ToggleMod {
  
  public final Setting<Boolean> no_particles =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
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
    FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(event.getEntityLiving());
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if(no_particles.get()) {
      event.getEntity().setInvisible(false);
      FastReflection.Methods.EntityLivingBase_resetPotionEffectMetadata.invoke(event.getEntityLiving());
    }
  }
}
