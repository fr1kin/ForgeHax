package com.matt.forgehax.events;

import com.matt.forgehax.util.tesselation.GeometryTessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 5/5/2017 by fr1kin
 */
public class RenderEvent extends Event {
  
  private final GeometryTessellator tessellator;
  private final Vec3d renderPos;
  private final double partialTicks;
  
  public RenderEvent(GeometryTessellator tessellator, Vec3d renderPos, double partialTicks) {
    this.tessellator = tessellator;
    this.renderPos = renderPos;
    this.partialTicks = partialTicks;
  }
  
  public GeometryTessellator getTessellator() {
    return tessellator;
  }
  
  public BufferBuilder getBuffer() {
    return tessellator.getBuffer();
  }
  
  public Vec3d getRenderPos() {
    return renderPos;
  }
  
  public void setTranslation(Vec3d translation) {
    getBuffer().setTranslation(-translation.x, -translation.y, -translation.z);
  }
  
  public void resetTranslation() {
    setTranslation(renderPos);
  }
  
  public double getPartialTicks() {
    return partialTicks;
  }
}
