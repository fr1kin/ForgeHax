package com.matt.forgehax.asm.events;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

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
