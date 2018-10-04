package com.matt.forgehax.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class EntityAddedEvent extends EntityEvent {
  public EntityAddedEvent(Entity entity) {
    super(entity);
  }
}
