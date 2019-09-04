package com.matt.forgehax.asm.events;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ApplyCollisionMotionEvent extends Event {

  private Entity entity;
  private Entity collidedWithEntity;

  private double motionX;
  private double motionY;
  private double motionZ;

  public ApplyCollisionMotionEvent(
    Entity entity, Entity collidedWithEntity, double mX, double mY, double mZ) {
    this.entity = entity;
    this.collidedWithEntity = collidedWithEntity;
    motionX = mX;
    motionY = mY;
    motionZ = mZ;
  }

  public Entity getEntity() {
    return entity;
  }

  public Entity getCollidedWithEntity() {
    return collidedWithEntity;
  }

  public double getMotionX() {
    return motionX;
  }

  public double getMotionY() {
    return motionY;
  }

  public double getMotionZ() {
    return motionZ;
  }
}
