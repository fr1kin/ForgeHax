package com.matt.forgehax.events;

import static com.matt.forgehax.Globals.MC;

import com.matt.forgehax.util.draw.SurfaceBuilder;
import net.minecraftforge.eventbus.api.Event;

/** Created on 9/2/2017 by fr1kin */
public class Render2DEvent extends Event {
  //private final ScaledResolution resolution = new ScaledResolution(MC);
  private final SurfaceBuilder surfaceBuilder = new SurfaceBuilder();
  private final float partialTicks;

  public Render2DEvent(float partialTicks) {
    this.partialTicks = partialTicks;
  }

  public float getPartialTicks() {
    return partialTicks;
  }

  @Deprecated
  public double getScreenWidth() {
    //return resolution.getScaledWidth_double();
    return MC.mainWindow.getScaledWidth();
  }

  @Deprecated
  public double getScreenHeight() {
    //return resolution.getScaledHeight_double();
    return MC.mainWindow.getScaledHeight();
  }

  public SurfaceBuilder getSurfaceBuilder() {
    return surfaceBuilder;
  }
}
