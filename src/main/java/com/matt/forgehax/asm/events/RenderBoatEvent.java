package com.matt.forgehax.asm.events;

import net.minecraft.entity.item.EntityBoat;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Created by Babbaj on 9/2/2017. */
@Cancelable
public class RenderBoatEvent extends Event {
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
