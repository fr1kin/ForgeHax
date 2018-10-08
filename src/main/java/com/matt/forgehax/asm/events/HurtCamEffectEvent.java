package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;

public class HurtCamEffectEvent extends Event implements Cancelable {
    private final float partialTicks;

    public HurtCamEffectEvent(float pt) {
        partialTicks = pt;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
