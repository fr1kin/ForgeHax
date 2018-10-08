package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import net.minecraft.entity.item.EntityBoat;
import com.matt.forgehax.util.event.Event;

/**
 * Created by Babbaj on 9/2/2017.
 */
public class RenderBoatEvent extends Event implements Cancelable {
    private float yaw;
    private EntityBoat boat;


    public RenderBoatEvent(EntityBoat boatIn, float entityYaw) {
        this.boat = boatIn;
        this.yaw = entityYaw;
    }

    public void setYaw(float yawIn) {
        this.yaw = yawIn;
    }

    public float getYaw() {
        return this.yaw;
    }

    public EntityBoat getBoat() {
        return this.boat;
    }

}
