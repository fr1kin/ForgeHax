package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class RenderNametagEvent extends Event implements Cancelable {

  private final EntityLivingBase entity;
  private final double x;
  private final double y;
  private final double z;

  public RenderNametagEvent(EntityLivingBase entity, double x, double y, double z) {
    this.entity = entity;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public EntityLivingBase getEntity() { return entity; }

  public double getX() { return x; }
  public double getY() { return y; }
  public double getZ() { return z; }


}
