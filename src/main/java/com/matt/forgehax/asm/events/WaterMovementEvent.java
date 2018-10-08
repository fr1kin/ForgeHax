package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import com.matt.forgehax.util.event.Event;

public class WaterMovementEvent extends Event implements Cancelable {
    private Entity entity;
    private Vec3d movement;

    public WaterMovementEvent(Entity entity, Vec3d movement) {
        this.entity = entity;
        this.movement = movement;
    }

    public Entity getEntity() {
        return entity;
    }

    public Vec3d getMoveDir() {
        return movement;
    }
}
