package com.matt.forgehax.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class OnEntityRemoved extends EntityEvent {
    public OnEntityRemoved(Entity entity) {
        super(entity);
    }
}
