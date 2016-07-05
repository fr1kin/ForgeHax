package com.matt.forgehax.events;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class OnLocalPlayerUpdate extends LivingEvent {
    public OnLocalPlayerUpdate(EntityLivingBase e) {
        super(e);
    }
}
