package com.matt.forgehax.events;

import com.matt.forgehax.asm.events.abstractevents.LivingEvent;
import net.minecraft.entity.EntityLivingBase;

public class LocalPlayerUpdateEvent extends LivingEvent {
    public LocalPlayerUpdateEvent(EntityLivingBase e) {
        super(e);
    }
}
