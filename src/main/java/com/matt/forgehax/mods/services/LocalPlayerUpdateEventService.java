package com.matt.forgehax.mods.services;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class LocalPlayerUpdateEventService extends ServiceMod {
    public LocalPlayerUpdateEventService() {
        super("LocalPlayerUpdateEventService");
    }

    @Subscribe
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(MC.world != null &&
                event.getEntityLiving().equals(MC.player)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
            ForgeHax.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        }
    }
}
