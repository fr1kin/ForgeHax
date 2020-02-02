package dev.fiki.forgehax.main.events;

import static dev.fiki.forgehax.main.Common.MC;

import dev.fiki.forgehax.main.util.draw.SurfaceBuilder;
import net.minecraftforge.eventbus.api.Event;

/**
 * Created on 9/2/2017 by fr1kin
 */
public class Render2DEvent extends Event {

  private final SurfaceBuilder surfaceBuilder = new SurfaceBuilder();
  private final float partialTicks;
  
  public Render2DEvent(float partialTicks) {
    this.partialTicks = partialTicks;
  }
  
  public float getPartialTicks() {
    return partialTicks;
  }
  
  public int getScreenWidth() {
    return MC.getMainWindow().getScaledWidth();
  }
  
  public int getScreenHeight() {
    return MC.getMainWindow().getScaledHeight();
  }
  
  public SurfaceBuilder getSurfaceBuilder() {
    return surfaceBuilder;
  }
}
