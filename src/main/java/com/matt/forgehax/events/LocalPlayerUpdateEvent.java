package com.matt.forgehax.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

public class LocalPlayerUpdateEvent extends LivingEvent {
  
  public LocalPlayerUpdateEvent(EntityLivingBase e) {
    super(e);
  }
}
