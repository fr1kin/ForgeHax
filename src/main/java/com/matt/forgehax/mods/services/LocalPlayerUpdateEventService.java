package com.matt.forgehax.mods.services;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.LivingUpdateEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.event.Event;

/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class LocalPlayerUpdateEventService extends ServiceMod {
    public LocalPlayerUpdateEventService() {
        super("LocalPlayerUpdateEventService");
    }

    @Subscribe
    public void onUpdate(LivingUpdateEvent event) {
        if(MC.world != null &&
                event.getEntity().equals(MC.player)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntity());
            ForgeHax.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        }
    }
}
