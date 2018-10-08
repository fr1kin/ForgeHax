package com.matt.forgehax.events;

import com.matt.forgehax.asm.events.abstractevents.EntityEvent;
import net.minecraft.entity.Entity;

public class EntityAddedEvent extends EntityEvent {
    public EntityAddedEvent(Entity entity) {
        super(entity);
    }
}
