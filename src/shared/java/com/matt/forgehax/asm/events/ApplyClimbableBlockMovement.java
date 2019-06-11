package com.matt.forgehax.asm.events;

import net.minecraft.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class ApplyClimbableBlockMovement extends LivingEvent {
  public ApplyClimbableBlockMovement(LivingEntity entity) {
    super(entity);
  }
}
