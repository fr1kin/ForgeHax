package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.entity.EntityLivingBase;

public abstract class RenderLivingEvent extends Event {

  private final EntityLivingBase entity;
  private final float partialTicks;
  private final double x;
  private final double y;
  private final double z;

  public RenderLivingEvent(EntityLivingBase entity, float partialTicks, double x, double y, double z) {
    this.entity = entity;
    this.partialTicks = partialTicks;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public EntityLivingBase getEntity() {
    return this.entity;
  }


  public float getPartialTicks() {
    return partialTicks;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  public static class Pre extends RenderLivingEvent implements Cancelable {
    public Pre(EntityLivingBase entity, float ticks, double x, double y, double z) {
      super(entity, ticks, x, y, z);
    }
  }

  public static class Post extends RenderLivingEvent {
    public Post(EntityLivingBase entity, float ticks, double x, double y, double z) {
      super(entity, ticks, x, y, z);
    }
  }

}