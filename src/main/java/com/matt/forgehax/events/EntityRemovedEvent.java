package com.matt.forgehax.events;

import com.matt.forgehax.asm.events.abstractevents.EntityEvent;
import net.minecraft.entity.Entity;

public class EntityRemovedEvent extends EntityEvent {
    public EntityRemovedEvent(Entity entity) {
        super(entity);
    }
}
