package com.matt.forgehax.asm.events.abstractevents;

import com.matt.forgehax.util.event.Event;
import net.minecraft.entity.Entity;

public abstract class EntityEvent<T extends Entity> extends Event {

  private final T entity;

  public EntityEvent(T entity)
  {
    this.entity = entity;
  }

  public T getEntity()
  {
    return entity;
  }

}
