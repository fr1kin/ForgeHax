package com.matt.forgehax.mods.services;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.asm.events.replacementhooks.WorldEvent;
import com.matt.forgehax.events.WorldChangeEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;


/**
 * Created on 6/14/2017 by fr1kin
 */
@RegisterMod
public class WorldEventService extends ServiceMod {
    private static final WorldListener WORLD_LISTENER = new WorldListener();

    public WorldEventService() {
        super("WorldEventService");
    }

    @Subscribe
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(WORLD_LISTENER);
        ForgeHax.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
    }

    @Subscribe
    public void onWorldUnload(WorldEvent.Unload event) {
        ForgeHax.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
    }
}
