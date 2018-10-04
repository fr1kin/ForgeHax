package com.matt.forgehax.events;

import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

/** Created on 5/5/2017 by fr1kin */
public class RenderEvent extends Event {
  private final GeometryTessellator tessellator;
  private final Vec3d renderPos;

  public RenderEvent(GeometryTessellator tessellator, Vec3d renderPos) {
    this.tessellator = tessellator;
    this.renderPos = renderPos;
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
}
