package com.matt.forgehax.mods.services;

import com.matt.forgehax.events.WorldChangeEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Created on 6/14/2017 by fr1kin */
@RegisterMod
public class WorldEventService extends ServiceMod {
  private static final WorldListener WORLD_LISTENER = new WorldListener();

  public WorldEventService() {
    super("WorldEventService");
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    event.getWorld().addEventListener(WORLD_LISTENER);
    MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    MinecraftForge.EVENT_BUS.post(new WorldChangeEvent(event.getWorld()));
  }
}
