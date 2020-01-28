package com.matt.forgehax.events;

import static com.matt.forgehax.Globals.MC;

import com.matt.forgehax.util.draw.SurfaceBuilder;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.VirtualScreen;
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
    return MC.func_228018_at_().getScaledWidth();
  }
  
  public int getScreenHeight() {
    return MC.func_228018_at_().getScaledHeight();
  }
  
  public SurfaceBuilder getSurfaceBuilder() {
    return surfaceBuilder;
  }
}
