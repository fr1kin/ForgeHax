package com.matt.forgehax.asm.events.abstractevents;

import net.minecraft.entity.EntityLivingBase;

public abstract class LivingEvent extends EntityEvent<EntityLivingBase> {

  public LivingEvent(EntityLivingBase entity) {
    super(entity);
  }

}
