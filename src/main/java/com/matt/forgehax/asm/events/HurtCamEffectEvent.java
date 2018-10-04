package com.matt.forgehax.asm.events;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class HurtCamEffectEvent extends Event {
  private final float partialTicks;

  public HurtCamEffectEvent(float pt) {
    partialTicks = pt;
  }

  public float getPartialTicks() {
    return partialTicks;
  }
}
