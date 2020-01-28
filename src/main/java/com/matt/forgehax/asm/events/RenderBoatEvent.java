package com.matt.forgehax.asm.events;

import net.minecraft.entity.item.BoatEntity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created by Babbaj on 9/2/2017.
 */
@Cancelable
public class RenderBoatEvent extends Event {
  
  private float yaw;
  private BoatEntity boat;
  
  public RenderBoatEvent(BoatEntity boatIn, float entityYaw) {
    this.boat = boatIn;
    this.yaw = entityYaw;
  }
  
  public void setYaw(float yawIn) {
    this.yaw = yawIn;
  }
  
  public float getYaw() {
    return this.yaw;
  }
  
  public BoatEntity getBoat() {
    return this.boat;
  }
}
