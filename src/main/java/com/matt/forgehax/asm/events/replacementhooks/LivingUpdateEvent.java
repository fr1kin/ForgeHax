package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class LivingUpdateEvent extends Event implements Cancelable {

    private final EntityLivingBase entityLiving;

    public LivingUpdateEvent(EntityLivingBase entity)
    {
        entityLiving = entity;
    }

    public EntityLivingBase getEntity()
    {
        return entityLiving;
    }
}
