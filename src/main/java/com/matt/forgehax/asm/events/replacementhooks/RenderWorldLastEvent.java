package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Event;
import net.minecraft.client.renderer.RenderGlobal;

public class RenderWorldLastEvent
{
  private final RenderGlobal context;
  private final float partialTicks;

  public RenderWorldLastEvent(RenderGlobal context, float partialTicks)
  {
    this.context = context;
    this.partialTicks = partialTicks;
  }

  public RenderGlobal getContext()
  {
    return context;
  }

  public float getPartialTicks()
  {
    return partialTicks;
  }
}