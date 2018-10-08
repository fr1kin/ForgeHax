package com.matt.forgehax.asm.events;

import com.matt.forgehax.asm.events.abstractevents.LivingEvent;
import com.matt.forgehax.util.event.Cancelable;
import net.minecraft.entity.EntityLivingBase;

public class ApplyClimbableBlockMovement extends LivingEvent implements Cancelable {
    public ApplyClimbableBlockMovement(EntityLivingBase entity) {
        super(entity);
    }
}
