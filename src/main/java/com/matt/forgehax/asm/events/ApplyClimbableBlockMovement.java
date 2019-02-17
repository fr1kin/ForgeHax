package com.matt.forgehax.asm.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ApplyClimbableBlockMovement extends LivingEvent {
  public ApplyClimbableBlockMovement(EntityLivingBase entity) {
    super(entity);
  }
}
