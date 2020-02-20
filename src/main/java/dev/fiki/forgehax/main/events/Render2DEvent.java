package dev.fiki.forgehax.main.events;

import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.draw.BufferBuilderEx;
import dev.fiki.forgehax.main.util.draw.BufferProvider;
import dev.fiki.forgehax.main.util.draw.SurfaceBuilder;
import lombok.Getter;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.eventbus.api.Event;

import static dev.fiki.forgehax.main.Common.getBufferProvider;

/**
 * Created on 9/2/2017 by fr1kin
 */
@Getter
public class Render2DEvent extends Event {
  private final SurfaceBuilder surfaceBuilder = new SurfaceBuilder();
  private final float partialTicks;
  
  public Render2DEvent(float partialTicks) {
    this.partialTicks = partialTicks;
  }

  public BufferBuilderEx getBuffer() {
    return getBufferProvider().getDefaultBuffer();
  }

  public float getPartialTicks() {
    return partialTicks;
  }
  
  public int getScreenWidth() {
    return Common.getScreenWidth();
  }
  
  public int getScreenHeight() {
    return Common.getScreenHeight();
  }
  
  public SurfaceBuilder getSurfaceBuilder() {
    return surfaceBuilder;
  }
}
