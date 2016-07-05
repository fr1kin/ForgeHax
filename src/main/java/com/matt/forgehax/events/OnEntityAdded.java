package com.matt.forgehax.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class OnEntityAdded extends EntityEvent {
    public OnEntityAdded(Entity entity) {
        super(entity);
    }
}
